// Copyright 2007, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.*;

/**
 * A bean to store Login address information for a user.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class LoginAddress extends DatabaseBean implements RemoteAddressBean {
	
	private String _remoteAddr;
	private String _remoteHost;
	private int _logins;

	/**
	 * Initializes the bean.
	 * @param id the database ID of the user 
	 */
	public LoginAddress(int id) {
		super();
		setID(id);
	}

	@Override
	public String getRemoteAddr() {
		return _remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return _remoteHost;
	}

	/**
	 * Returns the number of logins by this user from this address.
	 * @return the number of logins
	 * @see LoginAddress#setLoginCount(int)
	 */
	public int getLoginCount() {
		return _logins;
	}
	
	/**
	 * Updates the IP address used to log in.
	 * @param addr the IP address
	 * @see LoginAddress#getRemoteAddr()
	 */
	public void setRemoteAddr(String addr) {
		_remoteAddr = addr;
	}
	
	/**
	 * Updates the host name used to log in.
	 * @param host the host name
	 * @see LoginAddress#getRemoteHost()
	 */
	public void setRemoteHost(String host) {
		_remoteHost = host;
	}
	
	/**
	 * Updates the number of logins by this user from this address.
	 * @param logins the number of logins
	 * @throws IllegalArgumentException if logins is negative
	 * @see LoginAddress#getLoginCount()
	 */
	public void setLoginCount(int logins) {
		if (logins < 0)
			throw new IllegalArgumentException("Invalid Login Count - " + logins);
		
		_logins = logins;
	}
}