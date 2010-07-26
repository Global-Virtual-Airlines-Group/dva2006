// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.Date;

/**
 * A bean to store accomplishment IDs and Dates. This is used to improve
 * cacheability over storing merely the Accomoplishment ID.  
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class DatedAccomplishmentID implements Comparable<DatedAccomplishmentID> {

	private int _id;
	private Date _dt;
	
	/**
	 * Creates the bean.
	 * @param dt the Accomplishment date/time
	 * @param id the Accomplishment database ID
	 */
	public DatedAccomplishmentID(Date dt, int id) {
		super();
		_id = Math.max(0, id);
		_dt = dt;
	}

	/**
	 * Returns the Accomplishment ID.
	 * @return the Accomplishment database ID
	 */
	public int getID() {
		return _id;
	}
	
	/**
	 * Returns the date the Accomplishment was achieved.
	 * @return the Accomplishment date/time
	 */
	public Date getDate() {
		return _dt;
	}
	
	public int compareTo(DatedAccomplishmentID id2) {
		int tmpResult = _dt.compareTo(id2._dt);
		return (tmpResult == 0) ? Integer.valueOf(_id).compareTo(Integer.valueOf(id2._id)) : tmpResult;
	}
}