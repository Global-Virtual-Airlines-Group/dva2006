// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to annoate objects with an ID, but who do not wish to extend {@link DatabaseBean}. 
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public interface IDBean {

	/**
	 * Returns the database ID of this object.
	 * @return the database ID
	 */
	public int getID();
	
	/**
	 * Returns the hexadecimal database ID of this object.
	 * @return the hexadecimal formatted database ID, or an empty string if zero
	 */
	public String getHexID();
}