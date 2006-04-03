// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.text.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.CollectionUtils;

/**
 * @author Luke
 *
 */
public class GetSchedule extends DAO implements ScheduleLoadDAO {
	
	private static final DateFormat _df = new SimpleDateFormat("HH:mm");
	
	private Map<String, Airline> _airlines;
	private Map<String, Airport> _airports;

	private Collection<String> _errors = new ArrayList<String>();

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	public GetSchedule(InputStream is) {
		super(is);
	}

	/**
	 * Initializes the list of airlines.
	 * @param airlines a Collection of Airline beans
	 * @see ScheduleLoadDAO#setAirports(Collection)
	 */
	public void setAirlines(Collection<Airline> airlines) {
		_airlines = CollectionUtils.createMap(airlines, "code");
	}
	
	/**
	 * Initalizes the list of airports.
	 * @param airports a Collection of Airport beans
	 * @see ScheduleLoadDAO#setAirlines(Collection)
	 */
	public void setAirports(Collection<Airport> airports) {
		_airports = CollectionUtils.createMap(airports, "IATA");
	}
	
	/**
	 * Returns any error messages from the load.
	 * @return a Collection of error messages
	 */
	public Collection<String> getErrorMessages() {
		return _errors;
	}
	
	/**
	 * Helper method to load an airport bean.
	 */
	private Airport getAirport(String code, int line) {
		Airport a = _airports.get(code.toUpperCase());
		if (a == null) {
			_errors.add("Unknown Airport at Line " + line + " - " + code);
			a = new Airport(code, code, "Unknown - " + code);
		}

		return a;
	}

	/**
	 * Loads the Schedule Entries.
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<ScheduleEntry> process() throws DAOException {
		Collection<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
		
		try {
			LineNumberReader br = new LineNumberReader(getReader());
			
			// Iterate through the file
			while (br.ready()) {
				String txtData = br.readLine();
				if ((!txtData.startsWith(";")) && (txtData.length() > 5)) {
					try {
						StringTokenizer tkns = new StringTokenizer(txtData, ",");
						if (tkns.countTokens() != 11)
							throw new ParseException("Invalid number of tokens, count=" + tkns.countTokens(), 0);
						
						// Get the airline
						String aCode = tkns.nextToken();
						Airline a = _airlines.get(aCode.toUpperCase());
						if (a == null)
							throw new ParseException("Invalid Airline Code - " + aCode, 0);

						// Build the flight number and equipment type
						ScheduleEntry entry = new ScheduleEntry(a, Integer.parseInt(tkns.nextToken()), Integer
								.parseInt(tkns.nextToken()));
						entry.setEquipmentType(tkns.nextToken());

						// Get the airports and times
						entry.setAirportD(getAirport(tkns.nextToken(), br.getLineNumber()));
						entry.setTimeD(_df.parse(tkns.nextToken()));
						entry.setAirportA(getAirport(tkns.nextToken(), br.getLineNumber()));
						entry.setTimeA(_df.parse(tkns.nextToken()));
						if ((entry.getAirportD() == null) || (entry.getAirportA() == null))
							throw new ParseException("Invalid Airport Code", 0);

						// Discard distance
						tkns.nextToken();

						// Load historic/purgeable attributes
						entry.setHistoric(Boolean.valueOf(tkns.nextToken()).booleanValue());
						entry.setPurge(Boolean.valueOf(tkns.nextToken()).booleanValue());

						// Add to results
						results.add(entry);
					} catch (Exception e) {
						_errors.add("Error on line " + br.getLineNumber() + " - " + e.getMessage());
					}
				}
			}
			
			br.close();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		// Return results
		return results;
	}
}