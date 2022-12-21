// Copyright 2017, 2018, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

/**
 * A bean to store Equipment Stage statistics entries. 
 * @author Luke
 * @version 10.3
 * @since 8.1
 */

public class StageStatsEntry extends LegHoursDateStatsEntry<Integer> {

	/**
	 * Creates the bean.
	 * @param dt the date/time
	 */
	public StageStatsEntry(Instant dt) {
		super(dt);
	}
	
	/**
	 * Returns the number of flight legs for a particular stage.
	 * @param stage the stage number
	 * @return the number of legs
	 */
	public int getLegs(int stage) {
		return getLegs(Integer.valueOf(stage));
	}
	
	/**
	 * Returns the flight distance for a particular stage.
	 * @param stage the stage number
	 * @return the distance in miles
	 */
	public int getDistance(int stage) {
		return getDistance(Integer.valueOf(stage));
	}
	
	/**
	 * Returns the number of flight hours for a particular stage.
	 * @param stage the stage number
	 * @return the number of hours
	 */
	public double getHours(int stage) {
		return getHours(Integer.valueOf(stage));
	}
	
	/**
	 * Returns the maximum stage number in this bean.
	 * @return the highest stage
	 */
	public int getMaxStage() {
		return getMaxKey().intValue();
	}
	
	/**
	 * Sets stage statistics.
	 * @param stage the stage number
	 * @param legs the number of legs
	 * @param distance the flight distance in miles
	 * @param hours the number of hours
	 */
	public void setStage(int stage, int legs, int distance, double hours) {
		set(Integer.valueOf(stage), legs, distance, hours);
	}
}