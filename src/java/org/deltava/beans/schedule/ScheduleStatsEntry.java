// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean storing Flight Schedule departure/arrival statistcs. 
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class ScheduleStatsEntry implements Comparable<ScheduleStatsEntry> {

	private final int _hour;
	private int _ddLegs;
	private int _diLegs;
	private int _adLegs;
	private int _aiLegs;
	
	/**
	 * Creates the bean
	 * @param hour the hour of day
	 */
	public ScheduleStatsEntry(int hour) {
		super();
		_hour = hour;
	}

	/**
	 * Returns the hour of the day.
	 * @return the hour of day
	 */
	public int getHour() {
		return _hour;
	}
	
	/**
	 * Returns the number of domestic departure legs.
	 * @return the number of legs
	 */
	public int getDomesticDepartureLegs() {
		return _ddLegs;
	}

	/**
	 * Returns the number of international departure legs.
	 * @return the number of legs
	 */
	public int getInternationalDepartureLegs() {
		return _diLegs;
	}
	
	/**
	 * Returns the number of domestic arrival legs.
	 * @return the number of legs
	 */
	public int getDomesticArrivalLegs() {
		return _adLegs;
	}
	
	/**
	 * Returns the number of international arrival legs.
	 * @return the number of legs
	 */
	public int getInternationalArrivalLegs() {
		return _aiLegs;
	}
	
	/**
	 * Updates the number of departure flights.
	 * @param domestic the number of domestic legs
	 * @param intl the number of international legs
	 */
	public void setDepartureLegs(int domestic, int intl) {
		_ddLegs = Math.max(0, domestic);
		_diLegs = Math.max(0, intl);
	}
	
	/**
	 * Updates the number of arrival flights.
	 * @param domestic the number of domestic legs
	 * @param intl the number of international legs
	 */
	public void setArrivalLegs(int domestic, int intl) {
		_adLegs = Math.max(0, domestic);
		_aiLegs = Math.max(0, intl);
	}

	@Override
	public int compareTo(ScheduleStatsEntry sse2) {
		return Integer.compare(_hour, sse2._hour);
	}
}