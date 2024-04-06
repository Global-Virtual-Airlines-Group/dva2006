// Copyright 2011, 2016, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.time.*;
import java.time.temporal.ChronoUnit;

import org.deltava.util.StringUtils;

/**
 * A bean to store information about simulated economic cycles.
 * @author Luke
 * @version 11.2
 * @since 3.7
 */

public class EconomyInfo implements java.io.Serializable {
	
	private double _targetLoad;
	private double _minLoad = 0.125d;
	private double _amp;
	private double _yearHZ;
	
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
	 * Returns the cycle length in days.
	 * @return the cycle length
	 */
	public int getCycleLength() {
		return (int)Math.round(365d / _yearHZ);
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
	public void setCycleLength(int days) {
		setCyclesPerYear(365d / Math.max(1, days));
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("tgt=").append(_targetLoad);
		buf.append(",amp=").append(_amp);
		buf.append(",min=").append(_minLoad);
		buf.append(",yearHZ=").append(_yearHZ);
		buf.append(",sd=").append(StringUtils.format(_startDate, "MM/dd/yyyy"));
		return buf.toString();
	}
}