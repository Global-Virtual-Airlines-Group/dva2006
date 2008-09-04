// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.ViewEntry;

/**
 * A bean to store IMAP mailbox data. 
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class EMailConfiguration extends DatabaseBean implements ViewEntry {

	private String _addr;
	private String _mailDir;
	private int _quota;
	private boolean _active;
	
	private Set<String> _aliases = new TreeSet<String>();

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
	public Collection<String> getAliases() {
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
    public void setAliases(Collection<String> aliases) {
       _aliases.clear();
       _aliases.addAll(aliases);
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
     * @see EMailConfiguration#getQuota()
     */
	public void setQuota(int quota) {
		_quota = Math.max(0, quota);
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