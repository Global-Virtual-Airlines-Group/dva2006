// Copyright 2006, 2007, 2009, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.util.*;
import java.io.InputStream;

import org.deltava.beans.schedule.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;

/**
 * An abstract class to store common methods for Flight Schedule import Data Access Objects.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public abstract class ScheduleLoadDAO extends DAO {

	protected Map<String, Airline> _airlines;
	protected final Collection<String> _errors = new ArrayList<String>();
	
	protected final Collection<String> _invalidEQ = new TreeSet<String>();
	protected final Collection<String> _invalidAP = new TreeSet<String>();
	protected final Collection<String> _invalidAL = new TreeSet<String>();
	protected final Map<Airline, Collection<Airport>> _unsvcAirports = new TreeMap<Airline, Collection<Airport>>();
	
	private final Map<String, Aircraft> _iataMappings = new HashMap<String, Aircraft>();

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	protected ScheduleLoadDAO(InputStream is) {
		super(is);
	}

	/**
	 * Initializes the IATA/ICAO aircraft code mappings.
	 * @param acInfo a collection of Aircraft profile beans
	 */
	public void setAircraft(Collection<Aircraft> acInfo) {
		for (Aircraft a : acInfo) {
			a.getIATA().forEach(iata -> _iataMappings.putIfAbsent(iata, a));
			if (!StringUtils.isEmpty(a.getICAO()))
				_iataMappings.putIfAbsent(a.getICAO(), a);
		}
	}
	
	/**
	 * Initializes the list of airlines.
	 * @param airlines a Collection of Airline beans
	 * @see ScheduleLoadDAO#setAircraft(Collection)
	 */
	public void setAirlines(Collection<Airline> airlines) {
		_airlines = new HashMap<String, Airline>();
		airlines.forEach(a -> a.getCodes().forEach(c -> _airlines.put(c,  a)));
	}
	
	/**
	 * Returns back the loaded Flight Schedule entries.
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public abstract Collection<ScheduleEntry> process() throws DAOException;

	/**
	 * Returns any error messages from the Schedule load.
	 * @return a Collection of error messages
	 */
	public Collection<String> getErrorMessages() {
		return _errors;
	}
	
	/**
	 * Returns the Airports that are not serviced by a particular Airline in the schedule.
	 * @return a Map of Collections of Airports, keyed by Airline
	 */
	public Map<Airline, Collection<Airport>> getUnservedAirports() {
		return _unsvcAirports;
	}
	
	/**
	 * Returns any invalid IATA equipment codes encountered during the import.
	 * @return a sorted Collection of IATA equipment codes
	 */
	public Collection<String> getInvalidEQ() {
		return _invalidEQ;
	}
	
	/**
	 * Returns any invalid IATA airport codes encountered during the import.
	 * @return a sorted Collection of IATA airport codes
	 */
	public Collection<String> getInvalidAirports() {
		return _invalidAP;
	}
	
	/**
	 * Returns any invalid IATA airline codes encountered during the import.
	 * @return a sorted Collection of IATA airline codes
	 */
	public Collection<String> getInvalidAirlines() {
		return _invalidAL;
	}

	/**
	 * Maps an IATA equipment code to an aircraft type.
	 * @param iataCode the IATA code
	 * @return the aircraft type, or null if not found
	 * @throws NullPointerException if iataCode is null
	 * @see ScheduleLoadDAO#setAircraft(Collection)
	 */
	protected String getEquipmentType(String iataCode) {
		Aircraft a = _iataMappings.get(iataCode.toUpperCase());
		return (a == null) ? null : a.getName();
	}
	
	/*
	 * Helper function to return an invalid airport bucket.
	 */
	private Collection<Airport> getAirportBucket(Airline al) {
		Collection<Airport> bucket = _unsvcAirports.get(al);
		if (bucket == null) {
			bucket = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
			_unsvcAirports.put(al, bucket);
		}
		
		return bucket;
	}
	
	/**
	 * Ensures that both airports in a schedule entry are served by the Airline.
	 * @param se the ScheduleEntry to validate
	 * @return TRUE if the Airline serves both Airports, otherwise FALSE
	 */
	protected boolean validateAirports(ScheduleEntry se) {
		boolean isOK = true;
		
		Airline a = se.getAirline();
		if (!se.getAirportD().getAirlineCodes().contains(a.getCode())) {
			_errors.add(a.getName() + " does not serve " + se.getAirportD() + " - " + se.getFlightCode());
			getAirportBucket(a).add(se.getAirportD());
			isOK = false;
		}

		if (!se.getAirportA().getAirlineCodes().contains(a.getCode())) {
			_errors.add(a.getName() + " does not serve " + se.getAirportA() + " - " + se.getFlightCode());
			getAirportBucket(a).add(se.getAirportA());
			isOK = false;
		}
		
		return isOK;
	}
}