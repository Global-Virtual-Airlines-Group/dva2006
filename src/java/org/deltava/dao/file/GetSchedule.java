// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.text.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load an exported Flight Schedule.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class GetSchedule extends ScheduleLoadDAO {
	
	private final DateFormat _df = new SimpleDateFormat("HH:mm");
	
	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	public GetSchedule(InputStream is) {
		super(is);
	}

	/**
	 * Helper method to load an airport bean.
	 */
	private Airport getAirport(String code, int line) {
		Airport a = SystemData.getAirport(code);
		if (a == null) {
			_invalidAP.add(code.toUpperCase());
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
						Airline a = SystemData.getAirline(aCode);
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
						entry.setCanPurge(Boolean.valueOf(tkns.nextToken()).booleanValue());

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
		
		// Update the codeshare airlines
		updateCodeshares(results);
		return results;
	}
}