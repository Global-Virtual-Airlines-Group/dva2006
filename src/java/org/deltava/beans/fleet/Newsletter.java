// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.util.Date;

/**
 * A bean to store Newsletter information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Newsletter extends LibraryEntry {
	
	private Date _d;
	private String _category;

	/**
	 * Creates a new Newsletter.
	 * @param fName the File name
	 */
	public Newsletter(String fName) {
		super(fName);
	}

	/**
	 * Returns the date this newsletter was published.
	 * @return the publishing date
	 * @see Newsletter#setDate(Date)
	 */
	public Date getDate() {
		return _d;
	}
	
	/**
	 * Returns the newsletter category.
	 * @return the category
	 * @see Newsletter#setCategory(String)
	 */
	public String getCategory() {
		return _category;
	}
	
	/**
	 * Updates the date the newsletter was published.
	 * @param dt the publish date/time
	 * @see Newsletter#getDate()
	 */
	public void setDate(Date dt) {
		_d = dt;
	}
	
	/**
	 * Updates the newsletter category.
	 * @param ct the category
	 * @see Newsletter#getCategory()
	 */
	public void setCategory(String ct) {
		_category = ct;
	}
}