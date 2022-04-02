// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A bean to store date-ordered statistics entries that contain key value plus hours/legs. 
 * @author Luke
 * @version 10.2
 * @since 10.2
 * @param <K> The sort key
 */

abstract class LegHoursDateStatsEntry<K extends Comparable<K>> extends LegHoursStatsEntry<K> {
	
	private final Instant _dt;

	/**
	 * Creates the bean.
	 * @param dt the date/time
	 */
	protected LegHoursDateStatsEntry(Instant dt) {
		super();
		_dt = dt.truncatedTo(ChronoUnit.DAYS);
	}
	
	/**
	 * Returns the date.
	 * @return the date
	 */
	public Instant getDate() {
		return _dt;
	}
	
	@Override
	public int hashCode() {
		return _dt.hashCode();
	}
}