// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load a PHPVMSv7 Flight Schedule.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class GetPHPVMSSchedule extends ScheduleLoadDAO {
	
	private static final int TOKEN_COUNT = 27; 
	
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H[H]:mm").parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
	
	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	public GetPHPVMSSchedule(InputStream is) {
		super(ScheduleSource.VASYS, is);
	}

	/*
	 * Helper method to load an airport bean.
	 */
	private Airport getAirport(String code, int line) {
		Airport a = SystemData.getAirport(code);
		if (a == null) {
			_status.addInvalidAirport(code.toUpperCase());
			_status.addMessage(String.format("Unknown Airport at Line %d - %s", Integer.valueOf(line), code));
		}

		return a;
	}

	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		
		LocalDate today = LocalDate.now();
		LocalDate sd = today.minusDays(1);
		LocalDate ed = LocalDate.of(today.getYear(), 1, 1).plusYears(1).minusDays(1);
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		try (LineNumberReader br = new LineNumberReader(getReader())) {
			while (br.ready()) {
				String txtData = br.readLine();
				if ((txtData.length() < 25) || (txtData.indexOf(',') < 0)) continue;
				CSVTokens csv = StringUtils.parseCSV(txtData); int fn = StringUtils.parse(csv.get(1), -1);
				boolean isScheduled = (fn > 0) && (fn < 8000);
				
				if ((!txtData.startsWith(";")) && isScheduled) {
					try {
						if (csv.size() != TOKEN_COUNT)
							throw new IllegalArgumentException(String.format("Invalid number of tokens, count=%d expected=%d", Integer.valueOf(csv.size()), Integer.valueOf(TOKEN_COUNT)));
						
						// Get days of week
						String daysOfWeek = csv.get(8); Collection<DayOfWeek> days = new LinkedHashSet<DayOfWeek>();
						for (int x = 0; x < daysOfWeek.length(); x++) {
							char c = daysOfWeek.charAt(x);
							if (Character.isDigit(c)) {
								int day = Character.getNumericValue(c);
								if ((day > 0) && (day < 8))
									days.add(DayOfWeek.of(day));
							}
						}
						
						// Get the airline
						String aCode = csv.get(0);
						Airline a = SystemData.getAirline(aCode);
						if (a == null)
							throw new IllegalArgumentException(String.format("Invalid Airline Code - %s", aCode));

						// Build the flight number and equipment type
						RawScheduleEntry entry = new RawScheduleEntry(a, Integer.parseInt(csv.get(1)), Integer.parseInt(csv.get(4)));
						entry.setEquipmentType(csv.get(23));

						// Get the airports and times
						entry.setSource(ScheduleSource.VASYS);
						entry.setLineNumber(br.getLineNumber());
						entry.setStartDate(sd);
						entry.setEndDate(ed);
						days.forEach(entry::addDayOfWeek);
						
						// Load tokens and airports from parser
						String aD = csv.get(5); String tD = csv.get(9);
						String aA = csv.get(6); String tA = csv.get(10);
						entry.setAirportD(getAirport(aD, br.getLineNumber()));
						entry.setAirportA(getAirport(aA, br.getLineNumber()));
						if (!entry.isPopulated())
							throw new IllegalArgumentException(String.format("Invalid Airport Code - %s / %s", aD, aA));

						// Load departure/arrival times
						Instant iD = ZonedDateTime.of(today, LocalTime.parse(tD, _tf), ZoneOffset.UTC).toInstant();
						Instant iA = ZonedDateTime.of(today, LocalTime.parse(tA, _tf), ZoneOffset.UTC).toInstant();
						ZonedDateTime zD = ZonedDateTime.ofInstant(iD, entry.getAirportD().getTZ().getZone());
						ZonedDateTime zA = ZonedDateTime.ofInstant(iA, entry.getAirportA().getTZ().getZone());
						entry.setTimeD(zD.toLocalDateTime());
						entry.setTimeA(zA.toLocalDateTime());
						results.add(entry);
					} catch (Exception e) {
						_status.addMessage(String.format("Error on line %d - %s", Integer.valueOf(br.getLineNumber()), e.getMessage()));
					}
				}
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		return results;
	}
}