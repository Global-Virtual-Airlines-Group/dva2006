// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to track flight routes within a Flight Schedule import. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class ImportRoute extends AbstractRoute implements Comparable<ImportRoute> {

	private final ScheduleSource _src;
	private int _priority;

	/**
	 * Creates the bean.
	 * @param src the ScheduleSource
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 */
	public ImportRoute(ScheduleSource src, Airport aD, Airport aA) {
		super(aD, aA);
		_src = src;
	}
	
	/**
	 * Returns the source of the routes.
	 * @return a ScheduleSource
	 */
	public ScheduleSource getSource() {
		return _src;
	}

	/**
	 * Returns the priority score.
	 * @return the score
	 */
	public int getPriority() {
		return _priority;
	}
	
	/**
	 * Updates the priority score.
	 * @param p the score
	 */
	public void setPriority(int p) {
		_priority = p;
	}
	
	/**
	 * Updates the number of flights between these two Airports.
	 * @param cnt the number of flight legs
	 */
	public void setFlights(int cnt) {
		_frequency = cnt;
	}

	@Override
	public int compareTo(ImportRoute ir2) {
		int tmpResult = Integer.compare(_priority, ir2._priority);
		if (tmpResult == 0)
			tmpResult = toString().compareTo(ir2.toString()); // compare the routePair
		
		return (tmpResult == 0) ? _src.compareTo(ir2._src) : tmpResult;
	}
}