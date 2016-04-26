// Copyright 2006, 2007, 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts2;

import org.deltava.beans.*;

/**
 * A bean to store TeamSpeak 2 voice channel information.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class Channel extends TSObject implements ViewEntry {
	
	private String _name;
	private String _topic;
	private String _desc;
	private String _pwd;
	
	private int _serverID;
	private Codec _codec;
	private int _maxUsers;
	
	private boolean _isModerated;
	private boolean _isHierarchical;
	private boolean _isDefault;
	
	/**
	 * Creates a new Channel bean.
	 * @param name the channel name
	 * @throws NullPointerException if name is null
	 * @see Channel#setName(String) 
	 */
	public Channel(String name) {
		super();
		setName(name);
	}
	
	/**
	 * Returns the Channel name.
	 * @return the name
	 * @see Channel#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the Channel topic.
	 * @return the topic
	 * @see Channel#setTopic(String)
	 */
	public String getTopic() {
		return _topic;
	}
	
	/**
	 * Returns the Channel description.
	 * @return the description
	 * @see Channel#setDescription(String)
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the Channel password.
	 * @return the password
	 * @see Channel#setPassword(String)
	 */
	public String getPassword() {
		return _pwd;
	}
	
	/**
	 * Returns the Channel codec.
	 * @return the Codec
	 * @see Channel#setCodec(Codec)
	 */
	public Codec getCodec() {
		return _codec;
	}
	
	/**
	 * Returns the maximum number of users in the Channel.
	 * @return the maximum number of users
	 * @see Channel#setMaxUsers(int)
	 */
	public int getMaxUsers() {
		return _maxUsers;
	}
	
	/**
	 * Returns the TeamSpeak server's database ID.
	 * @return the database ID
	 * @see Channel#setServerID(int)
	 * @see Client#getServerID()
	 */
	public int getServerID() {
		return _serverID;
	}
	
	/**
	 * Returns if this is the default Channel.
	 * @return TRUE if the default Channel, otherwise FALSE
	 * @see Channel#setDefault(boolean)
	 */
	public boolean getDefault() {
		return _isDefault;
	}
	
	/**
	 * Returns if this Channel is moderated.
	 * @return TRUE if the Channel is moderated, otherwise FALSE
	 * @see Channel#setModerated(boolean)
	 */
	public boolean getModerated() {
		return _isModerated;
	}
	
	/**
	 * Returns if the Channel is hierarchical.
	 * @return TRUE if the Channel is hierarchical, otherwise FALSE
	 * @see Channel#setHierarchical(boolean)
	 */
	public boolean getHierarchical() {
		return _isHierarchical;
	}
	
	/**
	 * Updates the Channel name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see Channel#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the Channel topic.
	 * @param topic the topic
	 * @throws NullPointerException if topic is null
	 * @see Channel#getTopic()
	 */
	public void setTopic(String topic) {
		_topic = topic.trim();
	}
	
	/**
	 * Updates the Channel description.
	 * @param desc the description
	 * @see Channel#getDescription()
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates the Channel password.
	 * @param pwd the password
	 * @see Channel#getPassword()
	 */
	public void setPassword(String pwd) {
		_pwd = pwd;
	}
	
	/**
	 * Updates the TeamSpeak server's database ID.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is negative 
	 * @see Channel#getServerID()
	 * @see DatabaseBean#validateID(int, int)
	 */
	public void setServerID(int id) {
		if (id != 0) {
			validateID(_serverID, id);
			_serverID = id;
		}
	}
	
	/**
	 * Updates the maximum connected user count for this Channel.
	 * @param users the maximum number of users
	 * @throws IllegalArgumentException if users is zero, negative or &gt; 32000
	 * @see Channel#getMaxUsers()
	 */
	public void setMaxUsers(int users) {
		if (users > 32000)
			throw new IllegalArgumentException("Invalid maxUser count - " + users);
		
		_maxUsers = Math.max(1, users);
	}
	
	/**
	 * Updates the Channel's codec.
	 * @param c the Codec
	 * @see Channel#getCodec()
	 */
	public void setCodec(Codec c) {
		_codec = c;
	}
	
	/**
	 * Updates whether this Channel is the default channel. 
	 * @param isDefault TRUE if this is the default channel, otherwise FALSE
	 * @see Channel#getDefault()
	 */
	public void setDefault(boolean isDefault) {
		_isDefault = isDefault;
	}
	
	/**
	 * Updates whether the Channel is hierarchical.
	 * @param isHierarchical TRUE if the channel is hierarchical, otherwise FALSE
	 * @see Channel#getHierarchical()
	 */
	public void setHierarchical(boolean isHierarchical) {
		_isHierarchical = isHierarchical;
	}
	
	/**
	 * Updates whether the Channel is moderated.
	 * @param isModerated TRUE if the Channel is moderated, otherwise FALSE
	 * @see Channel#getModerated()
	 */
	public void setModerated(boolean isModerated) {
		_isModerated = isModerated;
	}
	
	/**
	 * Returns the Channel's cache key.
	 * @return CH- and the dataabase ID
	 */
	@Override
	public Object cacheKey() {
		StringBuilder buf = new StringBuilder("CH-");
		buf.append(String.valueOf(getID()));
		return buf.toString();
	}
	
	/**
	 * Returns the CSS class name when rendered in a view table.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		return _isDefault ? "opt2" : null;
	}
}