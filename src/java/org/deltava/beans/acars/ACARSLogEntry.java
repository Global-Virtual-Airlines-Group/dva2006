// Copyright 2005, 2007, 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.*;
import org.deltava.beans.system.IPAddressInfo;

/**
 * An abstract class for common ACRS log entry functions.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public abstract class ACARSLogEntry extends DatabaseBean implements ClientVersion, AuthoredBean {

	private IPAddressInfo _addrInfo;

	private int _clientVersion;
	private int _clientBuild;
	private int _beta;

	private String _remoteHost;
	private String _remoteAddr;

	/**
	 * Returns the date/time of this entry.
	 * @return the entry date/time
	 */
	public abstract Date getStartTime();

	/**
	 * Returns the IP address for this connection.
	 * @return the IP Address
	 * @see ConnectionEntry#setRemoteAddr(String)
	 * @see ConnectionEntry#getRemoteHost()
	 */
	public String getRemoteAddr() {
		return _remoteAddr;
	}

	/**
	 * Returns the host name for this connection.
	 * @return the host name
	 * @see ConnectionEntry#getRemoteAddr()
	 * @see ConnectionEntry#setRemoteHost(String)
	 */
	public String getRemoteHost() {
		return _remoteHost;
	}

	/**
	 * Returns information about this IP address.
	 * @return the IP address Info
	 * @see ConnectionEntry#setAddressInfo(IPAddressInfo)
	 */
	public IPAddressInfo getAddressInfo() {
		return _addrInfo;
	}

	public int getVersion() {
		return _clientVersion;
	}

	public int getClientBuild() {
		return _clientBuild;
	}

	public int getBeta() {
		return _beta;
	}

	public ClientType getClientType() {
		return ClientType.PILOT;
	}

	/**
	 * Updates the IP address for this connection.
	 * @param addr the IP address
	 * @see ConnectionEntry#getRemoteAddr()
	 * @see ConnectionEntry#setRemoteHost(String)
	 */
	public void setRemoteAddr(String addr) {
		_remoteAddr = addr;
	}

	/**
	 * Updates the host name for this connection.
	 * @param host the host name
	 * @see ConnectionEntry#getRemoteHost()
	 * @see ConnectionEntry#setRemoteAddr(String)
	 */
	public void setRemoteHost(String host) {
		_remoteHost = host;
	}

	public void setVersion(int ver) {
		_clientVersion = Math.max(1, ver);
	}

	public void setClientBuild(int ver) {
		_clientBuild = ver;
	}

	public void setBeta(int beta) {
		_beta = Math.max(0, beta);
	}
	
	   /**
	    * Updates information about this IP address.
	    * @param info the IP address Info
	    * @see ConnectionEntry#getAddressInfo()
	    */
	   public void setAddressInfo(IPAddressInfo info) {
		   _addrInfo = info;
	   }
}