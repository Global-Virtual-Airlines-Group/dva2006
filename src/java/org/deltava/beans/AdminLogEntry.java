// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A bean to track administrative actions. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class AdminLogEntry implements java.io.Serializable, AuditEntry, RemoteAddressBean {
	
	private final String _type;
	private final String _id;
	private final Instant _date;
	private int _userID;
	
	private String _remoteHost;
	private String _remoteAddr;
	
	/**
	 * Creates the log entry for a dataabase bean.
	 * @param db the DatabaseBean
	 */
	public AdminLogEntry(DatabaseBean db) {
		this(db.getClass().getSimpleName(), Instant.now(), db.getHexID());
	}

	/**
	 * Creates the log entry.
	 * @param type the operationg name
	 * @param dt the date/time the operation occured
	 * @param id the audit entry ID
	 */
	public AdminLogEntry(String type, Instant dt, String id) {
		super();
		_type = type;
		_date = dt;
		_id = id;
	}

	@Override
	public int getAuthorID() {
		return _userID;
	}

	@Override
	public void setAuthorID(int id) {
		DatabaseBean.validateID(_userID, id);
		_userID = id;
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
		return _date;
	}
	
	@Override
	public String getRemoteAddr() {
		return _remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return _remoteHost;
	}

	@Override
	public String getDescription() {
		return null; // NOT IMPLEMENTED
	}
	
	/**
	 * Updates the remote address for this log entry.
	 * @param addr the remote IP address
	 * @param host the remost host name
	 */
	public void setRemoteAddress(String addr, String host) {
		_remoteAddr = addr;
		_remoteHost = host;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_type);
		buf.append('-').append(_id);
		buf.append('-').append(_userID);
		buf.append('-').append(_date.toEpochMilli());
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}