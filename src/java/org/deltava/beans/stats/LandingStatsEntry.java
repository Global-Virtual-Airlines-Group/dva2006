// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.beans.flight.LandingRating;

/**
 * A bean to store Landing Rating statistics entries.
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

public class LandingStatsEntry extends LegHoursDateStatsEntry<LandingRating> {

	/**
	 * Creates the bean.
	 * @param dt the date/time
	 */
	public LandingStatsEntry(Instant dt) {
		super(dt);
	}

	/**
	 * Adds a landing to the statistics entry
	 * @param score the landing score
	 * @param distance the flight distance in miles
	 * @param hours the flight hours
	 */
	public void add(int score, int distance, double hours) {
		inc(LandingRating.rate(score), distance, hours);
	}
}