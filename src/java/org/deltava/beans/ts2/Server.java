// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts2;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store TeamSpeak 2 server information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Server extends DatabaseBean implements ComboAlias, ViewEntry, Comparable {

	private String _name;
	private String _msg;
	private String _desc;
	private String _pwd;
	private int _maxUsers;
	private int _port;
	private boolean _active;
	private Date _createdOn;
	
	private Collection<String> _roles = new TreeSet<String>();

	/**
	 * Creates a new server bean.
	 * @param name the server name
	 * @throws NullPointerException if name is null
	 */
	public Server(String name) {
		super();
		setName(name);
		_createdOn = new Date();
	}

	public String getComboName() {
		return _name;
	}

	public String getComboAlias() {
		return StringUtils.formatHex(getID());
	}

	/**
	 * Returns the server name.
	 * @return the name
	 * @see Server#setName(String)
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the server description.
	 * @return the description
	 * @see Server#setDescription(String)
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the server welcome message.
	 * @return the message
	 * @see Server#setWelcomeMessage(String)
	 */
	public String getWelcomeMessage() {
		return _msg;
	}
	
	/**
	 * Returns the server password.
	 * @return the password
	 * @see Server#setPassword(String)
	 */
	public String getPassword() {
		return _pwd;
	}

	/**
	 * Returns the maximum number of active users.
	 * @return the number of active users
	 * @see Server#setMaxUsers(int)
	 */
	public int getMaxUsers() {
		return _maxUsers;
	}

	/**
	 * Returns the UDP port for this server.
	 * @return the UDP port number
	 * @see Server#setPort(int)
	 */
	public int getPort() {
		return _port;
	}

	/**
	 * Returns wether the server is active.
	 * @return TRUE if the server is active, otherwise FALSE
	 * @see Server#setActive(boolean)
	 */
	public boolean getActive() {
		return _active;
	}

	/**
	 * Returns the date this Server entry was created on.
	 * @return the date/time the server was created
	 * @see Server#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the security roles authorized to access this server.
	 * @return a Collection of role names
	 * @see Server#addRole(String)
	 * @see Server#setRoles(Collection)
	 */
	public Collection<String> getRoles() {
		return _roles;
	}
	
	/**
	 * Adds a security role name authorized to access this server.
	 * @param role the role name
	 * @throws NullPointerException if role is null
	 * @see Server#setRoles(Collection)
	 * @see Server#getRoles()
	 */
	public void addRole(String role) {
		_roles.add(role);
	}
	
	/**
	 * Overwrites and updates the security roles authorized to access this server.
	 * @param roles a Collection of role names
	 * @see Server#addRole(String)
	 * @see Server#getRoles()
	 */
	public void setRoles(Collection<String> roles) {
		_roles.clear();
		_roles.addAll(roles);
	}

	/**
	 * Updates the server name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see Server#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}

	/**
	 * Updates the server description.
	 * @param desc the description
	 * @see Server#getDescription()
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates the server welcome message.
	 * @param msg the welcome message
	 * @see Server#getWelcomeMessage()
	 */
	public void setWelcomeMessage(String msg) {
		_msg = msg;
	}
	
	/**
	 * Sets the server password.
	 * @param pwd the password
	 * @see Server#getPassword()
	 */
	public void setPassword(String pwd) {
		_pwd = pwd;
	}

	/**
	 * Updates the maximum connected user count for this Server.
	 * @param users the maximum number of users
	 * @throws IllegalArgumentException if users is zero, negative or &gt; 32000
	 * @see Server#getMaxUsers()
	 */
	public void setMaxUsers(int users) {
		if ((users < 1) || (users > 32000))
			throw new IllegalArgumentException("Invalid maxUser count - " + users);

		_maxUsers = users;
	}
	
	/**
	 * Updates the UDP port number for this server.
	 * @param port the UDP port number
	 * @see Server#getPort()
	 */
	public void setPort(int port) {
		if ((port < 1) || (port > 65530))
			throw new IllegalArgumentException("Invalid Port - " + port);
		
		_port = port;
	}

	/**
	 * Updates the creation date of this Server record.
	 * @param dt the date/time the server record was created
	 * @see Server#getCreatedOn()
	 */
	public void setCreatedOn(Date dt) {
		_createdOn = dt;
	}
	
	/**
	 * Marks this server as active.
	 * @param isActive TRUE if the server is active, otherwise FALSE
	 * @see Server#getActive()
	 */
	public void setActive(boolean isActive) {
		_active = isActive;
	}
	
	/**
	 * Returns the CSS class name when rendered in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _active ? null : "warn";
	}
	
	/**
	 * Comapres two servers by comparing their names and ports.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Server s2 = (Server) o;
		int tmpResult = _name.compareTo(s2._name);
		return (tmpResult == 0) ? new Integer(_port).compareTo(new Integer(s2._port)) : tmpResult;
	}
}