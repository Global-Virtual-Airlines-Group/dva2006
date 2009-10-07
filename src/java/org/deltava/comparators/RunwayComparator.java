// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.navdata.Runway;

/**
 * A comparator to sort Runways based on appropriateness based on a wind heading. 
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class RunwayComparator implements java.util.Comparator<Runway> {
	
	private int _hdg;

	/**
	 * Initializes the Comparator.
	 * @param windHdg the wind heading in degrees  
	 */
	public RunwayComparator(int windHdg) {
		super();
		_hdg = windHdg;
	}

	/**
	 * Compares two runways by comparing their delta from the wind heading, followed by their natural order.
	 */
	public int compare(Runway r1, Runway r2) {
		
		// Calculate the heading difference between us and the runway1 heading
		int hd1 = Math.abs(r1.getHeading() - _hdg);
		if (hd1 >= 300)
			hd1 = Math.abs(hd1 - 360);

		// Calculate the heading difference between us and the runway2 heading
		int hd2 = Math.abs(r2.getHeading() - _hdg);
		if (hd2 >= 300)
			hd2 = Math.abs(hd2 - 360);
		
		// Order the two - we the one with the greater wind differential to be smaller than the other, just like
		// the one with the fewer uses will be smaller
		int tmpResult = Integer.valueOf(hd2 / 60).compareTo(Integer.valueOf(hd1 / 60));
		return (tmpResult == 0) ? r1.compareTo(r2) : tmpResult;
	}
}