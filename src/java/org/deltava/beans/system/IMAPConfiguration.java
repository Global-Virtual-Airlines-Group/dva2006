// Copyright 2005, 2006, 2008, 2010, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store IMAP mailbox data. 
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class IMAPConfiguration extends DatabaseBean implements Auditable, ViewEntry {

	private String _addr;
	private String _mailDir;
	private int _quota;
	private boolean _active;
	private boolean _allowSMTP;
	
	private final Collection<String> _aliases = new TreeSet<String>();

	/**
	 * Creates a new e-mail configuration bean.
	 * @param id the Pilot's database ID
	 * @param addr the primary e-mail address
	 * @throws NullPointerException if addr is null
     * @see IMAPConfiguration#setAddress(String)
	 */
	public IMAPConfiguration(int id, String addr) {
		super();
		setID(id);
		setAddress(addr);
	}

	/**
	 * Returns the primary e-mail address.
	 * @return the address
	 * @see IMAPConfiguration#setAddress(String)
	 */
	public String getAddress() {
		return _addr;
	}
	
    /**
     * Returns the mailbox quota.
     * @return the quota size in bytes
     * @see IMAPConfiguration#setQuota(int)
     */
	public int getQuota() {
		return _quota;
	}
	
    /**
     * Returns the mailbox maildir directory.
     * @return the maildir path
     * @see IMAPConfiguration#setMailDirectory(String)
     */
	public String getMailDirectory() {
		return _mailDir;
	}
	
    /**
     * Returns if the mailbox is active.
     * @return TRUE if the mailbox is active, otherwise FALSE
     * @see IMAPConfiguration#setActive(boolean)
     */
	public boolean getActive() {
		return _active;
	}
	
	/**
	 * Returns if this mailbox allows direct SMTP connetions.
	 * @return TRUE if SMTP connections allowed, otherwise FALSE
	 * @see IMAPConfiguration#setAllowSMTP(boolean)
	 */
	public boolean getAllowSMTP() {
		return _allowSMTP;
	}
	
    /**
     * Returns all aliases for this mailbox.
     * @return a Collection of alias addresses
     * @see IMAPConfiguration#setAliases(Collection)
     * @see IMAPConfiguration#addAlias(String)
     */
	public Collection<String> getAliases() {
		return _aliases;
	}
	
    /**
     * Adds a new alias to this mailbox.
     * @param alias the new address
     * @throws NullPointerException if alias is null
     * @see IMAPConfiguration#getAliases()
     * @see IMAPConfiguration#setAliases(Collection)
     */
	public void addAlias(String alias) {
		if (!StringUtils.isEmpty(alias)) {
			String tmp = alias.trim().replace("\n", "");
			_aliases.add(tmp.replace("\r", ""));
		}
	}
    
    /**
     * Updates the aliases for this mailbox.
     * @param aliases a new Collection of alias addresses
     * @throws NullPointerException if any element is null
     * @see IMAPConfiguration#addAlias(String)
     * @see IMAPConfiguration#getAliases()
     */
    public void setAliases(Collection<String> aliases) {
       _aliases.clear();
       for (String a : aliases)
    	   addAlias(a);
    }
	
    /**
     * Updates the primary address for this mailbox.
     * @param addr the address
     * @throws NullPointerException if addr is null
     * @see IMAPConfiguration#getAddress()
     */
	public void setAddress(String addr) {
		_addr = addr.trim();
	}
	
    /**
     * Updates the size quota for this mailbox.
     * @param quota the quota in bytes
     * @see IMAPConfiguration#getQuota()
     */
	public void setQuota(int quota) {
		_quota = Math.max(0, quota);
	}
	
    /**
     * Updates the mailbox directory.
     * @param path the mail directory
     * @throws NullPointerException if path is null
     * @see IMAPConfiguration#getMailDirectory()
     */
	public void setMailDirectory(String path) {
		_mailDir = path;
		if (!_mailDir.endsWith("/"))
			_mailDir = _mailDir.concat("/");
	}
	
    /**
     * Marks this mailbox as active.
     * @param active TRUE if the mailbox is active, otherwise FALSE
     * @see IMAPConfiguration#getActive()
     */
	public void setActive(boolean active) {
		_active = active;
	}
	
	/**
	 * Marks this mailbox as allowing direct SMTP access.
	 * @param allowSMTP TRUE if direct SMTP connections allowed, otherwise FALSE
	 * @see IMAPConfiguration#getAllowSMTP() 
	 */
	public void setAllowSMTP(boolean allowSMTP) {
		_allowSMTP = allowSMTP;
	}
    
	@Override
    public String getRowClassName() {
       return _active ? null : "warn";
    }

	@Override
	public String getAuditID() {
		return getHexID();
	}
}