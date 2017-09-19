// Copyright 2009, 2010, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.*;

import org.deltava.beans.navdata.Runway;
import org.deltava.util.GeoUtils;

/**
 * A comparator to sort Runways based on appropriateness based on a wind heading. 
 * @author Luke
 * @version 8.0
 * @since 2.6
 */

public class RunwayComparator implements Comparator<Runway>, java.io.Serializable {
	
	private final int _hdg;
	private final int _spd;

	/**
	 * Initializes the Comparator.
	 * @param windHdg the wind heading in degrees  
	 * @param windSpeed the wind speed in knots
	 */
	public RunwayComparator(int windHdg, int windSpeed) {
		super();
		_hdg = windHdg;
		_spd = windSpeed;
	}

	/**
	 * Compares two runways by comparing their delta from the wind heading, followed by their natural order.
	 */
	@Override
	public int compare(Runway r1, Runway r2) {
		
		// Calculate the headwind component on each runway
		double wD1 = GeoUtils.delta(r1.getHeading(), _hdg);
		double wD2 = GeoUtils.delta(r2.getHeading(), _hdg);
		int hw1 = (int) (StrictMath.cos(Math.toRadians(wD1)) * _spd);
		int hw2 = (int) (StrictMath.cos(Math.toRadians(wD2)) * _spd);
		
		// Order the two - we the one with the greater wind differential to be smaller than the other, just like
		// the one with the more uses will be smaller - THIS IS A REVERSE SORTER
		int tmpResult = Integer.compare(hw2 / 5, hw1 / 5);
		return (tmpResult == 0) ? r2.compareTo(r1) : tmpResult;
	}
	
	@Override
	public String toString() {
		return "RunwayComparator-" + String.valueOf(_hdg) + "/" + String.valueOf(_spd);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}