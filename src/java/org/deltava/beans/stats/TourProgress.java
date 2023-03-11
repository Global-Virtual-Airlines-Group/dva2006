// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Duration;
import java.time.Instant;

import org.deltava.util.StringUtils;

/**
 * A bean to track Pilot progress within a Flight Tour.
 * @author Luke
 * @version 10.5
 * @since 10.2
 */

public class TourProgress implements java.io.Serializable, org.deltava.beans.IDBean, Comparable<TourProgress> {
	
	private final int _pilotID;
	private final int _tourID;
	private final int _legs;
	
	private Instant _firstLeg;
	private Instant _lastLeg;
	
	/**
	 * Creates the bean.
	 * @param pilotID the Pilot database ID
	 * @param tourID the Flight Tour database ID
	 * @param legs the number of Flight Legs completed in this Tour
	 */
	public TourProgress(int pilotID, int tourID, int legs) {
		super();
		_pilotID = pilotID;
		_tourID = tourID;
		_legs = legs;
	}

	/**
	 * Returns the Pilot's database ID.
	 * @return the datbase ID
	 */
	@Override
	public int getID() {
		return _pilotID;
	}
	
	@Override
	public String getHexID() {
		return StringUtils.formatHex(_pilotID);
	}
	
	/**
	 * Returns the Tour's database ID.
	 * @return the datbase ID
	 */
	public int getTourID() {
		return _tourID;
	}
	
	/**
	 * Returns the number of legs completed.
	 * @return the number of legs
	 */
	public int getLegs() {
		return _legs;
	}
	
	/**
	 * Returns the flight date of the first leg in this Tour.
	 * @return the flight date
	 */
	public Instant getFirstLeg() {
		return _firstLeg;
	}
	
	/**
	 * Returns the flight date of the most recent leg in this Tour.
	 * @return the flight date
	 */
	public Instant getLastLeg() {
		return _lastLeg;
	}
	
	/**
	 * Returns the amount of time the Tour has been in progress by a Pilot.
	 * @return the Duration
	 */
	public Duration getProgressTime() {
		Instant ed = (_firstLeg == _lastLeg) ? Instant.now() : _lastLeg;
		return Duration.between(_firstLeg, ed);
	}

	/**
	 * Updates the flight date of the first leg in this Tour.
	 * @param dt the flight date
	 */
	public void setFirstLeg(Instant dt) {
		_firstLeg = dt;
	}
	
	/**
	 * Updates the flight date of the most recent leg in this Tour.
	 * @param dt the flight date
	 */
	public void setLastLeg(Instant dt) {
		_lastLeg = dt;
	}

	@Override
	public int compareTo(TourProgress tp2) {
		int tmpResult = Integer.compare(_legs, tp2._legs);
		if (tmpResult == 0)
			tmpResult = Integer.compare(_pilotID, tp2._pilotID);
		
		return (tmpResult == 0) ? Integer.compare(_tourID, tp2._tourID) : tmpResult;
	}
}