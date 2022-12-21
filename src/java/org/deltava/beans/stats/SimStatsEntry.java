// Copyright 2017, 2018, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.beans.Simulator;

/**
 * A bean to store Simulator version statistics entries. 
 * @author Luke
 * @version 10.3
 * @since 8.1
 */

public class SimStatsEntry extends LegHoursDateStatsEntry<Simulator> {

	/**
	 * Creates the bean.
	 * @param dt the date/time
	 */
	public SimStatsEntry(Instant dt) {
		super(dt);
	}
	
	/**
	 * Sets simulator statistics.
	 * @param s the Simulator
	 * @param legs the number of legs
	 * @param distance the flight distance in miles
	 * @param hours the number of hours
	 */
	public void setSimulator(Simulator s, int legs, int distance, double hours) {
		set(s, legs, distance, hours);
	}
}