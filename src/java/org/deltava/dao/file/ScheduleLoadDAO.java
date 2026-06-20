// Copyright 2006, 2007, 2009, 2015, 2016, 2017, 2019, 2020, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.util.*;
import java.time.*;
import java.io.InputStream;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * An abstract class to store common methods for Flight Schedule import Data Access Objects.
 * @author Luke
 * @version 12.5
 * @since 1.0
 */

public abstract class ScheduleLoadDAO extends DAO {

	protected final ImportStatus _status;
	protected final Map<String, Airline> _airlines = new HashMap<String, Airline>();
	
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
		String codeShare;
	}
	
	/**
	 * Exception to track invalid schedule data.
	 */
	protected static class InvalidDataException extends Exception {
		private final int _line;
		
		InvalidDataException(String msg, int line) {
			super(msg);
			_line = line;
		}
		
		public int getLine() {
			return _line;
		}
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
			_iataMappings.put(a.getName().toUpperCase(), a);
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
		_airlines.clear();
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
	 * Helper method to map an IATA equipment code to an aircraft type.
	 * @param iataCode the IATA code
	 * @param line the line/record number in the source file
	 * @return the Aircraft
	 * @throws InvalidDataException if the IATA code is empty or unknown
	 * @see ScheduleLoadDAO#setAircraft(Collection)
	 */
	protected String getEquipmentType(String iataCode, int line) throws InvalidDataException {
		if (ScheduleEntry.EQ_VARIES.equalsIgnoreCase(iataCode)) return ScheduleEntry.EQ_VARIES;
		if (StringUtils.isEmpty(iataCode))
			throw new InvalidDataException (String.format("No Equipment at Record %d", Integer.valueOf(line)), line);
		
		Aircraft a = _iataMappings.get(iataCode.toUpperCase());
		if (a == null) {
			_status.addInvalidEquipment(iataCode.toUpperCase());
			throw new InvalidDataException (String.format("Unknown Equipment at Record %d - %s", Integer.valueOf(line), iataCode), line);
		}
		
		return a.getName();
	}
	
	/*
	 * Helper method to load an Airport bean, tracking invalid entries. This can handle IATA and ICAO codes.
	 * @param code the Airport code (IATA or ICAO)
	 * @param line the line/record number in the source file
	 * @return the Airport
	 * @throws InvalidDataException if the code is empty or unknown
	 */
	protected Airport getAirport(String code, int line) throws InvalidDataException {
		if (StringUtils.isEmpty(code))
			throw new InvalidDataException (String.format("No Airport at Record %d", Integer.valueOf(line)), line);
		
		Airport a = SystemData.getAirport(code);
		if (a == null) {
			_status.addInvalidAirport(code.toUpperCase());
			throw new InvalidDataException(String.format("Unknown Airport at Record %d - %s", Integer.valueOf(line), code), line);
		}

		return a;
	}
	
	/**
	 * Helper method to load an Airline bean, tracking invalid entries.
	 * @param code the Airline code
	 * @param line the line/record number in the source file
	 * @return the Airline
	 * @throws InvalidDataException if the code is empty or unknown
	 */
	protected Airline getAirline(String code, int line) throws InvalidDataException {
		if (StringUtils.isEmpty(code))
			throw new InvalidDataException(String.format("No Airline at Record %d", Integer.valueOf(line)), line);
		
		Airline a = SystemData.getAirline(code);
		if (a == null) {
			_status.addInvalidAirline(code.toUpperCase());
			throw new InvalidDataException(String.format("Unknown Airline at Record %d - %s", Integer.valueOf(line), code), line);
		}
		
		return a;
	}
}