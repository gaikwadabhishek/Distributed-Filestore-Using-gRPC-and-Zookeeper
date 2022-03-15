package filestore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import grpc.FileStoreGrpc;
import grpc.FileStoreGrpc.FileStoreBlockingStub;
import grpc.NameServerGrpc.NameServerImplBase;
import grpc.Qddfs.BumpVersionRequest;
import grpc.Qddfs.DeleteFileRequest;
import grpc.Qddfs.NSAddReply;
import grpc.Qddfs.NSAddRequest;
import grpc.Qddfs.NSBeatReply;
import grpc.Qddfs.NSBeatRequest;
import grpc.Qddfs.NSCreateReply;
import grpc.Qddfs.NSCreateRequest;
import grpc.Qddfs.NSDeleteReply;
import grpc.Qddfs.NSDeleteRequest;
import grpc.Qddfs.NSListReply;
import grpc.Qddfs.NSListRequest;
import grpc.Qddfs.NSNameEntry;
import grpc.Qddfs.NSReadReply;
import grpc.Qddfs.NSReadRequest;
import grpc.Qddfs.NSRegisterReply;
import grpc.Qddfs.NSRegisterRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import nameserver.model.NSEntry;
import nameserver.model.NSVersion;

public class NameServer extends NameServerImplBase {

	public class ActiveFS {
		/*
		 * referred from
		 * https://crunchify.com/clean-expired-element-from-map-while-adding-elements-at
		 * -the-same-time-java-timer-timertask-and-futures-complete-examples/
		 */
		public ActiveFS(int seconds) {

			timer = new Timer();
			timer.schedule(new ActiveFSReminder(), 0, seconds * 1000L);

			// timer to check for total versions created in 60 seconds
			timer.schedule(new ActiveFSVersionCounter(), 120 * 1000L, 60 * 1000L);
		}

		// count the number of versions created in 60 seconds
		class ActiveFSVersionCounter extends TimerTask {
			@Override
			public void run() {
				System.out.println("checking if max version has bumped 10 versions in 1 minute");
				List<String> fileNames = new ArrayList<>();
				if (versionCounter > 10) {
					// bump active files of FS
					for (Entry<String, NSEntry> entry : nsEntries.entrySet()) {
						Map<Integer, NSVersion> versions = entry.getValue().getVersions();
						int maxVer = Collections.max(versions.keySet());
						if (!versions.get(maxVer).isTombstone()) {
							fileNames.add(entry.getKey());
						}

					}

					for (String hostPort : ActiveFSMap.keySet()) {
						ManagedChannel channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();

						FileStoreBlockingStub fileStore = FileStoreGrpc.newBlockingStub(channel);
						BumpVersionRequest request = BumpVersionRequest.newBuilder().addAllName(fileNames)
								.setNewVersion(bumpVersion).build();
						fileStore.bumpVersion(request);
					}

					// change minver to bumped version number
					try {
						Leader.setMinVersion(bumpVersion);
						bumpVersion = maxVersion;
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}
				}
				versionCounter = 0;
			}
		}

		class ActiveFSReminder extends TimerTask {
			@Override
			public void run() {
				System.out.println("checking heartbeat status " + ActiveFSMap.toString());
				fsClearExpiredElementsFromMap();
			}
		}

		private void fsClearExpiredElementsFromMap() {
			for (Entry<String, Instant> entry : ActiveFSMap.entrySet()) {
				if (Instant.now().isAfter(entry.getValue().plusSeconds(EXPIRED_TIME_IN_SEC))) {
					String hostPort = entry.getKey();
					System.out.println("REMOVING " + hostPort + " from ActiveFSMap");
					ActiveFSMap.remove(hostPort);
					System.out.println("HashMap = " + ActiveFSMap.toString());
					removeHostPortFromNSEntry(hostPort);

				}
			}

		}
	}

	Timer timer;
	private static final long EXPIRED_TIME_IN_SEC = 30L;
	public static Map<String, Instant> ActiveFSMap = new ConcurrentHashMap<>();

	static Map<String, NSEntry> nsEntries = new ConcurrentHashMap<>();
	public static final int PORT = NSConfig.NAME_SERVER_PORT;
	public static final String NAMESERVER_HOST_PORT = NSConfig.NAME_SERVER_HOST_PORT;

	public static int bumpVersion = 1;
	public static int maxVersion = 1;
	public static int nextMaxVersion = 11; // 10 + 1
	public static int versionCounter = 0;

	public static synchronized void removeHostPortFromNSEntry(String hostPort) {
		for (Entry<String, NSEntry> nsEntry : nsEntries.entrySet()) {
			Map<Integer, NSVersion> versions = nsEntry.getValue().getVersions();
			Set<Integer> keySet = new HashSet<>(versions.keySet());
			for (int version : keySet) {
				if (versions.get(version).getHostPorts().contains(hostPort)) {
					versions.get(version).removeHostPort(hostPort);
					if (versions.get(version).getHostPorts().isEmpty()) {
						versions.remove(version);
						if (versions.isEmpty()) {
							nsEntries.remove(nsEntry.getKey());
						}
					}
				}
			}
		}
		System.out.println("removing " + hostPort + " files :: " + nsEntries.toString());
	}

	// add latest version logic
	@Override
	public void doCreate(NSCreateRequest request, StreamObserver<NSCreateReply> responseObserver) {
		try {
			if (!Leader.getState().equals(Leader.MasterStates.ELECTED)) {

				responseObserver.onNext(NSCreateReply.newBuilder().setRc(2).build());
				responseObserver.onCompleted();
				return;
			}
			List<String> hostPorts = new ArrayList<>();
			System.out.println("RECEIVED doCreate request");
			int count = 0;
			for (String hostPort : ActiveFSMap.keySet()) {
				hostPorts.add(hostPort);
				count++;
				if (count == 2) {
					break;
				}
			}
			System.out.println("RETURNING " + hostPorts.toString() + " in doCreate request");

			// if current version is greater than the next max version (/maxver + 10) then
			// set /maxver + 10
			if (maxVersion >= nextMaxVersion) {
				nextMaxVersion = nextMaxVersion + 10;
				Leader.setMaxVersion(nextMaxVersion);
			}

			// creating file with version max_version + 1
			responseObserver.onNext(
					NSCreateReply.newBuilder().addAllHostPort(hostPorts).setVersion(maxVersion++).setRc(0).build());

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occurred while handling do create request");
			responseObserver.onNext(NSCreateReply.newBuilder().setRc(1).build());
		} finally {
			responseObserver.onCompleted();
		}

	}

	// doRead(path) - this will return the list of host:port pairs that store the
	// file.
	@Override
	public void doRead(NSReadRequest request, StreamObserver<NSReadReply> responseObserver) {

		NSReadReply readReply = null;
		try {
			if (!Leader.getState().equals(Leader.MasterStates.ELECTED)) {
				System.out.println("Im not the master or I'm currenly waiting 30 seconds");
				int count = 0;

				responseObserver.onNext(NSReadReply.newBuilder().setRc(2).build());
				responseObserver.onCompleted();
				return;
			}

			String name = request.getName();

			if (nsEntries.containsKey(name)) {
				Map<Integer, NSVersion> versions = nsEntries.get(name).getVersions();
				int maxVersion = Collections.max(versions.keySet());
				if (!versions.get(maxVersion).isTombstone()) {
					NSVersion version = versions.get(maxVersion);
					readReply = NSReadReply.newBuilder().addAllHostPort(version.getHostPorts()).setRc(0).build();
				} else {
					// file is deleted
					readReply = NSReadReply.newBuilder().setRc(1).build();
				}
			} else {
				// file not found
				readReply = NSReadReply.newBuilder().setRc(1).build();
			}
			responseObserver.onNext(readReply);
		} catch (Exception e) {
			System.out.println("Error occurred in do read method");
			e.printStackTrace();
			// readReply = NSReadReply.newBuilder().setRc(1).build();
		} finally {
			responseObserver.onCompleted();
		}

	}

	// doDelete(path) - this will return success or failure. The name node will
	// contact the FileStores that store the file and delete them.
	@Override
	public void doDelete(NSDeleteRequest request, StreamObserver<NSDeleteReply> responseObserver) {
		if (!Leader.getState().equals(Leader.MasterStates.ELECTED)) {
			System.out.println(
					"Im not the master or I'm currenly waiting 30 seconds, so holding request till master is elected");

			responseObserver.onNext(NSDeleteReply.newBuilder().setRc(2).build());
			responseObserver.onCompleted();
			return;
		}

		String name = request.getName();

		NSDeleteReply deleteReply;
		ManagedChannel channel = null;

		if (nsEntries.containsKey(name)) {
			Map<Integer, NSVersion> versions = nsEntries.get(name).getVersions();
			int maxVersion = Collections.max(versions.keySet());
			System.out.println("Max Version :" + maxVersion);
			if (!versions.get(maxVersion).isTombstone()) {
				NSVersion version = versions.get(maxVersion);

				System.out.println("Version :" + version.toString());
				// delete file from file stores
				for (String hostPort : version.getHostPorts()) {
					System.out.println("Sending delete request of file " + name + " to " + hostPort);
					channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();

					FileStoreBlockingStub fileStore = FileStoreGrpc.newBlockingStub(channel);

					DeleteFileRequest deleteRequest = DeleteFileRequest.newBuilder().setName(name)
							.setVersion(maxVersion + 1).build();

					fileStore.deleteFile(deleteRequest);

				}

				if (channel != null)
					channel.shutdown();

				deleteReply = NSDeleteReply.newBuilder().setRc(0).build();
			} else {
				// file is deleted
				deleteReply = NSDeleteReply.newBuilder().setRc(1).build();
			}
		} else {
			// file not found
			deleteReply = NSDeleteReply.newBuilder().setRc(1).build();
		}
		responseObserver.onNext(deleteReply);
		responseObserver.onCompleted();
	}

	@Override
	public void heartBeat(NSBeatRequest request, StreamObserver<NSBeatReply> responseObserver) {

		String hostport = request.getHostPort();
		System.out.println("Received HeartBeat request from " + hostport);

		ActiveFSMap.put(hostport, Instant.now());

		responseObserver.onNext(NSBeatReply.newBuilder().setRc(0).build());
		responseObserver.onCompleted();
	}

	@Override
	public void list(NSListRequest request, StreamObserver<NSListReply> responseObserver) {

		if (!Leader.getState().equals(Leader.MasterStates.ELECTED)) {
			System.out.println(
					"Im not the master or I'm currenly waiting 30 seconds, so holding request till master is elected");

			NSListReply reply = NSListReply.newBuilder().setRc(2).addAllEntries(null).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
			return;
		}

		try {
			String pattern = request.getPattern();
			List<NSNameEntry> nsNameEntries = new ArrayList<>();
			for (String name : nsEntries.keySet()) {

				if (name.matches(pattern)) {
					NSEntry nsEntry = nsEntries.get(name);
					int maxVer = Collections.max(nsEntry.getVersions().keySet());

					if (nsEntry.getVersions().containsKey(maxVer)) {
						NSVersion version = nsEntry.getVersions().get(maxVer);

						if (!version.isTombstone()) {

							nsNameEntries.add(NSNameEntry.newBuilder().setName(name).setSize(version.getSize())
									.setVersion(maxVer).build());
						}

					}
				}

			}
			System.out.println("List returning  - " + nsNameEntries.toString());
			NSListReply reply = NSListReply.newBuilder().setRc(0).addAllEntries(nsNameEntries).build();
			responseObserver.onNext(reply);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			responseObserver.onCompleted();
		}
	}

	@Override
	public void registerFilesAndTombstones(NSRegisterRequest request,
			StreamObserver<NSRegisterReply> responseObserver) {

		// get hostport from request
		String hostPort = request.getHostPort();

		System.out.println("Received registerFilesAndTombstones request from : " + hostPort);

		// iterate over entries to add it to our data model
		for (grpc.Qddfs.FSEntry fsEntry : request.getEntriesList()) {
			String fileName = fsEntry.getName();

			// file exists
			if (nsEntries.containsKey(fileName)) {
				NSEntry nsEntry = nsEntries.get(fileName);
				int version = fsEntry.getVersion();

				// version exists - add host port
				if (nsEntry.getVersions().containsKey(version)) {
					nsEntry.getVersions().get(version).addHostPort(hostPort);
					nsEntry.getVersions().get(version).setTombstone(fsEntry.getIsTombstone());

				} else {
					// create that version and add the host ports
					Set<String> hostPorts = new HashSet<String>();
					hostPorts.add(hostPort);
					nsEntry.addVersion(version,
							new NSVersion(version, fsEntry.getSize(), fsEntry.getIsTombstone(), hostPorts));

				}
			} else {
				// file doesnt exist - add ns entry and version
				Map<Integer, NSVersion> versions = new HashMap<>();
				Set<String> hostPorts = new HashSet<>();
				hostPorts.add(hostPort);
				versions.put(fsEntry.getVersion(),
						new NSVersion(fsEntry.getVersion(), fsEntry.getSize(), fsEntry.getIsTombstone(), hostPorts));
				NSEntry nsEntry = new NSEntry(fileName, versions);
				nsEntries.put(fileName, nsEntry);
			}
		}

		System.out.println("the updated nsEntries is :" + nsEntries.toString());
		// send reply to hostport;
		NSRegisterReply reply = NSRegisterReply.newBuilder().setRc(0).build();
		responseObserver.onNext(reply);
		responseObserver.onCompleted();

	}

	@Override
	public void addFileOrTombstone(NSAddRequest request, StreamObserver<NSAddReply> responseObserver) {

		if (!Leader.getState().equals(Leader.MasterStates.ELECTED)) {
			// send reply to hostport;
			System.out.println("I'm not the leader, sending rc = 2");
			responseObserver.onNext(NSAddReply.newBuilder().setRc(2).build());
			responseObserver.onCompleted();
			return;
		}
		grpc.Qddfs.FSEntry fsEntry = request.getEntry();
		String hostPort = request.getHostPort();

		System.out.println("Received addFileOrTombstone request from - " + hostPort + " for " + fsEntry.toString());

		String fileName = fsEntry.getName();
		// file exists
		if (nsEntries.containsKey(fileName)) {
			NSEntry nsEntry = nsEntries.get(fileName);
			int version = fsEntry.getVersion();

			// version exists - add host port
			if (nsEntry.getVersions().containsKey(version)) {
				nsEntry.getVersions().get(version).addHostPort(hostPort);
				nsEntry.getVersions().get(version).setTombstone(fsEntry.getIsTombstone());
			} else {
				// create that version and add the host ports
				Set<String> hostPorts = new HashSet<>();
				hostPorts.add(hostPort);
				nsEntry.addVersion(version,
						new NSVersion(version, fsEntry.getSize(), fsEntry.getIsTombstone(), hostPorts));

			}
		} else {
			// file doesnt exist - add ns entry and version
			Map<Integer, NSVersion> versions = new HashMap<>();
			Set<String> hostPorts = new HashSet<>();
			hostPorts.add(hostPort);
			versions.put(fsEntry.getVersion(),
					new NSVersion(fsEntry.getVersion(), fsEntry.getSize(), fsEntry.getIsTombstone(), hostPorts));
			NSEntry nsEntry = new NSEntry(fileName, versions);
			nsEntries.put(fileName, nsEntry);
		}
		System.out.println("the updated nsEntries is :");
		System.out.println(nsEntries.toString());

		// send reply to hostport;
		responseObserver.onNext(NSAddReply.newBuilder().setRc(0).build());
		responseObserver.onCompleted();
	}
	/*
	 * the max version number (/maxver). This will have an integer (represented as a
	 * string) that is the max version number that the current NameServer will
	 * assign. When a new NameServer takes over, it will read /maxver and start
	 * assigning version numbers at /maxver+1 it will also set a new /maxver, which
	 * for this assignment will be /maxver+10. If the NameServer needs a higher
	 * version number than the current /maxver, it will need to bump /maxver by 10
	 * before using a higher number.
	 */

	// called from leader in takeLeaderhip();
	public static void watchMaxVersionZNode(ZooKeeper zookeeper, String controlPath) {
		try {

			try {
				maxVersion = Integer
						.parseInt(new String(zookeeper.getData(controlPath, null, null), StandardCharsets.UTF_8));

				System.out.println("MAX VERSION IS " + maxVersion);
				System.out.println("Setting /maxver to " + (maxVersion + 10));
				// setting max version to max version + 10
				nextMaxVersion = maxVersion + 10;
				zookeeper.setData(controlPath, ("" + nextMaxVersion).getBytes(StandardCharsets.UTF_8),
						zookeeper.exists(controlPath, true).getVersion());
				bumpVersion = maxVersion;

			} catch (Exception e) {
				System.out.println("Error occurred while trying to read max version");
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.out.println("Error occurred while trying to read max version");
			e.printStackTrace();
		}
	}

	private static void startServer() throws IOException, InterruptedException {

		System.out.println("NAMSERVER STARTING ON " + PORT);
		NameServer nameserver = new NameServer();

		// send heartbeat every 15 seconds
		nameserver.new ActiveFS(15);
		Server server = ServerBuilder.forPort(PORT).addService(nameserver).build();

		server.start();

		// server shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Received Shutdown Request");
			server.shutdown();
			System.out.println("Successfully stopped the server");
		}));

		server.awaitTermination();
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Leader leader = new Leader();
		leader.startZK();

		// pass number of seconds you want to check ActiveFS by as parameter

		while (!leader.isConnected()) {
			Thread.sleep(100);
		}

		leader.runForMaster();

		startServer();

		while (!leader.isExpired()) {
			Thread.sleep(1000);
		}

		leader.stopZK();

	}
}
