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
import org.deltava.util.StringUtils;
import org.deltava.util.Tuple;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load the Delta flight schedule.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class GetDeltaSchedule extends ScheduleLoadDAO {
	
	private static final String DATE_FMT = "MMM-d[d]";
	
	private DateTimeFormatterBuilder _dfb = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(DATE_FMT);
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("h[h]:mma").toFormatter();
	private DateTimeFormatter _df;
	
	private LocalDate _effDate = LocalDate.now();
	
	private static final Logger log = Logger.getLogger(GetDeltaSchedule.class);

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetDeltaSchedule(InputStream is) {
		super(ScheduleSource.DELTA, is);
	}
	
	/**
	 * Sets the flight schedule effective date.
	 * @param ldt a LocalDateTime
	 */
	public void setEffectiveDate(LocalDateTime ldt) {
		if (ldt != null)
			_effDate = ldt.toLocalDate();
	}

	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		_df = _dfb.parseDefaulting(ChronoField.YEAR, _effDate.getYear()).toFormatter();
		
		try (LineNumberReader lr = getReader()) {
			Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
			boolean foundValidity = false;
		
			while (lr.ready()) {
				String data = lr.readLine(); boolean isOK = true; FlightData fd = null;
				
				// Check for effective date
				if (!foundValidity && data.startsWith("Validity Period:" )) {
					int pos = data.indexOf(" to ");
					if (pos > -1) {
						int year = StringUtils.parse(data.substring(pos - 4, pos), _effDate.getYear());
						_effDate = LocalDate.of(year, 1, 1);
						_dfb = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(DATE_FMT);
						_df = _dfb.parseDefaulting(ChronoField.YEAR, year).toFormatter();
						log.info("Updating effective year to " + year);
					}

					foundValidity = true;
				}
				
				try {
					fd = parse(data);
				} catch (Exception e) {
					log.warn("Parse error at Line " + lr.getLineNumber() + " - " + e.getMessage());
				}
				
				if (fd == null) continue;
				Tuple<LocalDate, LocalDate> dts = parseDates(fd.startDate, fd.endDate);
				if (dts == null) continue;
				
				boolean isCodeShare = fd.flightNumber.endsWith("*");
				if (isCodeShare) {
					fd.flightNumber = fd.flightNumber.substring(0, fd.flightNumber.length() - 1);
					isCodeShare = !fd.codeShare.contains("Delta");
				}
						
				RawScheduleEntry rse = new RawScheduleEntry(FlightCodeParser.parse(fd.flightNumber));
				rse.setAirportD(SystemData.getAirport(fd.airportD));
				rse.setAirportA(SystemData.getAirport(fd.airportA));
				rse.setEquipmentType(getEquipmentType(fd.eqType));
				rse.setStartDate(dts.getLeft());
				rse.setEndDate(dts.getRight());
				rse.setSource(ScheduleSource.DELTA);
				rse.setLineNumber(lr.getLineNumber());
				rse.setTimeD(LocalDateTime.of(_effDate, LocalTime.parse(fd.timeD, _tf)));
				rse.setTimeA(LocalDateTime.of(_effDate, LocalTime.parse(fd.timeA, _tf)));
				for (char c : fd.daysOfWeek.toCharArray()) {
					if (Character.isDigit(c))
						rse.addDayOfWeek(DayOfWeek.of(Character.getNumericValue(c)));
				}
				
				if (rse.getAirportD() == null) {
					isOK = false;
					_status.addInvalidAirport(fd.airportD);
					log.warn("Unknown airport at Line " + lr.getLineNumber() + " - " + fd.airportD);
					_status.addMessage("Unknown airport at Line " + lr.getLineNumber() + " - " + fd.airportD);
				} else if (rse.getAirportA() == null) {
					isOK = false;
					_status.addInvalidAirport(fd.airportA);
					log.warn("Unknown airport at Line " + lr.getLineNumber() + " - " + fd.airportA);
					_status.addMessage("Unknown airport at Line " + lr.getLineNumber() + " - " + fd.airportA);
				} else if (rse.getEquipmentType() == null) {
					isOK = false;
					_status.addInvalidEquipment(fd.eqType);
					log.warn("Unknown equipment code at Line " + lr.getLineNumber() + " - " + fd.eqType + " (" + data + ")");
					_status.addMessage("Unknown equipment code at Line " + lr.getLineNumber() + " - " + fd.eqType);
				} else if (rse.getAirline() == null) {
					isOK = false;
					_status.addInvalidAirline(fd.flightNumber.substring(0, 2));
					log.warn("Unknown airline at Line " + lr.getLineNumber() + " - " + fd.flightNumber + " (" + data + ")");
					_status.addMessage("Unknown airline at Line " + lr.getLineNumber() + " - " + fd.flightNumber);
				} else if (!rse.getAirline().getApplications().contains(SystemData.get("airline.code"))) {
					isOK = false;
					log.info("Disabled airline at Line " + lr.getLineNumber() + " - " + rse.getAirline().getCode() + " (" + fd.flightNumber.substring(0, 2) + ")");
				}
					
				if (isOK && !isCodeShare)
					results.add(rse);
				else if (isOK && log.isDebugEnabled())
					log.debug("Skipping codeshare " + fd.flightNumber);
			}

			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	private static FlightData parse(String data) {
		if (StringUtils.isEmpty(data)) return null;
		List<String> tkns = StringUtils.split(data, " ");
		if (tkns.size() < 14) return null;
		
		FlightData fd = new FlightData();
		fd.flightNumber = tkns.get(13);
		fd.startDate = tkns.get(0);
		fd.endDate = tkns.get(1);
		fd.airportD = tkns.get(2);
		fd.airportA = tkns.get(4);
		fd.eqType = tkns.get(tkns.size() - 4);
		fd.timeD = tkns.get(3).toUpperCase() + "M";
		String tA = tkns.get(5);
		if (tA.endsWith("+1") || tA.endsWith("+2"))
			tA = tA.substring(0, tA.length() - 2);
		
		fd.timeA = tA.toUpperCase() + "M";
		
		// Parse codeshare data
		StringBuilder csBuf = new StringBuilder();
		for (int idx = 16; (idx < tkns.size() - 4); idx++)
			csBuf.append(tkns.get(idx)).append(' ');

		fd.codeShare = csBuf.toString().trim();
		
		StringBuilder dwBuf = new StringBuilder();
		for (int ofs = 6; ofs < 13; ofs++) {
			char c = tkns.get(ofs).charAt(0);
			if (Character.isDigit(c))
				dwBuf.append(c);
		}
		
		fd.daysOfWeek = dwBuf.toString();
		return fd;
	}
	
	private Tuple<LocalDate, LocalDate> parseDates(String startDate, String endDate) {
		try {
			LocalDate startD = "-".equals(startDate) ? LocalDate.of(_effDate.getYear(), 1, 1) : LocalDate.parse(startDate, _df);
			LocalDate endD = "-".equals(endDate) ? LocalDate.of(_effDate.getYear(), 12, 31) : LocalDate.parse(endDate, _df);
			if (endD.isBefore(startD))
				endD = endD.plusYears(1);
			
			return Tuple.create(startD, endD);
		} catch (Exception e) {
			return null;
		}
	}
}