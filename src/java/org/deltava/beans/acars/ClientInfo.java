// Copyright 2011, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * A bean to store ACARS client versions.
 * @author Luke
 * @version 7.5
 * @since 4.1
 */

public class ClientInfo implements ClientVersion, Comparable<ClientVersion>, java.io.Serializable {
	
	private final int _version;
	private final int _build;
	private final int _beta;
	
	private ClientType _type = ClientType.PILOT;

	/**
	 * Creates a new client information bean.
	 * @param version the version number
	 * @param build the build number
	 */
	public ClientInfo(int version, int build) {
		this(version, build, Integer.MAX_VALUE);
	}

	/**
	 * Creates a new client information bean.
	 * @param version the version number
	 * @param build the build number
	 * @param beta the beta number
	 */
	public ClientInfo(int version, int build, int beta) {
		super();
		_version = Math.max(1, version);
		_build = Math.max(1, build);
		_beta = (beta < 1) ? Integer.MAX_VALUE : beta;
	}

	@Override
	public int getVersion() {
		return _version;
	}
	
	@Override
	public int getClientBuild() {
		return _build;
	}
	
	@Override
	public int getBeta() {
		return _beta;
	}
	
	@Override
	public ClientType getClientType() {
		return _type;
	}
	
	public boolean isATC() {
		return (_type == ClientType.ATC);
	}
	
	public boolean isDispatch() {
		return (_type == ClientType.DISPATCH);
	}
	
	/**
	 * Updates the client type.
	 * @param ct the ClientType
	 */
	public void setClientType(ClientType ct) {
		_type = ct;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(String.valueOf(_version));
		buf.append('.');
		buf.append(_build);
		if (isBeta()) {
			buf.append('b');
			buf.append(_beta);
		}
		
		return buf.toString();
	}

	@Override
	public int hashCode() {
		StringBuilder buf = new StringBuilder(_type.toString());
		buf.append(toString());
		return buf.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return ((o instanceof ClientInfo) && (compareTo((ClientInfo) o) == 0));
	}
	
	@Override
	public int compareTo(ClientVersion c2) {
		int tmpResult = _type.compareTo(c2.getClientType());
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_version).compareTo(Integer.valueOf(c2.getVersion()));
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_build).compareTo(Integer.valueOf(c2.getClientBuild()));
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_beta).compareTo(Integer.valueOf(c2.getBeta()));
		
		return tmpResult;
	}
}