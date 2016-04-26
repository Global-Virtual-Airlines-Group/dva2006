// Copyright 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * A bean to store information about simulated economic cycles.
 * @author Luke
 * @version 7.0
 * @since 3.7
 */

public class EconomyInfo implements java.io.Serializable {
	
	private double _targetLoad;
	private double _minLoad = 0.125d;
	private double _amp;
	
	private double _hourlyFactor = .1d;
	private double _yearHZ;
	private double _hourHZ;
	
	private Instant _startDate = ZonedDateTime.of(2001, 6, 10, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

	/**
	 * Initializes the economy. The amplitude cannot exceed one half of the difference
	 * between the target load and either 1 or 0.
	 * @param target the target load factor between 0 and 1
	 * @param amp the target amplitude
	 */
	public EconomyInfo(double target, double amp) {
		super();
		_targetLoad = Math.max(.01, Math.min(99.999d, target));
		double maxAmp = Math.min(_targetLoad, (1 - _targetLoad));
		_amp = Math.min(amp, (maxAmp / 2));
	}

	/**
	 * Returns the airline-wide target load.
	 * @return the target load factor between 0 and 1
	 */
	public double getTargetLoad() {
		return _targetLoad;
	}
	
	/**
	 * Returns the minimum load factor for a flight.
	 * @return the load factor between 0 and 1
	 */
	public double getMinimumLoad() {
		return _minLoad;
	}
	
	/**
	 * Returns the target load amplitude.
	 * @return the amplitude
	 */
	public double getAmplitude() {
		return _amp;
	}
	
	/**
	 * Sets the weighting of the hourly wave versus the daily wave.
	 * @return the factor to apply from 0 to 1
	 */
	public double getHourlyFactor() {
		return _hourlyFactor;
	}
	
	/**
	 * Returns the airline start date.
	 * @return the start date
	 */
	public Instant getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the frequency of the yearly oscilation.
	 * @return the number of cycles per year
	 */
	public double getCyclesPerYear() {
		return _yearHZ;
	}
	
	/**
	 * Returns the frequency of the hourly oscilation.
	 * @return the number of cycles per hour
	 */
	public double getCyclesPerHour() {
		return _hourHZ;
	}
	
	/**
	 * Updates the airline start date. The time component will be cleared.
	 * @param dt the start date/time
	 */
	public void setStartDate(Instant dt) {
		_startDate = dt.truncatedTo(ChronoUnit.DAYS);
	}

	/**
	 * Updates the minimum load factor for a flight.
	 * @param load the load factor between 0 and 1
	 */
	public void setMinimumLoad(double load) {
		_minLoad = Math.max(0, Math.min(_targetLoad, load));
	}
	
	/**
	 * Sets the weighting of the hourly wave versus the daily wave.
	 * @param f the factor to apply from 0 to 1
	 */
	public void setHourlyFactor(double f) {
		_hourlyFactor = Math.max(0d, Math.min(1d, f));
	}
	
	/**
	 * Sets the frequency of the yearly oscilation.
	 * @param hz the number of cycles per year
	 */
	public void setCyclesPerYear(double hz) {
		_yearHZ = Math.max(.000001d, hz);
	}
	
	/**
	 * Sets the yearly cycle length.
	 * @param days the number of days per cycle
	 */
	public void setYearlyCycleLength(int days) {
		setCyclesPerYear(365d / Math.max(1, days));
	}

	/**
	 * Sets the frequency of the hourly oscilation.
	 * @param hz the number of cycles per hour
	 */
	public void setCyclesPerHour(double hz) {
		_hourHZ = Math.max(.000001d, hz);
	}

	/**
	 * Sets the hourly cycle length.
	 * @param hours the number of hours per cycle
	 */
	public void setHourlyCycleLength(int hours) {
		setCyclesPerHour(1d / Math.max(1, hours));
	}
}