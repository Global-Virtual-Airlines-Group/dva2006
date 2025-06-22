// Copyright 2005, 2006, 2007, 2008, 2015, 2016, 2018, 2019, 2020, 2022, 2025 Global Virtual Airlines Group. All Rights Reserved.
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
 * A Data Access Object to load an exported Flight Schedule.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class GetSchedule extends ScheduleLoadDAO {
	
	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("d[d]-MMM[-YYYY]").parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear()).toFormatter();
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H[H]:mm").parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
	
	private final Map<ScheduleSource, Integer> _srcMaxLines = new HashMap<ScheduleSource, Integer>();
	
	private final boolean _isUTC;
		
	/**
	 * Initializes the Data Access Object.
	 * @param src the ScheduleSource
	 * @param is the input stream to read
	 * @param isUTC TRUE if departure/arrival times are UTC, otherwise FALSE
	 */
	public GetSchedule(ScheduleSource src, InputStream is, boolean isUTC) {
		super(src, is);
		_isUTC = isUTC;
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
	
	/**
	 * Updates the maximum line number for a Schedule source.
	 * @param src a ScheduleSource
	 * @param maxLine the maximum line number
	 */
	public void setMaxLine(ScheduleSource src, int maxLine) {
		_srcMaxLines.put(src, Integer.valueOf(maxLine));
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
						if (tkns.countTokens() != 17)
							throw new IllegalArgumentException("Invalid number of tokens, count=" + tkns.countTokens());
						
						// Get source and line
						String srcName = tkns.nextToken();
						ScheduleSource src = EnumUtils.parse(ScheduleSource.class, srcName, _status.getSource());
						int srcLine = StringUtils.parse(tkns.nextToken(), -1);
						if (src == null)
							throw new IllegalArgumentException("Invalid Schedule source - " + srcName);
						
						// If line is zero, add to max
						if (srcLine == 0) {
							srcLine = _srcMaxLines.getOrDefault(src, Integer.valueOf(0)).intValue() + 1;
							_srcMaxLines.put(src, Integer.valueOf(srcLine));
						}
						
						// Calculate start/end dates
						LocalDate sd = LocalDate.parse(tkns.nextToken(), _df);
						LocalDate ed = LocalDate.parse(tkns.nextToken(), _df);
						String daysOfWeek = tkns.nextToken();
						
						// Get the airline
						String aCode = tkns.nextToken();
						Airline a = SystemData.getAirline(aCode);
						if (a == null)
							throw new IllegalArgumentException(String.format("Invalid Airline Code - %s", aCode));

						// Build the flight number and equipment type
						RawScheduleEntry entry = new RawScheduleEntry(a, Integer.parseInt(tkns.nextToken()), Integer.parseInt(tkns.nextToken()));
						entry.setEquipmentType(tkns.nextToken());

						// Get the airports and times
						entry.setSource(src);
						entry.setLineNumber(srcLine);
						entry.setStartDate(sd);
						entry.setEndDate(ed);
						entry.setDaysOfWeek(daysOfWeek);
						
						// Load tokens and airports from parser
						String aD = tkns.nextToken(); String tD = tkns.nextToken();
						String aA = tkns.nextToken(); String tA = tkns.nextToken();
						entry.setAirportD(getAirport(aD, br.getLineNumber()));
						entry.setAirportA(getAirport(aA, br.getLineNumber()));
						if (!entry.isPopulated())
							throw new IllegalArgumentException(String.format("Invalid Airport Code - %s / %s", aD, aA));

						// Load departure/arrival times
						if (_isUTC) {
							Instant iD = ZonedDateTime.of(today, LocalTime.parse(tD, _tf), ZoneOffset.UTC).toInstant();
							Instant iA = ZonedDateTime.of(today, LocalTime.parse(tA, _tf), ZoneOffset.UTC).toInstant();
							ZonedDateTime zD = ZonedDateTime.ofInstant(iD, entry.getAirportD().getTZ().getZone());
							ZonedDateTime zA = ZonedDateTime.ofInstant(iA, entry.getAirportA().getTZ().getZone());
							entry.setTimeD(zD.toLocalDateTime());
							entry.setTimeA(zA.toLocalDateTime());
						} else {
							entry.setTimeD(LocalDateTime.of(today, LocalTime.parse(tD, _tf)));
							entry.setTimeA(LocalDateTime.of(today, LocalTime.parse(tA, _tf)));
						}

						// Discard distance, load historic
						tkns.nextToken();
						entry.setHistoric(Boolean.parseBoolean(tkns.nextToken()));
						entry.setForceInclude(Boolean.parseBoolean(tkns.nextToken()));
						entry.setAcademy(Boolean.parseBoolean(tkns.nextToken()));
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