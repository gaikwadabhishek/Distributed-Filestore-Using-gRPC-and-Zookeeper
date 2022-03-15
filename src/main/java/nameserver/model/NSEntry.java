package nameserver.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NSEntry {

	String fileName;
	Map<Integer, NSVersion> versions;

	public NSEntry() {
		versions = new ConcurrentHashMap<>();
	}

	public NSEntry(String fileName, Map<Integer, NSVersion> versions) {
		super();
		this.fileName = fileName;
		this.versions = versions;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Map<Integer, NSVersion> getVersions() {
		return versions;
	}

	public void setVersions(HashMap<Integer, NSVersion> versions) {
		this.versions = versions;
	}

	public void addVersion(int version, NSVersion nsVersion) {
		this.versions.put(version, nsVersion);
	}

	@Override
	public String toString() {
		return "NSEntry [fileName=" + fileName + ", versions=" + versions + "]";
	}

}
