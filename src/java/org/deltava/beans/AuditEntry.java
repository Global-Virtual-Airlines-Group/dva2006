// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to store audit log entries. 
 * @author Luke
 * @version 7.4
 * @since 7.4
 */

public interface AuditEntry extends AuthoredBean {
	
	/**
	 * Returns the audit object type.
	 * @return the type
	 */
	public String getAuditType();
	
	/**
	 * Returns the audit object ID.
	 * @return the ID
	 */
	public String getAuditID();

	/**
	 * Returns the date the entry was created.
	 * @return the creation date/time
	 */
	public java.time.Instant getDate();
	
	/**
	 * Returns the entry description.
	 * @return the description
	 */
	public String getDescription();
}