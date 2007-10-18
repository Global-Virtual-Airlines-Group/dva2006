// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
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

public class Server extends DatabaseBean implements ComboAlias, ViewEntry {

	public static final String ACCESS = "access";
	public static final String ADMIN = "admin";
	public static final String OPERATOR = "op";
	public static final String VOICE = "voice";

	private String _name;
	private String _msg;
	private String _desc;
	private String _pwd;
	private int _maxUsers;
	private int _port;
	private Date _createdOn;

	private boolean _active;
	private boolean _acarsOnly;
	private boolean _autoVoice;

	private Map<String, Collection<String>> _roles = new HashMap<String, Collection<String>>();
	private Collection<Channel> _channels = new TreeSet<Channel>();

	/**
	 * Creates a new server bean.
	 * @param name the server name
	 * @throws NullPointerException if name is null
	 */
	public Server(String name) {
		super();
		setName(name);
		_createdOn = new Date();

		// Build the role collections
		_roles.put(ACCESS, new TreeSet<String>());
		_roles.put(ADMIN, new TreeSet<String>());
		_roles.put(OPERATOR, new TreeSet<String>());
		_roles.put(VOICE, new TreeSet<String>());
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
	 * Returns if the Server can only be accessed when logged into ACARS.
	 * @return TRUE if the Server is accessible to ACARS-only users, otherwise FALSE
	 * @see Server#setACARSOnly(boolean)
	 */
	public boolean getACARSOnly() {
		return _acarsOnly;
	}

	/**
	 * Returns if users automatically receive voice privileges.
	 * @return TRUE if users can use voice, otherwise FALSE
	 * @see Server#setAutoVoice(boolean)
	 */
	public boolean getAutoVoice() {
		return _autoVoice;
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
	 * Returns the security roles authorized to access this virtual server.
	 * @return a Map of role types and role names
	 * @see Server#addRole(String, String)
	 * @see Server#setRoles(String, Collection)
	 */
	public Map<String, Collection<String>> getRoles() {
		return new LinkedHashMap<String, Collection<String>>(_roles);
	}
	
	/**
	 * Returns all Channel entries associated with this virtual server.
	 * @return a Collection of Channel beans
	 * @see Server#addChannel(Channel)
	 */
	public Collection<Channel> getChannels() {
		return _channels;
	}
	
	/**
	 * Adds a Channel entry to this virtual server.
	 * @param c a Channel bean
	 * @see Server#getChannels()
	 */
	public void addChannel(Channel c) {
		_channels.add(c);
	}

	/**
	 * Adds a security role name authorized to access or operate this virtual server.
	 * @param type the role type
	 * @param role the role name
	 * @throws NullPointerException if role or type are null
	 * @throws IllegalArgumentException if type is invalid
	 * @see Server#setRoles(String, Collection)
	 * @see Server#getRoles()
	 */
	public void addRole(String type, String role) {
		Collection<String> roles = _roles.get(type);
		if (roles == null)
			throw new IllegalArgumentException("Invalid role type - " + type);

		roles.add(role);

		// Add role to access list
		if (!ACCESS.equals(type))
			addRole(ACCESS, role);
	}

	/**
	 * Overwrites and updates the security roles authorized to access or operate this virtual server.
	 * @param type the role type
	 * @param newRoles a Collection of role names
	 * @throws NullPointerException if type is null
	 * @throws IllegalArgumentException if type is invalid
	 * @see Server#addRole(String, String)
	 * @see Server#getRoles()
	 */
	public void setRoles(String type, Collection<String> newRoles) {
		Collection<String> roles = _roles.get(type);
		if (roles == null)
			throw new IllegalArgumentException("Invalid role type - " + type);

		roles.clear();
		roles.addAll(newRoles);

		// Add role to access list
		if (!ACCESS.equals(type)) {
			for (Iterator<String> i = newRoles.iterator(); i.hasNext();) {
				String role = i.next();
				addRole(ACCESS, role);
			}
		}
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
	 * Updates wether the Server is only available to logged in ACARS users.
	 * @param isACARSOnly TRUE if the Server is accessible to ACARS-only users, otherwise FALSE
	 * @see Server#getACARSOnly()
	 */
	public void setACARSOnly(boolean isACARSOnly) {
		_acarsOnly = isACARSOnly;
	}

	/**
	 * Sets wether users to this Server automatically have voice privileges.
	 * @param autoVoice TRUE if voice privilegese automatically granted, otherwise FALSE
	 * @see Server#getAutoVoice()
	 */
	public void setAutoVoice(boolean autoVoice) {
		_autoVoice = autoVoice;
	}

	/**
	 * Returns the CSS class name when rendered in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		if (!_active)
			return "warn";
		
		return _acarsOnly ? "opt2" : null;
	}

	/**
	 * Comapres two servers by comparing their names and ports.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Server s2 = (Server) o;
		int tmpResult = _name.compareTo(s2._name);
		return (tmpResult == 0) ? Integer.valueOf(_port).compareTo(Integer.valueOf(s2._port)) : tmpResult;
	}

	/**
	 * Returns the Server name.
	 * @return the name
	 */
	public String toString() {
		return _name;
	}
}