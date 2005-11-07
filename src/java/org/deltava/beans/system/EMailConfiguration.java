// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.ViewEntry;

/**
 * A bean to store IMAP mailbox data. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EMailConfiguration extends DatabaseBean implements ViewEntry {

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
     * @see EMailConfiguration#setAddress(String)
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
	
    /**
     * Returns the mailbox quota.
     * @return the quota size in bytes
     * @see EMailConfiguration#setQuota(int)
     */
	public int getQuota() {
		return _quota;
	}
	
    /**
     * Returns the mailbox maildir directory.
     * @return the maildir path
     * @see EMailConfiguration#setMailDirectory(String)
     */
	public String getMailDirectory() {
		return _mailDir;
	}
	
    /**
     * Returns the mailbox password.
     * @return the password
     * @see EMailConfiguration#setPassword(String)
     */
	public String getPassword() {
		return _pwd;
	}
	
    /**
     * Returns if the mailbox is active.
     * @return TRUE if the mailbox is active, otherwise FALSE
     * @see EMailConfiguration#setActive(boolean)
     */
	public boolean getActive() {
		return _active;
	}
	
    /**
     * Returns all aliases for this mailbox.
     * @return a Collection of alias addresses
     * @see EMailConfiguration#setAliases(Collection)
     * @see EMailConfiguration#addAlias(String)
     */
	public Collection getAliases() {
		return _aliases;
	}
	
    /**
     * Adds a new alias to this mailbox.
     * @param alias the new address
     * @throws NullPointerException if alias is null
     * @see EMailConfiguration#getAliases()
     * @see EMailConfiguration#setAliases(Collection)
     */
	public void addAlias(String alias) {
		_aliases.add(alias.trim());
	}
    
    /**
     * Updates the aliases for this mailbox.
     * @param aliases a new Collection of alias addresses
     * @throws NullPointerException if any element is null
     * @see EMailConfiguration#addAlias(String)
     * @see EMailConfiguration#getAliases()
     */
    public void setAliases(Collection aliases) {
       _aliases.clear();
       for (Iterator i = aliases.iterator(); i.hasNext(); )
          addAlias((String) i.next());
    }
	
    /**
     * Updates the primary address for this mailbox.
     * @param addr the address
     * @throws NullPointerException if addr is null
     * @see EMailConfiguration#getAddress()
     */
	public void setAddress(String addr) {
		_addr = addr.trim();
	}
	
    /**
     * Updates the size quota for this mailbox.
     * @param quota the quota in bytes
     * @throws IllegalArgumentException if quota is negative
     * @see EMailConfiguration#getQuota()
     */
	public void setQuota(int quota) {
		if (quota < 0)
			throw new IllegalArgumentException("Invalid Mailbox quota - " + quota);
		
		_quota = quota;
	}
	
    /**
     * Updates the mailbox password.
     * @param pwd the new password
     * @see EMailConfiguration#getPassword()
     */
	public void setPassword(String pwd) {
		_pwd = pwd;
	}
	
    /**
     * Updates the mailbox directory.
     * @param path the mail directory
     * @throws NullPointerException if path is null
     * @see EMailConfiguration#getMailDirectory()
     */
	public void setMailDirectory(String path) {
		_mailDir = path;
		if (!_mailDir.endsWith("/"))
			_mailDir = _mailDir.concat("/");
	}
	
    /**
     * Marks this mailbox as active.
     * @param active TRUE if the mailbox is active, otherwise FALSE
     * @see EMailConfiguration#getActive()
     */
	public void setActive(boolean active) {
		_active = active;
	}
    
    /**
     * Returns the CSS row class name if in a view table.
     * @return the CSS class name
     */
    public String getRowClassName() {
       return _active ? null : "warn";
    }
}