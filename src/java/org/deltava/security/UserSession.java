// Copyright 2004, 2005, 2006, 2007, 2009, 2011, 2012, 2015, 2016, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

/**
 * A bean to store data about a User session.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class UserSession implements java.io.Serializable, Comparable<UserSession> {

	private final String _sessionID;
	private final String _userAgent;
	private final Pilot _p;
	private final IPBlock _addrInfo;

	/**
	 * Creates a new User session bean.
	 * @param p the Pilot associated with this session
	 * @param sessionID the HTTP session ID
	 * @param addrInfo the IP block info
	 * @param userAgent the user-agent header
	 */
	public UserSession(Pilot p, String sessionID, IPBlock addrInfo, String userAgent) {
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
	 * Returns information about the IP address block.
	 * @return an IPBlock bean
	 */
	public IPBlock getAddressInfo() {
		return _addrInfo;
	}
	
	/**
	 * Returns the User-Agent header.
	 * @return the user-agent
	 */
	public String getUserAgent() {
		return _userAgent;
	}
	
	@Override
	public int hashCode() {
		return _p.hashCode();
	}
	
	@Override
	public String toString() {
		return _p.getName();
	}
	
	@Override
	public int compareTo(UserSession usr2) {
		return _p.compareTo(usr2._p);
	}
}