// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to list valid ACARS client types. 
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public enum ClientType {
	PILOT, DISPATCH, ATC;
	
	/**
	 * Returns a formatted name.
	 * @return the name
	 */
	public String getName() {
		return (this == ATC) ? name() : name().substring(0, 1) + name().substring(1).toLowerCase(); 
	}
}