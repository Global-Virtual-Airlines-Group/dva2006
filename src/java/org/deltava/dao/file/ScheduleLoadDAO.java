// Copyright 2006, 2007, 2009, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.util.*;
import java.time.*;
import java.io.InputStream;

import org.deltava.beans.schedule.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * An abstract class to store common methods for Flight Schedule import Data Access Objects.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public abstract class ScheduleLoadDAO extends DAO {

	protected final ImportStatus _status;
	protected Map<String, Airline> _airlines;
	protected final Map<Airline, Collection<Airport>> _unsvcAirports = new TreeMap<Airline, Collection<Airport>>();
	
	private final Map<String, Aircraft> _iataMappings = new HashMap<String, Aircraft>();

	/**
	 * A utility class to store flight data components.
	 */
	protected static class FlightData {
		String startDate;
		String endDate;
		String daysOfWeek;
		String airportD;
		String airportA;
		String timeD;
		String timeA;
		String flightNumber;
		String eqType;
	}
	
	/**
	 * Initializes the Data Access Object.
	 * @param src the ScheduleSource
	 * @param is the input stream to read
	 */
	protected ScheduleLoadDAO(ScheduleSource src, InputStream is) {
		super(is);
		_status = new ImportStatus(src, Instant.now());
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
	public abstract Collection<RawScheduleEntry> process() throws DAOException;

	/**
	 * Returns the Airports that are not serviced by a particular Airline in the schedule.
	 * @return a Map of Collections of Airports, keyed by Airline
	 */
	public Map<Airline, Collection<Airport>> getUnservedAirports() {
		return _unsvcAirports;
	}
	
	/**
	 * Returns the schedule import results.
	 * @return an ImportStatus
	 */
	public ImportStatus getStatus() {
		return _status;
	}
	
	/**
	 * Maps an IATA equipment code to an aircraft type.
	 * @param iataCode the IATA code
	 * @return the aircraft type, or null if not found
	 * @throws NullPointerException if iataCode is null
	 * @see ScheduleLoadDAO#setAircraft(Collection)
	 */
	protected String getEquipmentType(String iataCode) {
		if ("EQV".equalsIgnoreCase(iataCode)) return "EQV";
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
			_status.addMessage(a.getName() + " does not serve " + se.getAirportD() + " - " + se.getFlightCode());
			getAirportBucket(a).add(se.getAirportD());
			isOK = false;
		}

		if (!se.getAirportA().getAirlineCodes().contains(a.getCode())) {
			_status.addMessage(a.getName() + " does not serve " + se.getAirportA() + " - " + se.getFlightCode());
			getAirportBucket(a).add(se.getAirportA());
			isOK = false;
		}
		
		return isOK;
	}
}