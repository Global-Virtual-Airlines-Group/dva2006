// Copyright 2005, 2006, 2007, 2008, 2015, 2016, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load an exported Flight Schedule.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetSchedule extends ScheduleLoadDAO {
	
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H:mm").toFormatter();
		
	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	public GetSchedule(InputStream is) {
		super(null, is);
	}

	/*
	 * Helper method to load an airport bean.
	 */
	private Airport getAirport(String code, int line) {
		Airport a = SystemData.getAirport(code);
		if (a == null) {
			_status.addInvalidAirport(code.toUpperCase());
			_status.addMessage("Unknown Airport at Line " + line + " - " + code);
			a = new Airport(code, code, "Unknown - " + code);
		}

		return a;
	}

	/**
	 * Loads the Schedule Entries.
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if an I/O error occurs
	 */
	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		
		LocalDate today = LocalDate.now();
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		try (LineNumberReader br = new LineNumberReader(getReader())) {
			while (br.ready()) {
				String txtData = br.readLine();
				if ((!txtData.startsWith(";")) && (txtData.length() > 5)) {
					try {
						StringTokenizer tkns = new StringTokenizer(txtData, ",");
						if (tkns.countTokens() != 11)
							throw new IllegalArgumentException("Invalid number of tokens, count=" + tkns.countTokens());
						
						// Get the airline
						String aCode = tkns.nextToken();
						Airline a = SystemData.getAirline(aCode);
						if (a == null)
							throw new IllegalArgumentException("Invalid Airline Code - " + aCode);

						// Build the flight number and equipment type
						RawScheduleEntry entry = new RawScheduleEntry(a, Integer.parseInt(tkns.nextToken()), Integer.parseInt(tkns.nextToken()));
						entry.setEquipmentType(tkns.nextToken());

						// Get the airports and times
						entry.setAirportD(getAirport(tkns.nextToken(), br.getLineNumber()));
						entry.setTimeD(LocalDateTime.of(today, LocalTime.parse(tkns.nextToken(), _tf)));
						entry.setAirportA(getAirport(tkns.nextToken(), br.getLineNumber()));
						entry.setTimeA(LocalDateTime.of(today, LocalTime.parse(tkns.nextToken(), _tf)));
						if (!entry.isPopulated())
							throw new IllegalArgumentException("Invalid Airport Code");

						// Discard distance
						tkns.nextToken();

						// Load historic attributes
						entry.setHistoric(Boolean.valueOf(tkns.nextToken()).booleanValue());

						results.add(entry);
					} catch (Exception e) {
						_status.addMessage("Error on line " + br.getLineNumber() + " - " + e.getMessage());
					}
				}
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		return results;
	}
}