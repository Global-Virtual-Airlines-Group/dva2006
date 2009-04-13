// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.*;

/**
 * A bean used to block user names and IP addresses from registering.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class RegistrationBlock extends DatabaseBean implements ViewEntry {

	private boolean _active;
	private boolean _userFeedback;
	
	private String _firstName;
	private String _lastName;
	
	private String _remoteHost;
	private long _remoteAddress;
	private long _netMask = 0xFFFFFF00;
	
	private String _comments;
	
	/**
	 * Initializes the bean.
	 * @param fName the first name
	 * @param lName the last name
	 * @see RegistrationBlock#setName(String, String)
	 * @see RegistrationBlock#getFirstName()
	 * @see RegistrationBlock#getLastName()
	 */
	public RegistrationBlock(String fName, String lName) {
		super();
		setName(fName, lName);
	}
	
	/**
	 * Returns wether the block is active.
	 * @return TRUE if the block is active, otherwise FALSE
	 * @see RegistrationBlock#setActive(boolean)
	 */
	public boolean getActive() {
		return _active;
	}
	
	/**
	 * Returns wether the user should be informed that the registration attempt has been blocked. This can be used to
	 * avoid providing feedback to spambots.
	 * @return TRUE if feedback should be provided, otherwise FALSE
	 * @see RegistrationBlock#setHasUserFeedback(boolean)
	 */
	public boolean getHasUserFeedback() {
		return _userFeedback;
	}
	
	/**
	 * Returns the first name. 
	 * @return the first name, or null if none
	 * @see RegistrationBlock#RegistrationBlock(String, String)
	 */
	public String getFirstName() {
		return _firstName;
	}
	
	/**
	 * Returns the last name. 
	 * @return the last name, or null if none
	 * @see RegistrationBlock#RegistrationBlock(String, String)
	 */
	public String getLastName() {
		return _lastName;
	}
	
	/**
	 * Returns the remote host name.
	 * @return the host name
	 * @see RegistrationBlock#setHostName(String)
	 */
	public String getHostName() {
		return _remoteHost;
	}
	
	/**
	 * Returns user comments about this Registration block.
	 * @return the comments
	 * @see RegistrationBlock#setComments(String)
	 */
	public String getComments() {
		return _comments;
	}
	
	/**
	 * Returns the IP address or network to block.
	 * @return the network address
	 * @see RegistrationBlock#setAddress(long)
	 * @see RegistrationBlock#getNetMask()
	 */
	public long getAddress() {
		return _remoteAddress;
	}
	
	/**
	 * Returns the IP address netmask.
	 * @return the netmask
	 * @see RegistrationBlock#setNetMask(long)
	 * @see RegistrationBlock#getAddress()
	 */
	public long getNetMask() {
		return _netMask;
	}
	
	/**
	 * Marks this registration block as active/inactive.
	 * @param isActive TRUE if the rule is active, otherwise FALSE
	 * @see RegistrationBlock#getActive()
	 */
	public void setActive(boolean isActive) {
		_active = isActive;
	}
	
	/**
	 * Marks wether the user should receive feedback if a registration attempt was rejected.
	 * @param hasFeedback TRUE wether feedback should be given on rejection, otherwise FALSE
	 * @see RegistrationBlock#getHasUserFeedback()
	 */
	public void setHasUserFeedback(boolean hasFeedback) {
		_userFeedback = hasFeedback;
	}
	
	/**
	 * Updates the name.
	 * @param fName the first name
	 * @param lName the last name
	 */
	public void setName(String fName, String lName) {
		_firstName = fName;
		_lastName = lName;
	}
	
	/**
	 * Updates the blocked host name.
	 * @param hostName the host name
	 * @see RegistrationBlock#getHostName()
	 */
	public void setHostName(String hostName) {
		_remoteHost = hostName;
	}
	
	/**
	 * Updates user comments about this Registration block entry.
	 * @param comments the comments
	 * @see RegistrationBlock#getComments()
	 */
	public void setComments(String comments) {
		_comments = comments;
	}

	/**
	 * Updates the IP address.
	 * @param addr the IPv4 address
	 * @see RegistrationBlock#getAddress()
	 * @see RegistrationBlock#setNetMask(long)
	 */

	public void setAddress(long addr) {
		_remoteAddress = addr & _netMask;
	}

	/**
	 * Updates the IP address and network mask.
	 * @param mask the network mask
	 * @see RegistrationBlock#getNetMask()
	 * @see RegistrationBlock#setAddress(long)
	 */
	public void setNetMask(long mask) {
		_netMask = mask;
	}
	
	/**
	 * Returns the CSS table row class used when displaying in an HTML table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _active ? null : "warn";
	}
}