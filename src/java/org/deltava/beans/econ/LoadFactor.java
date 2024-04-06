// Copyright 2011, 2016, 2021, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.util.Random;
import java.time.Instant;

/**
 * A utility class to calculate flight passenger load factors.
 * 
 * The target load factor per day changes hourly. It is calculated using
 * a sine wave based around an airline-wide target load factor, the
 * cycle length and the maximum amplitude. There is a secondary load
 * factor which moves in 360-minute cycles.
 *  
 * @author Luke
 * @version 11.2
 * @since 3.7
 */

public class LoadFactor {
	
	private static final long MS_PER_HOUR = 60*60*1000;
	
	private final EconomyInfo _info;
	
	private static final Random _r = new Random();
	private static final double MAX_RAW = 3.75;

	/**
	 * Initializes the load factor calculator.
	 * @param info an EconomyInfo bean
	 */
	public LoadFactor(EconomyInfo info) {
		super();
		_info = info;
	}
	
	/**
	 * Returns the target load factor for a date.
	 * @param dt the date/time 
	 * @return a load factor between 0 and 1
	 */
	public double getTargetLoad(Instant dt) {
		
		// Calculate days/hours since airline start
		long hoursSinceStart = (dt.toEpochMilli() - _info.getStartDate().toEpochMilli()) / MS_PER_HOUR;
		long daysSinceStart = hoursSinceStart / 24;
		
		// Calculate the daily factor
		double daysPerCycle = 365d / _info.getCyclesPerYear();
		double dFreq = 2* Math.PI / daysPerCycle;
		return (Math.sin(daysSinceStart * dFreq) * _info.getAmplitude()) + _info.getTargetLoad();
	}
	
	/**
	 * Calculates a load factor for a flight.
	 *  @param dt the flight date/time
	 * @return a load factor between 0 and 1
	 */
	public double generate(Instant dt) {
		double avgLoad = getTargetLoad(dt);
		double maxAdjustAmt = Math.min(avgLoad, (1.05 -  avgLoad));
		
		// Generate the random factor for this run
		double scaleFactor = _r.nextGaussian();
		while (Math.abs(scaleFactor) > MAX_RAW)
			scaleFactor = _r.nextGaussian();
		
		// The load factor for this flight is going to be a random percentage of the maximum
		// adjustment amount.
		double rndAdjustFactor = scaleFactor * (1-(Math.abs(scaleFactor) / MAX_RAW));
		double adjustFactor = rndAdjustFactor * maxAdjustAmt;
		
		// Start with the average load factor, not the target
		double loadFactor = avgLoad + adjustFactor;
		
		// Save the load factor
		loadFactor = Math.min(1d, Math.max(_info.getMinimumLoad(), loadFactor));
		return loadFactor;
	}
}