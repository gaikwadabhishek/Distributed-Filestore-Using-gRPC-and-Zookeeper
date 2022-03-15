package nameserver.model;

import java.util.Set;

import io.grpc.netty.shaded.io.netty.util.internal.ConcurrentSet;

public class NSVersion {

	int version;
	long size;
	boolean isTombstone;
	Set<String> hostPorts;

	public NSVersion() {
		hostPorts = new ConcurrentSet<>();
	}

	public NSVersion(int version, long size, boolean isTombstone, Set<String> hostPorts) {
		super();
		this.version = version;
		this.size = size;
		this.isTombstone = isTombstone;
		this.hostPorts = hostPorts;
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

	public Set<String> getHostPorts() {
		return hostPorts;
	}

	public void setHostPorts(Set<String> hostPorts) {
		this.hostPorts = hostPorts;
	}

	public void addHostPort(String hostPort) {
		this.hostPorts.add(hostPort);
	}

	public void removeHostPort(String hostPort) {
		this.hostPorts.remove(hostPort);
	}

	@Override
	public String toString() {
		return "NSVersion [version=" + version + ", size=" + size + ", isTombstone=" + isTombstone + ", hostPorts="
				+ hostPorts + "]";
	}

}
