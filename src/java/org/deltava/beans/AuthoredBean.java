// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * A database bean with an author.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface AuthoredBean {

	/**
	 * Returns the Author of this bean.
	 * @return the author's database ID
	 * @see AuthoredBean#setAuthorID(int)
	 */
	public int getAuthorID();
	
	/**
	 * Updates the author of this bean.
	 * @param id the author's database ID.
	 * @throws IllegalArgumentException if id is negative
	 * @see AuthoredBean#getAuthorID()
	 */
	public void setAuthorID(int id);
}