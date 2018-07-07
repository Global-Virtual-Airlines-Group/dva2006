// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.beans.Simulator;

/**
 * A bean to store Simulator version statistics entries. 
 * @author Luke
 * @version 8.3
 * @since 8.1
 */

public class SimStatsEntry extends LegHoursStatsEntry<Simulator> {

	/**
	 * Creates the bean.
	 * @param dt the date/time
	 */
	public SimStatsEntry(Instant dt) {
		super(dt);
	}
	
	/**
	 * Returns the maximum Simulator in this bean.
	 * @return the highest Simulator
	 */
	public Simulator getMaxSimulator() {
		return getMaxKey();
	}
	
	/**
	 * Sets simulator statistics.
	 * @param s the Simulator
	 * @param legs the number of legs
	 * @param hours the number of hours
	 */
	public void setSimulator(Simulator s, int legs, double hours) {
		set(s, legs, hours);
	}
}