// Copyright 2006, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.io.File;
import java.time.Instant;

/**
 * A bean to store Newsletter information.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class Newsletter extends LibraryEntry {
	
	private Instant _d;
	private String _category;

	/**
	 * Creates a new Newsletter.
	 * @param f the File
	 */
	public Newsletter(File f) {
		super(f);
	}

	/**
	 * Returns the date this newsletter was published.
	 * @return the publishing date
	 * @see Newsletter#setDate(Instant)
	 */
	public Instant getDate() {
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
	public void setDate(Instant dt) {
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