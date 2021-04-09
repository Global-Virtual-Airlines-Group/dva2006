// Copyright 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.time.Instant;

import org.deltava.util.BeanUtils;
import org.deltava.util.system.SystemData;

/**
 * A bean to store an audit log record. 
 * @author Luke
 * @version 10.0
 * @since 7.4
 */

public class AuditLog implements AuditEntry, Comparable<AuditLog> {
	
	public static final String COMMON = "COMMON";
	
	private final String _type;
	private final String _id;
	private int _authorID;
	private Instant _createdOn;
	private String _app = COMMON;
	
	private String _remoteAddr;
	private String _remoteHost;
	
	private String _desc;
	
	/**
	 * Creates an audit log from a list of bean property changes.
	 * @param a the Auditable object
	 * @param delta the list of changes
	 * @param authorID the author's database ID
	 * @return an AuditLog, or null if no changes
	 */
	public static AuditLog create(Auditable a, Collection<BeanUtils.PropertyChange> delta, int authorID) {
		if (delta.isEmpty()) return null;
		AuditLog ae = new AuditLog(a, authorID);
		ae.setDate(Instant.now());
		if (!a.isCrossApp())
			ae.setApplication(SystemData.get("airline.code"));
		
		StringBuilder buf = new StringBuilder();
		for (Iterator<BeanUtils.PropertyChange> i = delta.iterator(); i.hasNext(); ) {
			BeanUtils.PropertyChange bc = i.next();
			buf.append(bc.toString());
			if (i.hasNext())
				buf.append('\n');
		}
		
		ae.setDescription(buf.toString());
		return ae;
	}
	
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
	
	/**
	 * Creates the log entry.
	 * @param a the Auditable object
	 * @param authorID the author's database ID
	 */
	public AuditLog(Auditable a, int authorID) {
		this (a.getAuditType(), a.getAuditID(), authorID);
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
	
	/**
	 * Returns the application name.
	 * @return the application name, or COMMON for shared objects
	 */
	public String getApplication() {
		return _app;
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
	 * Updates the application name.
	 * @param appName the name, or COMMON for shared objects
	 */
	public void setApplication(String appName) {
		_app = appName;
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

	@Override
	public int compareTo(AuditLog ae2) {
		return _createdOn.compareTo(ae2._createdOn);
	}
}