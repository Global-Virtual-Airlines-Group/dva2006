// Copyright 2005, 2006, 2007, 2008, 2015, 2016, 2018, 2019, 2020, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.util.EnumUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load an exported Flight Schedule.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class GetSchedule extends ScheduleLoadDAO {
	
	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("d[d]-MMM[-YYYY]").parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear()).toFormatter();
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H[H]:mm").parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
	
	private final Map<ScheduleSource, Integer> _srcMaxLines = new HashMap<ScheduleSource, Integer>();
		
	/**
	 * Initializes the Data Access Object.
	 * @param src the ScheduleSource
	 * @param is the input stream to read
	 */
	public GetSchedule(ScheduleSource src, InputStream is) {
		super(src, is);
	}

	/*
	 * Helper method to load an airport bean.
	 */
	private Airport getAirport(String code, int line) {
		Airport a = SystemData.getAirport(code);
		if (a == null) {
			_status.addInvalidAirport(code.toUpperCase());
			_status.addMessage("Unknown Airport at Line " + line + " - " + code);
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
						
						// Get days of week
						String daysOfWeek = tkns.nextToken(); Collection<DayOfWeek> days = new LinkedHashSet<DayOfWeek>();
						for (int x = 0; x < daysOfWeek.length(); x++) {
							char c = daysOfWeek.charAt(x);
							if (Character.isDigit(c)) {
								int day = Character.getNumericValue(c);
								if ((day > 0) && (day < 8))
									days.add(DayOfWeek.of(day));
							}
						}
						
						// Get the airline
						String aCode = tkns.nextToken();
						Airline a = SystemData.getAirline(aCode);
						if (a == null)
							throw new IllegalArgumentException("Invalid Airline Code - " + aCode);

						// Build the flight number and equipment type
						RawScheduleEntry entry = new RawScheduleEntry(a, Integer.parseInt(tkns.nextToken()), Integer.parseInt(tkns.nextToken()));
						entry.setEquipmentType(tkns.nextToken());

						// Get the airports and times
						entry.setSource(src);
						entry.setLineNumber(srcLine);
						entry.setStartDate(sd);
						entry.setEndDate(ed);
						days.forEach(entry::addDayOfWeek);
						entry.setAirportD(getAirport(tkns.nextToken(), br.getLineNumber()));
						entry.setTimeD(LocalDateTime.of(today, LocalTime.parse(tkns.nextToken(), _tf)));
						entry.setAirportA(getAirport(tkns.nextToken(), br.getLineNumber()));
						entry.setTimeA(LocalDateTime.of(today, LocalTime.parse(tkns.nextToken(), _tf)));
						if (!entry.isPopulated())
							throw new IllegalArgumentException("Invalid Airport Code");

						// Discard distance, load historic
						tkns.nextToken();
						entry.setHistoric(Boolean.parseBoolean(tkns.nextToken()));
						entry.setForceInclude(Boolean.parseBoolean(tkns.nextToken()));
						entry.setAcademy(Boolean.parseBoolean(tkns.nextToken()));
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