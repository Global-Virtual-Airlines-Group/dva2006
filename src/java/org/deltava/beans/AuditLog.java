// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A bean to store an audit log record. 
 * @author Luke
 * @version 7.4
 * @since 7.4
 */

public class AuditLog implements AuditEntry, Comparable<AuditLog> {
	
	private final String _type;
	private final String _id;
	private int _authorID;
	private Instant _createdOn;
	
	private String _remoteAddr;
	private String _remoteHost;
	
	private String _desc;
	
	/**
	 * Creates the log entry.
	 * @param type the entry type
	 * @param id the entry ID
	 * @param authorID the author's database ID
	 */
	public AuditLog(String type, String id, int authorID) {
		super();
		_type = type;
		_id = id;
		setAuthorID(authorID);
	}
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}

	@Override
	public String getAuditType() {
		return _type;
	}

	@Override
	public String getAuditID() {
		return _id;
	}

	@Override
	public Instant getDate() {
		return _createdOn;
	}

	@Override
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the IP address for this audit entry.
	 * @return the address
	 */
	public String getRemoteAddr() {
		return _remoteAddr;
	}
	
	/**
	 * Returns the host name for this audit entry.
	 * @return the host name
	 */
	public String getRemoteHost() {
		return _remoteHost;
	}
	
	@Override
	public void setAuthorID(int id) {
		DatabaseBean.validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Updates the entry description.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates the entry creation date.
	 * @param i the date/time
	 */
	public void setDate(Instant i) {
		_createdOn = i;
	}
	
	/**
	 * Updates the IP address for this audit entry.
	 * @param addr the address
	 */
	public void setRemoteAddr(String addr) {
		_remoteAddr = addr;
	}
	
	/**
	 * Updates the host name for this audit entry.
	 * @param host the host name
	 */
	public void setRemoteHost(String host) {
		_remoteHost = host;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_type).append('-');
		buf.append(_id);
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AuditLog)) return false;
		AuditLog a = (AuditLog) o;
		return ((a.hashCode() == hashCode()) && (compareTo(a) == 0));
	}

	/**
	 * Compres two log entries by comparing their creation date/times.
	 */
	@Override
	public int compareTo(AuditLog ae2) {
		return _createdOn.compareTo(ae2._createdOn);
	}
}