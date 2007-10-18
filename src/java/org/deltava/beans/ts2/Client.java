// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts2;

import java.util.*;

import org.deltava.beans.DatabaseBean;

import org.deltava.util.UserID;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store TeamSpeak 2 user information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Client extends DatabaseBean implements Cacheable {
	
	private String _userID;
	private String _pwd;
	private int _serverID;
	
	private boolean _serverAdmin;
	private boolean _serverOperator;
	private boolean _autoVoice;
	
	private Date _createdOn = new Date();
	private Date _lastOnline;
	
	private boolean _isACARS;
	
	private final Collection<Integer> _channelIDs = new HashSet<Integer>();
	
	/**
	 * Creates a new TeamSpeak 2 user.
	 * @param userID the user ID
	 * @throws NullPointerException if userID is null
	 * @see Client#setUserID(String)
	 */
	public Client(String userID) {
		super();
		setUserID(userID);
	}
	
	/**
	 * Creates a new Teamspeak 2 user record as a copy of an existing user.
	 * @param usr the existing user
	 * @param serverID the new Server ID
	 */
	public Client(Client usr, int serverID) {
		super();
		_userID = usr._userID;
		_pwd = usr._pwd;
		_serverID = serverID;
		_serverAdmin = usr._serverAdmin;
		_createdOn = (Date) usr._createdOn.clone();
		_lastOnline = (usr._createdOn == null) ? null : (Date) usr._createdOn.clone();
	}

	/**
	 * Returns the user's TeamSpeak User ID.
	 * @return the user ID
	 * @see Client#setUserID(String)
	 */
	public String getUserID() {
		return _userID;
	}
	
	/**
	 * Returns the user's pilot code.
	 * @return the pilot code, or 0 if an internal account
	 */
	public int getPilotCode() {
		UserID id = new UserID(_userID);
		return id.getUserID();
	}
	
	/**
	 * Returns the user's password. This may not be populated.
	 * @return the password, or null if not populated
	 * @see Client#setPassword(String)
	 */
	public String getPassword() {
		return _pwd;
	}
	
	/**
	 * Returns the applicable TeamSpeak server ID. <i>This is usually 1</i>.
	 * @return the server ID
	 * @see Client#setServerID(int)
	 */
	public int getServerID() {
		return _serverID;
	}
	
	/**
	 * Returns wether the user is a Server Administrator.
	 * @return TRUE if the user is an Administrator, otherwise FALSE
	 * @see Client#setServerAdmin(boolean)
	 * @see Client#getServerOperator()
	 * @see Client#getAutoVoice()
	 */
	public boolean getServerAdmin() {
		return _serverAdmin;
	}
	
	/**
	 * Returns wether the user is a Server Operator.
	 * @return TRUE if the user is an Operator, otherwise FALSE
	 * @see Client#setServerOperator(boolean)
	 * @see Client#getServerAdmin()
	 * @see Client#getAutoVoice()
	 */
	public boolean getServerOperator() {
		return _serverOperator;
	}
	
	/**
	 * Returns wether the user has automatic voice permissions.
	 * @return TRUE if the user has voice permissions, otherwise FALSE
	 * @see Client#setAutoVoice(boolean)
	 * @see Client#getServerAdmin()
	 * @see Client#getServerOperator()
	 */
	public boolean getAutoVoice() {
		return _autoVoice;
	}
	
	/**
	 * Returns the date the user was created on.
	 * @return the creation date/time
	 * @see Client#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the date the user was last connected to TeamSpeak on.
	 * @return the last online date/time
	 * @see Client#setLastOnline(Date)
	 */
	public Date getLastOnline() {
		return _lastOnline;
	}

	/**
	 * Returns all Channels associated with this Client.
	 * @return a Collection of Channel database IDs
	 * @see Client#addChannelID(int)
	 * @see Client#addChannels(Server)
	 */
	public Collection<Integer> getChannelIDs() {
		return _channelIDs;
	}
	
	/**
	 * Returns wether the user is currently logged into ACARS.
	 * @return TRUE if the user is logged into ACARS, otherwise FALS
	 * @see Client#setIsACARS(boolean)
	 */
	public boolean getIsACARS() {
		return _isACARS;
	}
	
	/**
	 * Updates the TeamSpeak user ID.
	 * @param id the user ID
	 * @throws NullPointerException if id is null
	 * @see Client#setUserID(String)
	 */
	public void setUserID(String id) {
		_userID = id.trim().toUpperCase();
	}
	
	/**
	 * Updates the user's password.
	 * @param pwd the password
	 * @see Client#getPassword()
	 */
	public void setPassword(String pwd) {
		_pwd = pwd;
	}
	
	/**
	 * Sets the server database ID. <i>This is usually 1</i>.
	 * @param id the server database ID
	 * @throws IllegalArgumentException if id is negative
	 * @see Client#getServerID()
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
	 * @see Client#getServerAdmin()
	 * @see Client#setServerOperator(boolean)
	 * @see Client#setAutoVoice(boolean)
	 */
	public void setServerAdmin(boolean isAdmin) {
		_serverAdmin = isAdmin;
	}
	
	/**
	 * Updates wether the user is a Server Operator.
	 * @param isOperator TRUE if the user is an operator, otherwise FALSE
	 * @see Client#getServerOperator()
	 * @see Client#setServerAdmin(boolean)
	 * @see Client#setAutoVoice(boolean)
	 */
	public void setServerOperator(boolean isOperator) {
		_serverOperator = isOperator;
	}
	
	/**
	 * Updates wether the user is currently logged into ACARS.
	 * @param isLoggedIn TRUE if the user is logged in, otherwise FALSE
	 * @see Client#getIsACARS()
	 */
	public void setIsACARS(boolean isLoggedIn) {
		_isACARS = isLoggedIn;
	}
	
	/**
	 * Updates wether the user automatically receives voice permissions.
	 * @param autoVoice TRUE if automatic voice enabled, otherwise FALSE
	 * @see Client#getAutoVoice()
	 * @see Client#setServerAdmin(boolean)
	 * @see Client#setServerOperator(boolean)
	 */
	public void setAutoVoice(boolean autoVoice) {
		_autoVoice = autoVoice;
	}
	
	/**
	 * Updates the creation date of this Teamspeak user.
	 * @param dt the creation date/time
	 * @see Client#getCreatedOn()
	 * @see Client#setLastOnline(Date)
	 */
	public void setCreatedOn(Date dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the last online date of this Teamspeak user
	 * @param dt the last online date/time
	 * @throws IllegalArgumentException if dt is before CreatedOn
	 * @see Client#getLastOnline()
	 * @see Client#setCreatedOn(Date)
	 */
	public void setLastOnline(Date dt) {
		if ((dt != null) && dt.before(_createdOn))
			throw new IllegalArgumentException("Last Online cannot be before Created");
		
		_lastOnline = dt;
	}
	
	/**
	 * Adds a channel ID to this Client.
	 * @param id the channel database ID
	 * @see Client#addChannels(Server)
	 * @see Client#getChannelIDs()
	 */
	public void addChannelID(int id) {
		_channelIDs.add(new Integer(id));
	}
	
	/**
	 * Adds all Channels linked to a particular TeamSpeak 2 virtual server to this client.
	 * @param srv the Server bean
	 * @see Client#addChannelID(int)
	 * @see Client#getChannelIDs()
	 */
	public void addChannels(Server srv) {
		for (Iterator<Channel> i = srv.getChannels().iterator(); i.hasNext(); ) {
			Channel ch = i.next();
			_channelIDs.add(new Integer(ch.getID()));
		}
	}
	
	/**
	 * Comapres two users by comparing their TS2 user ID.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Client usr = (Client) o;
		return _userID.compareTo(usr._userID);
	}
	
	/**
	 * Returns the user's cache key.
	 * @return the user ID
	 */
	public Object cacheKey() {
		StringBuilder buf = new StringBuilder(String.valueOf(getID()));
		buf.append('-');
		buf.append(String.valueOf(_serverID));
		return buf.toString();
	}
}