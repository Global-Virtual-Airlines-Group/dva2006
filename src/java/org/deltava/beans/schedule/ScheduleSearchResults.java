// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

/**
 * A bean to store Flight Schedule results. 
 * @author Luke
 * @version 5.1
 * @since 5.1
 */
 
public class ScheduleSearchResults extends ArrayList<ScheduleEntry> {

	private Date _importDate;
	
	/**
	 * Initializes the bean.
	 * @param results the search results
	 */
	public ScheduleSearchResults(Collection<ScheduleEntry> results) {
		super(results);
	}

	/**
	 * Returns the last schedule import date.
	 * @return the date/time of the last import, or null if none
	 */
	public Date getImportDate() {
		return _importDate;
	}
	
	/**
	 * Updates the last schedule import date.
	 * @param dt the date/time of the last import
	 */
	public void setImportDate(Date dt) {
		_importDate = dt;
	}
}