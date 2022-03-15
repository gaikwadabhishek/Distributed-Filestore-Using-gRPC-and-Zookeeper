package filestore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NSConfig {

	public static final String ZOOKEEPER_IP = "172.24.9.201:2181";
	public static final String ZK_PATH_CONFIG = "/test2";
	public static final int FILE_STORE_PORT = 50051;
	public static final String FILE_STORE_HOST_PORT = "172.24.9.102:" + FILE_STORE_PORT;
	public static final int NAME_SERVER_PORT = 50052;
	public static final String NAME_SERVER_HOST_PORT = "172.24.9.102:" + NAME_SERVER_PORT;
	// wait for 30 seconds, and then start serving requests
	public static final int SERVER_START_COOLDOWN = 30;

	public static final String FILESTORE_FOLDER = "C:/Users/Checkout/eclipse-workspace/Assignment5/target/"
			+ FILE_STORE_PORT + "/";
	public static final String MIN_VERSION_PATH = "/minver";

	public static ConcurrentMap<String, FSEntry> metaData = new ConcurrentHashMap<>();
	public static ConcurrentMap<String, Integer> deleteHistory = new ConcurrentHashMap<>();
	public static ObjectMapper mapper = new ObjectMapper();
	public static int finalRC = 0;

	public NSConfig() {
	}
}
