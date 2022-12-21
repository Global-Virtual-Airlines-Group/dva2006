// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.beans.flight.OnTime;

/**
 * A bean to provide on-time statistics by date.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class OnTimeStatsEntry extends LegHoursDateStatsEntry<OnTime> {

	/**
	 * Creates the bean.
	 * @param dt the date
	 */
	public OnTimeStatsEntry(Instant dt) {
		super(dt);
	}
	
	/**
	 * Returns the total number of flight legs during this period.
	 * @return the number of legs
	 */
	public int getTotalLegs() {
		return getKeys().stream().mapToInt(this::getLegs).sum();
	}
	
	/**
	 * Returns the total number of on-time flight legs during this period.
	 * @return the number of legs
	 */
	public int getOnTimeLegs() {
		return getKeys().stream().filter(ot -> ((ot == OnTime.ONTIME) || (ot == OnTime.EARLY))).mapToInt(this::getLegs).sum();
	}
	
	/**
	 * Adds a statistics entry.
	 * @param ot the OnTime entry
	 * @param legs the number of legs
	 * @param distance the flight distance in miles
	 * @param hours the number of hours
	 */
	@Override
	public void set(OnTime ot, int legs, int distance, double hours) {
		super.set(ot, legs, distance, hours);
	}
}