// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts2;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store TeamSpeak 2 user information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class User extends DatabaseBean implements Comparable, Cacheable {
	
	private String _userID;
	private String _pwd;
	
	private int _serverID;
	private boolean _serverAdmin;
	
	private Date _createdOn;
	private Date _lastOnline;

	/**
	 * Creates a new TeamSpeak 2 user.
	 * @param userID the user ID
	 * @throws NullPointerException if userID is null
	 * @see User#setUserID(String)
	 */
	public User(String userID) {
		super();
		setUserID(userID);
		_createdOn = new Date();
	}

	/**
	 * Returns the user's TeamSpeak User ID.
	 * @return the user ID
	 * @see User#setUserID(String)
	 */
	public String getUserID() {
		return _userID;
	}
	
	/**
	 * Returns the user's password. This may not be populated.
	 * @return the password, or null if not populated
	 * @see User#setPassword(String)
	 */
	public String getPassword() {
		return _pwd;
	}
	
	/**
	 * Returns the applicable TeamSpeak server ID. <i>This is usually 1</i>.
	 * @return the server ID
	 * @see User#setServerID(int)
	 */
	public int getServerID() {
		return _serverID;
	}
	
	/**
	 * Returns wether the user is a Server Administrator.
	 * @return TRUE if the user is an Administrator, otherwise FALSE
	 * @see User#setServerAdmin(boolean)
	 */
	public boolean getServerAdmin() {
		return _serverAdmin;
	}
	
	/**
	 * Returns the date the user was created on.
	 * @return the creation date/time
	 * @see User#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the date the user was last connected to TeamSpeak on.
	 * @return the last online date/time
	 * @see User#setLastOnline(Date)
	 */
	public Date getLastOnline() {
		return _lastOnline;
	}
	
	/**
	 * Updates the TeamSpeak user ID.
	 * @param id the user ID
	 * @throws NullPointerException if id is null
	 * @see User#setUserID(String)
	 */
	public void setUserID(String id) {
		_userID = id.toUpperCase();
	}
	
	/**
	 * Updates the user's password.
	 * @param pwd the password
	 * @see User#getPassword()
	 */
	public void setPassword(String pwd) {
		_pwd = pwd;
	}
	
	/**
	 * Sets the server database ID. <i>This is usually 1</i>.
	 * @param id the server database ID
	 * @throws IllegalArgumentException if id is negative
	 * @see User#getServerID()
	 * @see DatabaseBean#validateID(int, int)
	 */
	public void setServerID(int id) {
		if (id != 0) {
			validateID(_serverID, id);
			_serverID = id;
		}
	}
	
	/**
	 * Updates wether the user is a Server Administrator. 
	 * @param isAdmin TRUE if the user is an administrator, otherwise FALSE
	 * @see User#getServerAdmin()
	 */
	public void setServerAdmin(boolean isAdmin) {
		_serverAdmin = isAdmin;
	}
	
	/**
	 * Updates the creation date of this Teamspeak user.
	 * @param dt the creation date/time
	 * @see User#getCreatedOn()
	 * @see User#setLastOnline(Date)
	 */
	public void setCreatedOn(Date dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the last online date of this Teamspeak user
	 * @param dt the last online date/time
	 * @throws IllegalArgumentException if dt is before CreatedOn
	 * @see User#getLastOnline()
	 * @see User#setCreatedOn(Date)
	 */
	public void setLastOnline(Date dt) {
		if ((dt != null) && dt.before(_createdOn))
			throw new IllegalArgumentException("Last Online cannot be before Created");
		
		_lastOnline = dt;
	}
	
	/**
	 * Comapres two users by comparing their TS2 user ID.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		User usr = (User) o;
		return _userID.compareTo(usr._userID);
	}
	
	/**
	 * Returns the user's cache key.
	 * @return the user ID
	 */
	public Object cacheKey() {
		return _userID;
	}
}