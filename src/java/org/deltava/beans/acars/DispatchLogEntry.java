// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

/**
 * A bean to store ACARS Dispatch flight data log entries. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class DispatchLogEntry extends DatabaseBean implements RoutePair {
	
	private Instant _createdOn;
	private int _flightID;
	private int _dispatcherID;
	private int _pilotID;
	
	private Simulator _sim;
	
	private int _fuelLoad;
	private Airport _airportD;
	private Airport _airportA;
	private String _eqType;
	private String _cruiseAlt;
	
	private String _sid;
	private String _star;
	private String _route;

	/**
	 * Creates the bean.
	 * @param id the database ID
	 */
	public DispatchLogEntry(int id) {
		super();
		setID(id);
	}

	/**
	 * Returns the creation date of this dispatch log entry.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the database ID of the ACARS flight associated with this Dispatch entry.
	 * @return the database ID, or zero if none
	 */
	public int getFlightID() {
		return _flightID;
	}
	
	/**
	 * Returns the database ID of the Dispatcher who created this log entry.
	 * @return the Dispatcher's database ID
	 */
	public int getDispatcherID() {
		return _dispatcherID;
	}

	/**
	 * Returns the database ID of the Pilot who received this flight data.
	 * @return the Pilot's database ID
	 */
	public int getPilotID() {
		return _pilotID;
	}
	
	/**
	 * Returns the total fuel load for this dispatch entry. 
	 * @return the total fuel load in pounds
	 */
	public int getFuelLoad() {
		return _fuelLoad;
	}
	
	/**
	 * Returns the equipment type used in this flight data. 
	 * @return the equipment type
	 */
	public String getEquipmentType() {
		return _eqType;
	}
	
	/**
	 * Returns the cruise altitude.
	 * @return the initial cruise altitude
	 */
	public String getCruiseAltitude() {
		return _cruiseAlt;
	}
	
	/**
	 * Returns the simulator for this dispatch log entry.
	 * @return the Simulator
	 */
	public Simulator getSimulator() {
		return _sim;
	}
	
	@Override
	public Airport getAirportD() {
		return _airportD;
	}
	
	@Override
	public Airport getAirportA() {
		return _airportA;
	}
	
	/**
	 * Returns the SID for this dispatch entry.
	 * @return the SID ID, or null if none
	 */
	public String getSID() {
		return _sid;
	}

	/**
	 * Returns the STAR for this dispatch entry.
	 * @return the STAR ID, or null if none
	 */
	public String getSTAR() {
		return _star;
	}
	
	/**
	 * Returns the Route for this dispatch entry.
	 * @return the route
	 */
	public String getRoute() {
		return _route;
	}

	/**
	 * Updates the creation date of this log entry.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	public void setFlightID(int id) {
		if (id > 0)
			validateID(_flightID, id);
		
		_flightID = id;
	}
	
	public void setDispatcherID(int id) {
		validateID(_dispatcherID, id);
		_dispatcherID = id;
	}
	
	public void setPilotID(int id) {
		validateID(_pilotID, id);
		_pilotID = id;
	}
	
	/**
	 * Updates the simulator for this dispatch log entry.
	 * @param sim the Simulator
	 */
	public void setSimulator(Simulator sim) {
		_sim = sim;
	}

	/**
	 * Updates the departure airport of this dipsatch entry.
	 * @param a the Airport
	 */
	public void setAirportD(Airport a) {
		_airportD = a;
	}
	
	/**
	 * Updates the arrival airport of this dipsatch entry. 
	 * @param a the Airport
	 */
	public void setAirportA(Airport a) {
		_airportA = a;
	}
	
	/**
	 * Updates the equipment used in this dispatch log entry.
	 * @param eqType the equipment type.
	 */
	public void setEquipmentType(String eqType) {
		_eqType = eqType;
	}
	
	/**
	 * Updates the cruise altitude for this dispatch log entry.
	 * @param alt the initial cruise altitude
	 */
	public void setCruiseAltitude(String alt) {
		_cruiseAlt = alt;
	}

	/**
	 * Updates the fuel load for this dispatch entry.
	 * @param fuel the total fuel load in pounds
	 */
	public void setFuelLoad(int fuel) {
		_fuelLoad = fuel;
	}

	/**
	 * Updates the SID for this dispatch entry.
	 * @param sid the SID ID
	 */
	public void setSID(String sid) {
		_sid = sid;
	}

	/**
	 * Updates the STAR for this dispatch entry.
	 * @param star the STAR ID
	 */
	public void setSTAR(String star) {
		_star = star;
	}

	/**
	 * Updates the Route for this dispatch entry.
	 * @param rt the route waypoints
	 */
	public void setRoute(String rt) {
		_route = rt;
	}
}