// Copyright 2017, 2019, 2020, 2023, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load raw PDF-exported airline schedules.
 * @author Luke
 * @version 12.5
 * @since 8.0
 */

public class GetRawPDFSchedule extends ScheduleLoadDAO {
	
	private final DateTimeFormatterBuilder _dfb = new DateTimeFormatterBuilder().appendPattern("MMM-d[d]");
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter();
	private LocalDate _effDate = LocalDate.now();
	
	private static final Logger log = LogManager.getLogger(GetRawPDFSchedule.class);

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetRawPDFSchedule(InputStream is) {
		super(ScheduleSource.DELTA, is);
	}
	
	/**
	 * Sets the flight schedule effective date.
	 * @param ldt a LocalDateTime
	 */
	public void setEffectiveDate(LocalDateTime ldt) {
		if (ldt != null)
			_effDate = ldt.truncatedTo(ChronoUnit.DAYS).toLocalDate();
	}
	
	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		DateTimeFormatter df = _dfb.parseDefaulting(ChronoField.YEAR_OF_ERA, _effDate.getYear()).toFormatter();
		
		try (LineNumberReader lr = getReader()) {
			Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
			lr.readLine(); String data = lr.readLine();
			while (data != null) {
 				CSVTokens csv = StringUtils.parseCSV(data);
 				if (csv.size() < 10) {
 					data = lr.readLine();
 					continue;
 				}
				
				// Parse the entry
 				try {
 					RawScheduleEntry rse = new RawScheduleEntry(getAirline(csv.get(7), lr.getLineNumber()), StringUtils.parse(csv.get(8), 1), StringUtils.parse(csv.get(9), 1));
					rse.setAirportD(getAirport(csv.get(2), lr.getLineNumber()));
					rse.setAirportA(getAirport(csv.get(4), lr.getLineNumber()));
					rse.setEquipmentType(getEquipmentType(csv.get(10), lr.getLineNumber()));
					rse.setTimeD(LocalDateTime.of(_effDate, LocalTime.parse(csv.get(3), _tf)));
				rse	.setTimeA(LocalDateTime.of(_effDate, LocalTime.parse(csv.get(5), _tf)));
					rse.setDayMap(StringUtils.parse(csv.get(6), 0));
					rse.setSource(ScheduleSource.DELTA);
					rse.setLineNumber(lr.getLineNumber());

					LocalDate startDate = "-".equals(csv.get(0)) ? LocalDate.now().minusDays(LocalDate.now().getDayOfYear()) : LocalDate.parse(csv.get(0), df);
					LocalDate endDate = "-".equals(csv.get(1)) ? LocalDate.now().minusDays(LocalDate.now().getDayOfYear()).plusYears(1) : LocalDate.parse(csv.get(1), df);
					rse.setStartDate(startDate);
					rse.setEndDate(endDate);
					if (!rse.getAirline().getApplications().contains(SystemData.get("airline.code")))
						throw new InvalidDataException(String.format("Disabled airline at Line %d - %s (%s)", Integer.valueOf(lr.getLineNumber()), rse.getAirline().getCode(), csv.get(7)), lr.getLineNumber());
					
					results.add(rse);
 				} catch (InvalidDataException ide) {
 					log.warn(ide.getMessage());
 				}
				
				data = lr.readLine();
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}		
	}
}