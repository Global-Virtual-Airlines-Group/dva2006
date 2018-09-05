// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load VABase-formatted airline schedules.
 * @author Luke
 * @version 8.3
 * @since 8.0
 */

public class GetVABaseSchedule extends ScheduleLoadDAO {
	
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("HHmm").toFormatter();
	private LocalDate _startDate = LocalDate.now();
	
	private static final Logger log = Logger.getLogger(GetVABaseSchedule.class);

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetVABaseSchedule(InputStream is) {
		super(is);
	}
	
	/**
	 * Sets the flight schedule effective date.
	 * @param ldt a LocalDateTime
	 */
	public void setStartDate(LocalDateTime ldt) {
		if (ldt != null)
			_startDate = ldt.truncatedTo(ChronoUnit.DAYS).toLocalDate();
	}
	
	private static String expandTime(String t) {
		StringBuilder ft = new StringBuilder(t);
		while (ft.length() < 4)
			ft.insert(0, '0');
		
		return ft.toString();
	}
	
	@Override
	public Collection<ScheduleEntry> process() throws DAOException {
		try (LineNumberReader lr = getReader()) {
			Collection<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
			lr.readLine(); String data = lr.readLine();
			while (data != null) {
 				List<String> csv = StringUtils.split(data, ","); boolean isOK = true;
				DayOfWeek dow = DayOfWeek.valueOf(csv.get(0).toUpperCase()); int dayDelta = dow.ordinal() - _startDate.getDayOfWeek().ordinal();
				LocalDate flightDate = _startDate.plusDays((dayDelta < 0) ? (7 + dayDelta) : dayDelta);
				
				RawScheduleEntry rse = new RawScheduleEntry(SystemData.getAirline(csv.get(1)), StringUtils.parse(csv.get(2), 1), StringUtils.parse(csv.get(3), 1));
				rse.setAirportD(SystemData.getAirport(csv.get(5)));
				rse.setAirportA(SystemData.getAirport(csv.get(7)));
				rse.setEquipmentType(getEquipmentType(csv.get(4)));
				rse.setTimeD(LocalDateTime.of(flightDate, LocalTime.parse(expandTime(csv.get(6)), _tf)));
				rse.setTimeA(LocalDateTime.of(flightDate, LocalTime.parse(expandTime(csv.get(8)), _tf)));
				rse.setDay(dow);
				
				if (rse.getEquipmentType() == null) {
					isOK = false;
					_invalidEQ.add(csv.get(9));
					log.warn("Unknown equipment code at Line " + lr.getLineNumber() + " - " + csv.get(4) + " (" + data + ")");
					_errors.add("Unknown equipment code at Line " + lr.getLineNumber() + " - " + csv.get(4));
				} else if (rse.getAirportD() == null) {
					isOK = false;
					_invalidAP.add(csv.get(5));
					log.warn("Unknown Airport at Line " + lr.getLineNumber() + " - " + csv.get(5) + " (" + data + ")");
					_errors.add("Unknown Airport at Line " + lr.getLineNumber() + " - " + csv.get(5));
				} else if (rse.getAirportA() == null) {
					isOK = false;
					_invalidAP.add(csv.get(7));
					log.warn("Unknown Airport at Line " + lr.getLineNumber() + " - " + csv.get(7) + " (" + data + ")");
					_errors.add("Unknown Airport at Line " + lr.getLineNumber() + " - " + csv.get(7));
				} else if (rse.getAirline() == null) {
					isOK = false;
					_invalidAL.add(csv.get(1));
					log.warn("Unknown airline at Line " + lr.getLineNumber() + " - " + csv.get(1) + " (" + data + ")");
					_errors.add("Unknown airline at Line " + lr.getLineNumber() + " - " + csv.get(1));
				} else if (!rse.getAirline().getApplications().contains(SystemData.get("airline.code"))) {
					isOK = false;
					log.info("Disabled airline at Line " + lr.getLineNumber() + " - " + rse.getAirline().getCode() + " (" + csv.get(1) + ")");
				}
				
				data = lr.readLine();
				if (isOK)
					results.add(rse);
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}		
	}
}