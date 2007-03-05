// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.net.InetAddress;

import org.deltava.beans.*;

/**
 * A bean used to block user names and IP addresses from registering.
 * @author Luke
 * @verison 1.0
 * @since 1.0
 */

public class RegistrationBlock extends DatabaseBean implements ViewEntry {

	private boolean _active;
	private boolean _userFeedback;
	
	private String _firstName;
	private String _lastName;
	
	private String _remoteHost;
	private int _remoteAddress;
	private int _netMask;
	
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
	 * @see RegistrationBlock#setAddress(int, int)
	 * @see RegistrationBlock#setAddress(int, int)
	 * @see RegistrationBlock#getNetMask()
	 */
	public int getAddress() {
		return _remoteAddress;
	}
	
	/**
	 * Returns the IP address netmask.
	 * @return the netmask
	 * @see RegistrationBlock#setAddress(int, int)
	 * @see RegistrationBlock#setAddress(String, String)
	 * @see RegistrationBlock#getAddress()
	 */
	public int getNetMask() {
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
	 * Updates the IP address and network mask.
	 * @param addr the IPv4 address
	 * @param mask the network mask
	 * @see RegistrationBlock#setAddress(String, String)
	 * @see RegistrationBlock#getAddress()
	 * @see RegistrationBlock#getNetMask()
	 */
	public void setAddress(int addr, int mask) {
		_remoteAddress = addr;
		_netMask = mask;
	}
	
	/**
	 * Updates the IP address and the network mask.
	 * @param address the IP address
	 * @param mask the network mask as either a bit count or an IP address
	 * @throws IllegalArgumentException if addr or mask cannot be parsed
	 * @see RegistrationBlock#setAddress(int, int)
	 * @see RegistrationBlock#getAddress()
	 * @see RegistrationBlock#getNetMask()
	 */
	public void setAddress(String address, String mask) {
		try {
			byte[] ip = InetAddress.getByName(address).getAddress();
			int addr = ip[3] + (ip[2] << 8) + (ip[3] << 16) + (ip[4] << 24);
			
			// Calculate the netmask
			int netMask = 0xFFFFFFFF;
			if (mask.indexOf('.') != -1) {
				byte[] msk = InetAddress.getByName(mask).getAddress();
				netMask = msk[3] + (msk[2] << 8) + (msk[3] << 16) + (msk[4] << 24);
			} else
				netMask -= (1 << (33 - Integer.parseInt(mask))) - 1;
			
			// Update the address
			setAddress(addr, netMask);
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
	
	/**
	 * Returns the CSS table row class used when displaying in an HTML table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _active ? null : "warn";
	}
}