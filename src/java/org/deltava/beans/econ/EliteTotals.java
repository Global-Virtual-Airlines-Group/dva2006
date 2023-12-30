// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

/**
 * An interface to describe beans that store Elite program totals.
 * @author Luke
 * @version 11.1
 * @since 9.2
 */

public interface EliteTotals {

	/**
	 * Returns the number of flight legs.
	 * @return the number of legs
	 */
	public int getLegs();
	
	/**
	 * Returns the distance flown.
	 * @return the distance in miles
	 */
	public int getDistance();
	
	/**
	 * Returns the number of Elite points.
	 * @return the number of points
	 */
	public int getPoints();
	
	/**
	 * Returns if legs, distance and points are all zero.
	 * @return TRUE if all are zero, otherwise FALSE
	 */
	default boolean isZero() {
		return (getLegs() == 0) && (getDistance() == 0) && (getPoints() == 0);
	}
	
	/**
	 * Compares two EliteEotal beans by comparing their legs, distance and points.
	 * @param et1 the first EliteTotals bean
	 * @param et2 the second EliteTotals bean
	 * @return a comparison result
	 */
	static int compare(EliteTotals et1, EliteTotals et2) {
		int tmpResult = Integer.compare(et1.getLegs(), et2.getLegs());
		if (tmpResult == 0)
			tmpResult = Integer.compare(et1.getDistance(),  et2.getDistance());
		
		return (tmpResult == 0) ? Integer.compare(et1.getPoints(), et2.getPoints()) : tmpResult;
	}
}