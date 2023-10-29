// Copyright 2009, 2010, 2015, 2016, 2017, 2019, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.*;

import org.deltava.beans.UseCount;
import org.deltava.beans.navdata.Runway;

import org.deltava.util.GeoUtils;

/**
 * A comparator to sort Runways based on appropriateness based on a wind heading. 
 * @author Luke
 * @version 11.1
 * @since 2.6
 */

public class RunwayComparator implements Comparator<Runway>, java.io.Serializable {
	
	private final int _hdg;
	private final int _spd;
	private final boolean _compareUse;
	
	/**
	 * Initializes the Comparator.
	 * @param windHdg the wind heading in degrees  
	 * @param windSpeed the wind speed in knots
	 * @param compareUse TRUE if usage stats should be used if available, otherwise FALSE
	 */
	public RunwayComparator(int windHdg, int windSpeed, boolean compareUse) {
		super();
		_hdg = windHdg;
		_spd = windSpeed;
		_compareUse = compareUse;
	}

	@Override
	public int compare(Runway r1, Runway r2) {
		
		// Calculate the headwind component on each runway
		double wD1 = GeoUtils.delta(r1.getHeading(), _hdg);
		double wD2 = GeoUtils.delta(r2.getHeading(), _hdg);
		int hw1 = (int) (StrictMath.cos(Math.toRadians(wD1)) * _spd);
		int hw2 = (int) (StrictMath.cos(Math.toRadians(wD2)) * _spd);
		
		// Get use count if needed
		int u1 = _compareUse && (r1 instanceof UseCount uc1) ? uc1.getUseCount() : 0;
		int u2 = _compareUse && (r2 instanceof UseCount uc2) ? uc2.getUseCount() : 0;
		
		// Order the two - we the one with the greater wind differential to be smaller than the other, just like
		// the one with the more uses will be smaller - THIS IS A REVERSE SORTER
		int tmpResult = Integer.compare(hw2 / 3, hw1 / 3);
		if (tmpResult == 0) tmpResult = Integer.compare(u2, u1);
		return (tmpResult == 0) ? -(r2.compareTo(r1)) : tmpResult;
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