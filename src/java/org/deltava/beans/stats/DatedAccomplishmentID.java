// Copyright 2010, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

/**
 * A bean to store accomplishment IDs and Dates. This is used to improve
 * cacheability over storing merely the Accomoplishment ID.  
 * @author Luke
 * @version 8.0
 * @since 3.2
 */

public class DatedAccomplishmentID implements java.io.Serializable, Comparable<DatedAccomplishmentID> {

	private final int _id;
	private final Instant _dt;
	
	/**
	 * Creates the bean.
	 * @param dt the Accomplishment date/time
	 * @param id the Accomplishment database ID
	 */
	public DatedAccomplishmentID(Instant dt, int id) {
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
	public Instant getDate() {
		return _dt;
	}
	
	@Override
	public int compareTo(DatedAccomplishmentID id2) {
		int tmpResult = _dt.compareTo(id2._dt);
		return (tmpResult == 0) ? Integer.compare(_id, id2._id) : tmpResult;
	}
}