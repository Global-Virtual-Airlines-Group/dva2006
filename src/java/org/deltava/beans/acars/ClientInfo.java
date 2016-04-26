// Copyright 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * A bean to store ACARS client versions.
 * @author Luke
 * @version 7.0
 * @since 4.1
 */

public class ClientInfo implements ClientVersion, Comparable<ClientInfo>, java.io.Serializable {
	
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
	
	/**
	 * Returns whether this is a beta build.
	 * @return TRUE if a beta build, otherwise FALSE
	 */
	public boolean isBeta() {
		return (_beta < Integer.MAX_VALUE);
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
	public int compareTo(ClientInfo c2) {
		int tmpResult = _type.compareTo(c2._type);
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_version).compareTo(Integer.valueOf(c2._version));
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_build).compareTo(Integer.valueOf(c2._build));
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_beta).compareTo(Integer.valueOf(c2._beta));
		
		return tmpResult;
	}
}