// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store IMAP mailbox data. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class EMailConfiguration extends DatabaseBean {

	private String _addr;
	private String _pwd;
	private String _mailDir;
	private int _quota;
	private boolean _active;
	
	private Set _aliases = new TreeSet();

	/**
	 * Creates a new e-mail configuration bean.
	 * @param id the Pilot's database ID
	 * @param addr the primary e-mail address
	 * @throws NullPointerException if addr is null
	 */
	public EMailConfiguration(int id, String addr) {
		super();
		setID(id);
		setAddress(addr);
	}

	/**
	 * Returns the primary e-mail address.
	 * @return the address
	 * @see EMailConfiguration#setAddress(String)
	 */
	public String getAddress() {
		return _addr;
	}
	
	public int getQuota() {
		return _quota;
	}
	
	public String getMailDirectory() {
		return _mailDir;
	}
	
	public String getPassword() {
		return _pwd;
	}
	
	public boolean getActive() {
		return _active;
	}
	
	public Collection getAliases() {
		return _aliases;
	}
	
	public void addAlias(String alias) {
		_aliases.add(alias);
	}
	
	public void setAddress(String addr) {
		_addr = addr.trim();
	}
	
	public void setQuota(int quota) {
		if (quota < 0)
			throw new IllegalArgumentException("Invalid Mailbox quota - " + quota);
		
		_quota = quota;
	}
	
	public void setPassword(String pwd) {
		_pwd = pwd;
	}
	
	public void setMailDirectory(String path) {
		_mailDir = path;
		if (!_mailDir.endsWith("/"))
			_mailDir = _mailDir.concat("/");
	}
	
	public void setActive(boolean active) {
		_active = active;
	}
}