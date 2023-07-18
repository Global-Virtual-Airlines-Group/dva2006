// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.Comparator;

import org.deltava.beans.econ.YearlyTotal;

/**
 * A Comparator for YearlyTotal beans.
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

public class YearlyTotalComparator implements Comparator<YearlyTotal> {
	
	public static final int LEGS = 0;
	public static final int DISTANCE = 1;
	public static final int POINTS = 2;
	
	private final int _sortType;

	/**
	 * Initializes the comparator.
	 * @param sortType the sort type 
	 */
	public YearlyTotalComparator(int sortType) {
		super();
		_sortType = sortType;
	}

	@Override
	public int compare(YearlyTotal yt1, YearlyTotal yt2) {
		switch (_sortType) {
		case POINTS:
			int tmpResult = Integer.compare(yt1.getPoints(), yt2.getPoints());
			if (tmpResult == 0) tmpResult = Integer.compare(yt1.getLegs(), yt2.getLegs());
			return (tmpResult == 0) ? Integer.compare(yt1.getDistance(), yt2.getDistance()) : tmpResult;

		case DISTANCE:
			tmpResult = Integer.compare(yt1.getDistance(), yt2.getDistance());
			if (tmpResult == 0) tmpResult = Integer.compare(yt1.getLegs(), yt2.getLegs());
			return (tmpResult == 0) ? Integer.compare(yt1.getPoints(), yt2.getPoints()) : tmpResult;
			
		default:
			tmpResult = Integer.compare(yt1.getLegs(), yt2.getLegs());
			if (tmpResult == 0) tmpResult = Integer.compare(yt1.getDistance(), yt2.getDistance());
			return (tmpResult == 0) ? Integer.compare(yt1.getPoints(), yt2.getPoints()) : tmpResult;
		}
	}
}