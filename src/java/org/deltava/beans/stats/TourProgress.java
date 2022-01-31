// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.IDBean;

import org.deltava.util.StringUtils;

/**
 * A bean to track Pilot progress within a Flight Tour.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class TourProgress implements java.io.Serializable, IDBean, Comparable<TourProgress> {
	
	private final int _pilotID;
	private final int _tourID;
	private final int _legs;

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

	@Override
	public int compareTo(TourProgress tp2) {
		int tmpResult = Integer.compare(_legs, tp2._legs);
		if (tmpResult == 0)
			tmpResult = Integer.compare(_pilotID, tp2._pilotID);
		
		return (tmpResult == 0) ? Integer.compare(_tourID, tp2._tourID) : tmpResult;
	}
}