// Copyright 2010, 2011, 2012, 2014, 2015, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration to track Accomplishment units of measurement.
 * @author Luke
 * @version 9.1
 * @since 6.3
 */

public enum AccomplishUnit implements ComboAlias {
	
	LEGS("Flight Legs"), MILES("Flight Miles"), OLEGS("Online Legs"), VLEGS("VATSIM Legs"), ILEGS("IVAO Legs"), HLEGS("Historic Legs"), EVENTS("Events"), 
	DLEGS("Dispatch Legs"), ALEGS("ACARS Legs"), AIRPORTS("Airports Visited", true), AIRCRAFT("Aircraft Used"), COUNTRIES("Countries Visited", true), 
	STATES("States Visited", true), MEMBERDAYS("Days since joining", Data.NONE, false), AIRLINES("Airlines"), 	DFLIGHTS("Flights Dispatched", Data.DISPATCH, false), 
	DHOURS("Dispatch Hours", Data.DISPATCH, false), EQLEGS("Legs in Aircraft"), CONTINENTS("Continents Visited", true), PAX("Passengers Carried"),
	AIRPORTD("Departure Airport", true), AIRPORTA("Arrival Airport", true), PROMOLEGS("Promotion Legs"), ADLEGS("Departures from Airport", true), AALEGS("Arrivals at Airport", true);

	public enum Data {
		NONE, DISPATCH, FLIGHTS
	}
	
	private final String _name;
	private final Data _data;
	private final boolean _isGeo;
	
	/**
	 * Creates the Accomplishment Unit with Flights as the type of data.
	 * @param name the Unit name
	 */
	AccomplishUnit(String name) {
		this(name, Data.FLIGHTS, false);
	}
	
	/**
	 * Creates the Accomplishment Unit with Flights as the type of data.
	 * @param name the Unit name
	 * @param isGeo TRUE if a geolocation is utilized, otherwise FALSE
	 */
	AccomplishUnit(String name, boolean isGeo) {
		this(name, Data.FLIGHTS, isGeo);
	}
	
	/**
	 * Creates the Accomplishment Unit.
	 * @param name the Unit name
	 * @param d the type of data to track
	 * @param isGeo TRUE if a geolocation is utilized, otherwise FALSE
	 */
	AccomplishUnit(String name, Data d, boolean isGeo) {
		_name = name;
		_data = d;
		_isGeo= isGeo;
	}
	
	/**
	 * Returns the Unit name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the type of data required to calculate eligibility.
	 * @return a Data enumeration
	 */
	public Data getDataRequired() {
		return _data;
	}
	
	/**
	 * Returns whether this Unit tracks geographic locations.
	 * @return TRUE if a geolocation is utilized, otherwise FALSE
	 */
	public boolean isGeo() {
		return _isGeo;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
}