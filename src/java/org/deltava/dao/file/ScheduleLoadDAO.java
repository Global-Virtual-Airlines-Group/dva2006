// Copyright 2006, 2007, 2009, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.util.*;
import java.time.*;
import java.io.InputStream;

import org.deltava.beans.schedule.*;

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
	
	/**
	 * Ground equipment codes.
	 */
	protected static final List<String> GROUND_EQ = List.of("TRN", "BUS", "LMO", "RFS");
	
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
}