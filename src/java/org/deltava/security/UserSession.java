// Copyright 2004, 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.*;

/**
 * A bean to store data about a User session.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class UserSession implements Comparable<UserSession> {

	private String _sessionID;
	private String _userAgent;
	private Pilot _p;
	private IPAddressInfo _addrInfo;

	/**
	 * Creates a new User session bean.
	 * @param p the Pilot associated with this session
	 * @param sessionID the HTTP session ID
	 * @param addrInfo the IP address info
	 * @param userAgent the user-agent header
	 */
	public UserSession(Pilot p, String sessionID, IPAddressInfo addrInfo, String userAgent) {
		super();
		_p = p;
		_sessionID = sessionID;
		_userAgent = userAgent;
		_addrInfo = addrInfo;
	}

	/**
	 * Returns the authenticated User associated with this session.
	 * @return the Person
	 */
	public Pilot getPerson() {
		return _p;
	}

	/**
	 * Returns the HTTP session ID.
	 * @return the HTTP session ID
	 */
	String getSessionID() {
		return _sessionID;
	}
	
	/**
	 * Returns information about the IP address.
	 * @return an IPAddressInfo bean
	 */
	public IPAddressInfo getAddressInfo() {
		return _addrInfo;
	}
	
	/**
	 * Returns the User-Agent header.
	 * @return the user-agent
	 */
	public String getUserAgent() {
		return _userAgent;
	}
	
	public int hashCode() {
		return _p.hashCode();
	}
	
	public String toString() {
		return _p.getName();
	}
	
	/**
	 * Compares two user sessions by comparing their users.
	 */
	public int compareTo(UserSession usr2) {
		return _p.compareTo(usr2._p);
	}
}