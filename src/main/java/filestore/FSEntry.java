package filestore;

public class FSEntry {
	String name;
	int version;
	long size;
	boolean isTombstone;

	public FSEntry() {

	}

	public FSEntry(String name, int version, long size, boolean isTombstone) {
		super();
		this.name = name;
		this.version = version;
		this.size = size;
		this.isTombstone = isTombstone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public boolean isTombstone() {
		return isTombstone;
	}

	public void setTombstone(boolean isTombstone) {
		this.isTombstone = isTombstone;
	}

	@Override
	public String toString() {
		return "FSEntry [name=" + name + ", version=" + version + ", size=" + size + ", isTombstone=" + isTombstone
				+ "]";
	}

}
