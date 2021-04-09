// Copyright 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface for objects that can have audit logs. 
 * @author Luke
 * @version 10.0
 * @since 7.4
 */

public interface Auditable extends Cloneable, java.io.Serializable {

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
	
	/**
	 * Returns whether this object is within multiple web applications. Objects where this is false will need additional data appended to the audit ID to prevent information leakage
	 * across applications where the audit IDs are identical between apps. 
	 * @return TRUE if cross-application, otherwise FALSE
	 */
	default boolean isCrossApp() {
		return true;
	}
}