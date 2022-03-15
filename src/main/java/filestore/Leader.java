package filestore;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// referred from - https://www.oreilly.com/library/view/zookeeper/9781449361297/ch04.html#callout_dealing_with_state_change_CO2-1
public class Leader implements Watcher, Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(Leader.class);

	public static final String IP = NSConfig.ZOOKEEPER_IP;
	public static final String ZK_PATH_CONFIG = NSConfig.ZK_PATH_CONFIG;

	public enum MasterStates {
		RUNNING, ELECTED, NOTELECTED
	}

	private volatile static MasterStates state = MasterStates.RUNNING;

	public static MasterStates getState() {
		return state;
	}

	private Random random = new Random(this.hashCode());
	private static ZooKeeper zk;
	private String serverId = Integer.toHexString(random.nextInt());
	private volatile boolean connected = false;
	private volatile boolean expired = false;

	void startZK() throws IOException {
		zk = new ZooKeeper(IP, 15000, this);
	}

	void stopZK() throws InterruptedException, IOException {
		zk.close();
	}

	@Override
	public void process(WatchedEvent e) {
		System.out.println("Processing event: " + e.toString());
		if (e.getType() == Event.EventType.None) {
			switch (e.getState()) {
			case SyncConnected:
				connected = true;
				break;
			case Disconnected:
				connected = false;
				System.out.println("SESSION DISCONNECTED");
				break;
			case Expired:
				expired = true;
				connected = false;
				LOG.error("Session expiration");
			default:
				break;
			}
		}
	}

	boolean isConnected() {
		return connected;
	}

	boolean isExpired() {
		return expired;
	}

	StringCallback masterCreateCallback = new StringCallback() {
		@Override
		public void processResult(int rc, String path, Object ctx, String name) {
			switch (Code.get(rc)) {
			case CONNECTIONLOSS:
				checkMaster();

				break;
			case OK:
				System.out.println("Waiting 30 after becoming leader");
				try {
					Thread.sleep(NSConfig.SERVER_START_COOLDOWN * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				state = MasterStates.ELECTED;
				takeLeadership();

				break;
			case NODEEXISTS:
				state = MasterStates.NOTELECTED;
				masterExists();

				break;
			default:
				state = MasterStates.NOTELECTED;
				LOG.error("Something went wrong when running for master.", KeeperException.create(Code.get(rc), path));
			}
			System.out.println("I'm " + (state == MasterStates.ELECTED ? "" : "not ") + "the leader " + serverId);
		}
	};

	void masterExists() {
		zk.exists(ZK_PATH_CONFIG + "/leader", masterExistsWatcher, masterExistsCallback, null);
	}

	StatCallback masterExistsCallback = new StatCallback() {
		@Override
		public void processResult(int rc, String path, Object ctx, Stat stat) {
			switch (Code.get(rc)) {
			case CONNECTIONLOSS:
				masterExists();
				break;
			case OK:
				break;
			case NONODE:
				state = MasterStates.RUNNING;
				runForMaster();
				System.out.println("It sounds like the previous master is gone, " + "so let's run for master again.");

				break;
			default:
				checkMaster();
				break;
			}
		}
	};

	Watcher masterExistsWatcher = new Watcher() {
		@Override
		public void process(WatchedEvent e) {
			if (e.getType() == EventType.NodeDeleted) {

				runForMaster();
			}

		}
	};

	void takeLeadership() {
		System.out.println("TAKING LEADERSHIP");
		NameServer.watchMaxVersionZNode(zk, ZK_PATH_CONFIG + "/maxver");
	}

	public void runForMaster() {
		System.out.println("Running for master");
		zk.create(ZK_PATH_CONFIG + "/leader",
				(NSConfig.NAME_SERVER_HOST_PORT + "\nAbhishek Gaikwad").getBytes(StandardCharsets.UTF_8),
				Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, masterCreateCallback, null);
	}

	DataCallback masterCheckCallback = new DataCallback() {
		@Override
		public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
			switch (Code.get(rc)) {
			case CONNECTIONLOSS:
				checkMaster();
				break;
			case NONODE:
				runForMaster();

				break;
			case OK:
				if (serverId.equals(new String(data))) {
					state = MasterStates.ELECTED;
					takeLeadership();
				} else {
					state = MasterStates.NOTELECTED;
					masterExists();
				}

				break;
			default:
				LOG.error("Error when reading data.", KeeperException.create(Code.get(rc), path));
			}
		}
	};

	void checkMaster() {
		zk.getData("/test/leader", false, masterCheckCallback, null);
	}

	public static void setMinVersion(int maxVersion) throws KeeperException, InterruptedException {
		zk.setData(ZK_PATH_CONFIG + "/minver", ("" + maxVersion).getBytes(StandardCharsets.UTF_8),
				zk.exists(ZK_PATH_CONFIG + "/minver", true).getVersion());

	}

	public static void setMaxVersion(int maxVersion) throws KeeperException, InterruptedException {
		zk.setData(ZK_PATH_CONFIG + "/maxver", ("" + maxVersion).getBytes(StandardCharsets.UTF_8),
				zk.exists(ZK_PATH_CONFIG + "/maxver", true).getVersion());

	}

	@Override
	public void close() throws IOException {
		if (zk != null) {
			try {
				zk.close();
			} catch (InterruptedException e) {
				System.out.printf("Interrupted while closing ZooKeeper session.", e);
			}
		}
	}

	public static void main(String args[]) throws Exception {
		Leader m = new Leader();
		m.startZK();

		while (!m.isConnected()) {
			Thread.sleep(100);
		}

		m.runForMaster();

		while (!m.isExpired()) {
			Thread.sleep(1000);
		}

		m.stopZK();
	}

}