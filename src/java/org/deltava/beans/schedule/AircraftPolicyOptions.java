// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.Auditable;
import org.deltava.beans.flight.ETOPS;

/**
 * A bean to store airline-specific aircraft options. 
 * @author Luke
 * @version 8.7
 * @since 8.7
 */

public class AircraftPolicyOptions implements Auditable {

	private final String _aircraft;
	private final String _appCode;
	
	private int _maxRange;
	
	private ETOPS _etops;
	private boolean _useSoftRunway;
	
	private int _toRunwayLength;
	private int _lndRunwayLength;
	
	private int _seats;

	/**
	 * Creates the bean.
	 * @param aircraft the aircraft code
	 * @param appCode the virtual airline code
	 */
	public AircraftPolicyOptions(String aircraft, String appCode) {
		super();
		_aircraft = aircraft;
		_appCode = appCode.toUpperCase().trim();
	}
	
	/**
	 * Returns the virtual airline code for these options.
	 * @return the app code
	 */
	public String getCode() {
		return _appCode;
	}
	
	/**
	 * Returns the maximum range of the aircraft.
	 * @return the range in miles
	 * @see AircraftPolicyOptions#setRange(int)
	 */
	public int getRange() {
		return _maxRange;
	}
	
	/**
	 * Returns whether this aircraft can use soft runways.
	 * @return TRUE if soft runways available, otherwise FALSE
	 * @see AircraftPolicyOptions#setUseSoftRunways(boolean)
	 */
	public boolean getUseSoftRunways() {
		return _useSoftRunway;
	}
	
	/**
	 * Returns whether the aircraft is ETOPS-qualified.
	 * @return TRUE if this is ETOPS-rated, otherwise FALSE
	 * @see AircraftPolicyOptions#setETOPS(ETOPS)
	 */
	public ETOPS getETOPS() {
		return _etops;
	}
	
	/**
	 * Returns the number of seats on the aircraft.
	 * @return the number of seats
	 * @see AircraftPolicyOptions#setSeats(int)
	 */
	public int getSeats() {
		return _seats;
	}
	
	/**
	 * Returns the Aircraft's minimum takeoff runway length.
	 * @return the runway length in feet
	 * @see AircraftPolicyOptions#setTakeoffRunwayLength(int)
	 */
	public int getTakeoffRunwayLength() {
		return _toRunwayLength;
	}
	
	/**
	 * Returns the Aircraft's minimum landing runway length.
	 * @return the runway length in feet
	 * @see AircraftPolicyOptions#setLandingRunwayLength(int)
	 */
	public int getLandingRunwayLength() {
		return _lndRunwayLength;
	}
	
	/**
	 * Updates the maximum range of the aircraft.
	 * @param range the range in miles
	 * @see AircraftPolicyOptions#getRange()
	 */
	public void setRange(int range) {
		_maxRange = Math.max(0, range);
	}
	
	/**
	 * Updates whether this aircraft is ETOPS-rated.
	 * @param e the ETOPS classification
	 */
	public void setETOPS(ETOPS e) {
		_etops = e;
	}
	
	/**
	 * Updates whether this aircraft can use soft runways.
	 * @param sr TRUE if soft runways available, otherwise FALSE
	 * @see AircraftPolicyOptions#getUseSoftRunways()
	 */
	public void setUseSoftRunways(boolean sr) {
		_useSoftRunway = sr;
	}
	
	/**
	 * Updates the minimum takeoff runway length of the Aircraft.
	 * @param len the runway length in feet
	 * @see AircraftPolicyOptions#getTakeoffRunwayLength()
	 */
	public void setTakeoffRunwayLength(int len) {
		_toRunwayLength = Math.max(0, len);
	}
	
	/**
	 * Updates the minimum landing runway length of the Aircraft.
	 * @param len the runway length in feet
	 * @see AircraftPolicyOptions#getLandingRunwayLength()
	 */
	public void setLandingRunwayLength(int len) {
		_lndRunwayLength = Math.max(0, len);
	}
	
	/**
	 * Updates the number of seats on the aircraft.
	 * @param seats the number of seats
	 * @see AircraftPolicyOptions#getSeats()
	 */
	public void setSeats(int seats) {
		_seats = Math.max(0, seats);
	}

	@Override
	public String getAuditID() {
		StringBuilder buf = new StringBuilder(_aircraft);
		buf.append("!!").append(_appCode);
		return buf.toString();
	}
}