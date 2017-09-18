// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.util.StringUtils;

/**
 * A Schedule Entry with passenger capacity data. 
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class RawScheduleEntry extends ScheduleEntry {

	private String _codeShare;
	
	private int _first;
	private int _biz;
	private int _economy;
	
	/**
	 * Creates the bean.
	 * @param a the Airline
	 * @param fNumber the flight number
	 */
	public RawScheduleEntry(Airline a, int fNumber) {
		super(a, fNumber, 1);
	}

	/**
	 * Returns the number of seats available in First class.
	 * @return the number of seats
	 */
	public int getFirst() {
		return _first;
	}
	
	/**
	 * Returns the number of seats available in Business class.
	 * @return the number of seats
	 */
	public int getBusiness() {
		return _biz;
	}
	
	/**
	 * Returns the number of seats available in Economy class.
	 * @return the number of seats
	 */
	public int getEconomy() {
		return _economy;
	}
	
	/**
	 * If a codeshare, the flight code of the operator's flight.
	 * @return the flight code, or null if none
	 */
	public String getCodeShare() {
		return _codeShare;
	}

	/**
	 * Sets the available seats on this flight.
	 * @param first the number of first class seats
	 * @param biz the number of business class seats
	 * @param economy the number of economy class seats
	 */
	public void setCapacity(int first, int biz, int economy) {
		_first = first;
		_biz = biz;
		_economy = economy;
	}
	
	/**
	 * If this is a codeshare flight, the flight code of the operator's flight.
	 * @param flightCode the flight code
	 */
	public void setCodeShare(String flightCode) {
		if (!StringUtils.isEmpty(flightCode))
			_codeShare = flightCode;
	}
}