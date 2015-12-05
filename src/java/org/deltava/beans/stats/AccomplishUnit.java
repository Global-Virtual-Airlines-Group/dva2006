// Copyright 2010, 2011, 2012, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration to track Accomplishment units of measurement.
 * @author Luke
 * @version 6.3
 * @since 6.3
 */

public enum AccomplishUnit implements ComboAlias {
	
	LEGS("Flight Legs"), MILES("Flight Miles"), OLEGS("Online Legs"), VLEGS("VATSIM Legs"),
	ILEGS("IVAO Legs"), HLEGS("Historic Legs"), EVENTS("Events"), DLEGS("Dispatch Legs"),
	ALEGS("ACARS Legs"), AIRPORTS("Airports Visited"), AIRCRAFT("Aircraft Used"), 
	COUNTRIES("Countries Visited"), STATES("States Visited"), 
	MEMBERDAYS("Days since joining", Data.NONE), AIRLINES("Airlines"), 
	DFLIGHTS("Flights Dispatched", Data.DISPATCH), DHOURS("Dispatch Hours", Data.DISPATCH),
	EQLEGS("Legs in Aircraft"), CONTINENTS("Continents Visited"), PAX("Passengers Carried"),
	AIRPORTD("Departure Airport"), AIRPORTA("Arrival Airport"), PROMOLEGS("Promotion Legs");

	public enum Data {
		NONE, DISPATCH, FLIGHTS
	}
	
	private final String _name;
	private final Data _data;
	
	AccomplishUnit(String name) {
		this(name, Data.FLIGHTS);
	}
	
	AccomplishUnit(String name, Data d) {
		_name = name;
		_data = d;
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
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
}
