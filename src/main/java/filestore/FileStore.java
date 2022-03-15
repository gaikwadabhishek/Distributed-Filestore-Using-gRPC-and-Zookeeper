package filestore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import grpc.NameServerGrpc;
import grpc.NameServerGrpc.NameServerBlockingStub;
import grpc.Qddfs.NSAddReply;
import grpc.Qddfs.NSAddRequest;
import grpc.Qddfs.NSBeatReply;
import grpc.Qddfs.NSBeatRequest;
import grpc.Qddfs.NSRegisterReply;
import grpc.Qddfs.NSRegisterRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class FileStore {

	// static List<FSEntry> listFiles = new ArrayList<>();
	public static final String LEADER_PATH = "/leader";
	public static String nameServerHostPort = "";
	public static boolean isNameServerAvailable = false;

	public static NameServerBlockingStub nameServer = null;

	public class HeartBeat {

		/*
		 * referred from
		 * https://crunchify.com/clean-expired-element-from-map-while-adding-elements-at
		 * -the-same-time-java-timer-timertask-and-futures-complete-examples/
		 */
		Timer timer;

		public HeartBeat(int seconds) {

			timer = new Timer();
			timer.schedule(new HeartBeatReminder(), 0, seconds * 1000L);
		}

		class HeartBeatReminder extends TimerTask {
			@Override
			public void run() {

				if (isNameServerAvailable)
					sendHeartBeatToNameserver();
			}
		}

		private void sendHeartBeatToNameserver() {
			System.out.println("Sending nameserver heartbeat at " + new Date().toString());
			try {
				NSBeatRequest heartBeatRequest = NSBeatRequest.newBuilder().setHostPort(NSConfig.FILE_STORE_HOST_PORT)
						.setBytesAvailable(0).setBytesUsed(0).build();
				NSBeatReply reply = nameServer.heartBeat(heartBeatRequest);
				if (reply.getRc() == 0) {
					System.out.println("heartbeat attemp SUCCESSFUL");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("heartbeat attempt UNSUCCESSFUL");
			}
		}
	}

	private static void startZK(String[] args) throws IOException, KeeperException, InterruptedException {
		// 172.24.9.107:2181 /test
		String zookeeperHostPortList = args[0];
		String controlPath = args[1];

		System.out.println("Zookeeper ip : " + zookeeperHostPortList);
		System.out.println("control path : " + controlPath);

		// creating zookeeper
		CountDownLatch connectionLatch = new CountDownLatch(1);
		ZooKeeper zookeeper = new ZooKeeper(zookeeperHostPortList, 1000, new Watcher() {
			@Override
			public void process(WatchedEvent we) {

				// do error handling
				if (we.getState() == KeeperState.SyncConnected) {
					connectionLatch.countDown();
				}
			}
		});

		connectionLatch.await();

		String reply = zookeeper.create(controlPath + "/replica-",
				(NSConfig.FILE_STORE_HOST_PORT + "\nAbhishek Gaikwad").getBytes(StandardCharsets.UTF_8),
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("My node name is  : " + reply);

		watchMinVersion(zookeeper, NSConfig.ZK_PATH_CONFIG + NSConfig.MIN_VERSION_PATH);

		fetchNameserverInformation(zookeeper, controlPath + LEADER_PATH);

	}

	private static void fetchNameserverInformation(ZooKeeper zookeeper, String controlPath)
			throws InterruptedException {
		try {
			System.out.println("WATCHING " + controlPath);
			Watcher dataChangeWatcher = new Watcher() {
				@Override
				public void process(WatchedEvent e) {
					System.out.println("EVENT E ==" + e.getType());

					if (e.getType() == EventType.NodeCreated || e.getType() == EventType.NodeDataChanged
							|| e.getType() == EventType.NodeDeleted) {
						try {
							fetchNameserverInformation(zookeeper, controlPath);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			};
			try {
				String[] nameAndIp = new String(zookeeper.getData(controlPath, dataChangeWatcher, null),
						StandardCharsets.UTF_8).split("\n", 2);

				System.out.println(
						"Current Nameserver Details : HOSTPORT :: " + nameAndIp[0] + " :: LEADER :: " + nameAndIp[1]);
				nameServerHostPort = nameAndIp[0];
				isNameServerAvailable = true;

				// call register files and tombstones
				registerFilesAndTombstones(nameServerHostPort);

			} catch (Exception e) {
				// zookeeper.exists(controlPath, dataChangeWatcher);
				System.out.println("NAMESERVER NOT AVAILABLE\nTrying after 5 seconds");
				e.printStackTrace();
				nameServerHostPort = "";
				isNameServerAvailable = false;
				Thread.sleep(5000);
				fetchNameserverInformation(zookeeper, controlPath);

			}

		} catch (Exception e) {
			System.out.println("NAMESERVER NOT AVAILABLE\nTrying after 5 seconds");
			Thread.sleep(5000);
			e.printStackTrace();
			nameServerHostPort = "";
			isNameServerAvailable = false;
			fetchNameserverInformation(zookeeper, controlPath);

		}
	}

	/*
	 * the minimum version (/minver). This will have an integer (represented as a
	 * string) that will indicate the minimum version of any active file or
	 * tombstone. FileStores will watch this znode and delete any files or
	 * tombstones they have with versions less than /minver.
	 */
	public static void watchMinVersion(ZooKeeper zookeeper, String controlPath) {
		try {
			System.out.println("WATCHING " + controlPath);
			Watcher dataChangeWatcher = new Watcher() {
				@Override
				public void process(WatchedEvent e) {
					System.out.println("EVENT E ==" + e.getType());
					if (e.getType() == EventType.NodeDataChanged) {
						watchMinVersion(zookeeper, controlPath);

					}
				}
			};
			try {
				int minVersion = Integer.parseInt(
						new String(zookeeper.getData(controlPath, dataChangeWatcher, null), StandardCharsets.UTF_8));

				System.out.println("MINVERSION IS " + minVersion);
				removeAllVersionsLessThanMinVersion(minVersion);

			} catch (Exception e) {
				System.out.println("Error occurred while trying to read min version");
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void removeAllVersionsLessThanMinVersion(int minVersion)
			throws JsonGenerationException, JsonMappingException, IOException {
		boolean changesToFSEntry = false;
		for (FSEntry fsEntry : NSConfig.metaData.values()) {
			if (fsEntry.getVersion() < minVersion) {
				changesToFSEntry = true;
				// remove from metaData
				System.out.println("deleting " + fsEntry.getName() + " as it was lesser than minversion");
				NSConfig.metaData.remove(fsEntry.getName());

				if (isNameServerAvailable) {

					try {
						NSAddRequest addFileRequest = NSAddRequest.newBuilder()
								.setEntry(grpc.Qddfs.FSEntry.newBuilder().setName(fsEntry.getName())
										.setVersion(fsEntry.getVersion()).setSize(0).setIsTombstone(true).build())
								.build();
						NSAddReply reply = FileStore.nameServer.addFileOrTombstone(addFileRequest);
						if (reply.getRc() == 0) {
							System.out.println("File added to nameserver");
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Tombstone not added to filestore due to error");
					}

				}
			}
		}
		if (changesToFSEntry) {
			// write to metadata file
			NSConfig.mapper.writeValue(new File(NSConfig.FILESTORE_FOLDER + "metaData.json"),
					NSConfig.metaData.values());
		}

	}

	private static void registerFilesAndTombstones(String hostPort) {

		System.out.println("REGISTERING FILES AND TOMBSTONES");
		ManagedChannel channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();
		nameServer = NameServerGrpc.newBlockingStub(channel);

		List<grpc.Qddfs.FSEntry> entries = new ArrayList<>();
		for (FSEntry entry : NSConfig.metaData.values()) {
			entries.add(grpc.Qddfs.FSEntry.newBuilder().setName(entry.getName()).setSize(entry.getSize())
					.setVersion(entry.getVersion()).setIsTombstone(entry.isTombstone()).build());
		}

		NSRegisterRequest request = NSRegisterRequest.newBuilder().addAllEntries(entries).setBytesAvailable(0)
				.setBytesUsed(0).setHostPort(NSConfig.FILE_STORE_HOST_PORT).build();
		try {
			NSRegisterReply reply = nameServer.registerFilesAndTombstones(request);
			if (reply.getRc() == 0) {
				System.out.println("FILES REGISTERED SUCCESSFULLY");
			} else {
				System.out.println("FILES NOT REGISTERED");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("NAMESERVER NOT AVAILABLE");
		}

	}

	private static void createAndLoadMetaData() throws IOException {
		File metaDataFile = new File(NSConfig.FILESTORE_FOLDER + "metaData.json");
		if (!metaDataFile.exists()) {
			Files.createDirectories(Paths.get(NSConfig.FILESTORE_FOLDER));
			List<FSEntry> list = new ArrayList<>();
			NSConfig.mapper.writeValue(new File(NSConfig.FILESTORE_FOLDER + "metaData.json"), list);
		} else {
			List<FSEntry> entries = Arrays.asList(
					NSConfig.mapper.readValue(new File(NSConfig.FILESTORE_FOLDER + "metaData.json"), FSEntry[].class));

			for (FSEntry entry : entries) {
				NSConfig.metaData.put(entry.getName(), entry);
			}
			System.out.println(NSConfig.metaData.toString());
		}
	}

	private static void startServer() throws IOException, InterruptedException {
		Server server = ServerBuilder.forPort(NSConfig.FILE_STORE_PORT).addService(new FileStoreImpl()).build();

		server.start();

		// server shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Received Shutdown Request");
			server.shutdown();
			System.out.println("Successfully stopped the server");
		}));

		server.awaitTermination();
	}

	public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

		// read from metaData file
		createAndLoadMetaData();

		if (args.length == 2) {
			startZK(args);
		} else {
			// Ben's server - 172.24.9.107:2181 /test
			args = new String[2];
			args[0] = NSConfig.ZOOKEEPER_IP;
			args[1] = NSConfig.ZK_PATH_CONFIG;
			startZK(args);
		}

		new FileStore().new HeartBeat(15);

		System.out.println(
				"Assignment 4 - QDDFS - Abhishek Gaikwad - Replica Running on port - " + NSConfig.FILE_STORE_PORT);

		// start server
		startServer();

	}
}
