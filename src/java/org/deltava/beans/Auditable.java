// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface for objects that can have audit logs. 
 * @author Luke
 * @version 7.4
 * @since 7.4
 */

public interface Auditable extends Cloneable {

	/**
	 * Returns the audit type for this auditable object.
	 * @return the type
	 */
	default String getAuditType() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Returns the object ID for this auditable object.
	 * @return the ID
	 */
	public String getAuditID();
}