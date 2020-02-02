// Copyright 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.FlightCodeParser;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to import the SkyTeam schedule.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class GetSkyTeamSchedule extends ScheduleLoadDAO {
	
	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("dd MMM").parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear()).toFormatter();
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter();
	
	private static final Logger log = Logger.getLogger(GetSkyTeamSchedule.class);
	
	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetSkyTeamSchedule(InputStream is) {
		super(ScheduleSource.SKYTEAM, is);
	}
	
	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		
		boolean isStarted = false; Airport aD = null, aA = null; RawScheduleEntry lastEntry = null;
		try (LineNumberReader lr = getReader()) {
			Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();

			// Get to first routepair block
			String data = null;
			do {
				data = lr.readLine();
				isStarted = data.startsWith("FROM: ");
			} while (!isStarted && lr.ready());
			
			while (data != null) {
				if (data.startsWith("FROM: ")) {
					String code = data.substring(data.lastIndexOf(' ') + 1);
					aD = SystemData.getAirport(code);
					if (aD == null) {
						_status.addInvalidAirport(code);
						_status.addMessage("Unknown airport at Line " + lr.getLineNumber() + " - " + code);
						log.warn("Unknown airport at Line " + lr.getLineNumber() + " - " + code);
					}
				} else if (data.startsWith("TO: ")) {
					String code = data.substring(data.lastIndexOf(' ') + 1);
					aA = SystemData.getAirport(code);
					if (aA == null) {
						_status.addInvalidAirport(code);
						_status.addMessage("Unknown airport at Line " + lr.getLineNumber() + " - " + code);
						log.warn("Unknown airport at Line " + lr.getLineNumber() + " - " + code);
					}
				} else if ((data.length() > 40) && Character.isDigit(data.charAt(0)) && Character.isDigit(data.charAt(1)) && (aD != null) && (aA != null)) {
					FlightData fd = parseFlightLine(data); boolean isOK = true;
					
					// Check for codeshare
					if (!fd.flightNumber.endsWith("*") && !GROUND_EQ.contains(fd.eqType)) {
						RawScheduleEntry se = new RawScheduleEntry(FlightCodeParser.parse(fd.flightNumber));
						se.setAirportD(aD); se.setAirportA(aA);
						se.setEquipmentType(getEquipmentType(fd.eqType));
						se.setSource(ScheduleSource.SKYTEAM);
						se.setLineNumber(lr.getLineNumber());
						for (char c : fd.daysOfWeek.toCharArray()) {
							int dayNumber = Character.getNumericValue(c);
							se.addDayOfWeek((dayNumber == 1) ? DayOfWeek.SUNDAY : DayOfWeek.of(dayNumber - 1));
						}
						
						// Parse dates/times - if start date is more than 100 days in the past, add a year
						LocalDate sd = LocalDate.parse(fd.startDate, _df);
						LocalDate ed = LocalDate.parse(fd.endDate, _df);
						long deltaDays = sd.toEpochDay() - ed.toEpochDay();
						if (deltaDays <= -100)
							sd = sd.plusYears(1);
						if (ed.isBefore(sd))
							ed = ed.plusYears(1);
						
						LocalDate endDate = sd;
						if (fd.timeA.endsWith("+1") || fd.timeA.endsWith("+2")) {
							int days = Character.getNumericValue(fd.timeA.charAt(fd.timeA.length() - 1));
							fd.timeA = fd.timeA.substring(0, fd.timeA.length() - 2);
							endDate = endDate.plusDays(days);
						} else if (fd.timeA.endsWith("+-1")) {
							fd.timeA = fd.timeA.substring(0, fd.timeA.length() - 3);
							endDate = endDate.plusDays(-1);
						}

						se.setStartDate(sd);
						se.setEndDate(ed);
						se.setTimeD(LocalDateTime.of(sd, LocalTime.parse(fd.timeD, _tf)));
						se.setTimeA(LocalDateTime.of(endDate, LocalTime.parse(fd.timeA, _tf)));
						
						if (se.getEquipmentType() == null) {
							isOK = false;
							_status.addInvalidEquipment(fd.eqType);
							log.warn("Unknown equipment code at Line " + lr.getLineNumber() + " - " + fd.eqType + " (" + data + ")");
							_status.addMessage("Unknown equipment code at Line " + lr.getLineNumber() + " - " + fd.eqType);
						} else if (se.getAirline() == null) {
							isOK = false;
							_status.addInvalidAirline(fd.flightNumber.substring(0, 2));
							log.warn("Unknown airline at Line " + lr.getLineNumber() + " - " + fd.flightNumber + " (" + data + ")");
							_status.addMessage("Unknown airline at Line " + lr.getLineNumber() + " - " + fd.flightNumber);
						} else if (!se.getAirline().getApplications().contains(SystemData.get("airline.code"))) {
							isOK = false;
							log.info("Disabled airline at Line " + lr.getLineNumber() + " - " + se.getAirline().getCode() + " (" + fd.flightNumber.substring(0, 2) + ")");
						}

						if (isOK) {
							results.add(se);
							lastEntry = se;
						}
					} else if (lastEntry != null)
						lastEntry.setCodeShare(fd.flightNumber.substring(0, fd.flightNumber.length() - 2));
				}	
				
				data = lr.readLine();
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	private static FlightData parseFlightLine(String src) {
		
		FlightData data = new FlightData();
		data.startDate = src.substring(0, 7).trim();
		data.endDate = src.substring(11, 17);
		
		int timePos = src.indexOf(':') - 2; int fnPos = src.indexOf(' ', timePos + 10); int eqPos = src.indexOf(' ', fnPos + 2);
		data.timeD = src.substring(timePos, timePos + 5);
		data.timeA = src.substring(timePos + 6, fnPos);
		data.flightNumber = src.substring(fnPos + 1, eqPos);
		data.eqType = src.substring(eqPos + 1, src.indexOf(' ', eqPos + 1));
		
		// Days of week - this can be variable
		StringBuilder dwBuf = new StringBuilder();
		for (int pos = 18; pos < timePos; pos++) {
			char c = src.charAt(pos);
			if ("1234567".indexOf(c) > -1)
				dwBuf.append(c);
		}
		
		data.daysOfWeek = dwBuf.toString();
		return data;
	}
}