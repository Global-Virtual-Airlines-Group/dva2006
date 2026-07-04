// Copyright 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;

/**
 * A Data Access Object to load a PHPVMSv7 Flight Schedule.
 * @author Luke
 * @version 12.5
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

	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		
		// Get start/end dates
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
						
						// Build the flight number and equipment type
						RawScheduleEntry entry = new RawScheduleEntry(getAirline(csv.get(0), br.getLineNumber()), Integer.parseInt(csv.get(1)), StringUtils.parse(csv.get(4), 1));
						String eqType = getEquipmentType(csv.get(23), br.getLineNumber());
						if (eqType == null)
							throw new IllegalArgumentException(String.format("Unknown equipment type - %s", csv.get(23)));

						// Get the airports and times
						entry.setEquipmentType(eqType);
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
						
						// Check for one airport DST
						boolean useGMT = (entry.getAirportD().getTZ().hasDST() != entry.getAirportA().getTZ().hasDST());

						// Load departure/arrival times
						ZonedDateTime utcD = ZonedDateTime.of(today, LocalTime.parse(tD, _tf), ZoneOffset.UTC);
						ZonedDateTime utcA = ZonedDateTime.of(today, LocalTime.parse(tA, _tf), ZoneOffset.UTC);
						if (useGMT) {
							entry.setIsUTC(true);
							entry.setTimeD(utcD.toLocalDateTime());
							entry.setTimeA(utcA.toLocalDateTime());
						} else {
							ZonedDateTime zD = ZonedDateTime.ofInstant(utcD.toInstant(), entry.getAirportD().getTZ().getZone());
							ZonedDateTime zA = ZonedDateTime.ofInstant(utcA.toInstant(), entry.getAirportA().getTZ().getZone());
							entry.setTimeD(zD.toLocalDateTime());
							entry.setTimeA(zA.toLocalDateTime());
						}
						
						results.add(entry);
					} catch (Exception e) {
						if (!(e instanceof InvalidDataException))
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