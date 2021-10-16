// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to mark beans with a remote IP address and host name. 
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public interface RemoteAddressBean {

	/**
	 * Returns the remote IP address.
	 * @return an IPv4 or IPv6 address
	 */
	public String getRemoteAddr();

	/**
	 * Returns the remote host name.
	 * @return the host name, or address if unresolved
	 */
	public String getRemoteHost();
	
	/**
	 * Returns if the host name has been resolved.
	 * @return TRUE if the host name is not null and does not equal the remote address, otherwise FALSE
	 */
	default boolean getIsResolved() {
		return ((getRemoteHost() != null) && !getRemoteHost().equals(getRemoteAddr()));
	}
}