// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;

import org.json.*;
import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read SimVector JSON sechedule data.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class GetSimVector extends ScheduleLoadDAO {
	
	private static final Logger log = LogManager.getLogger(GetSimVector.class);
	
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H[H]:mm").parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
	
	private int _baseLine;

	/**
	 * Creates the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetSimVector(InputStream is) {
		super(ScheduleSource.SIMVECTOR, is);
	}
	
	/**
	 * Sets the base line number for this file. Since this is a JSON feed. the line reported will be the offset in the source JSON array.
	 * @param lineNumber the line number the base line number
	 */
	public void setBaseLine(int lineNumber) {
		_baseLine = lineNumber;
	}

	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		
		// Parse the JSON
		JSONArray ja = null;
		try (InputStream is = getStream()) {
			ja = new JSONArray(new JSONTokener(is));
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		LocalDate today = LocalDate.now();
		LocalDate sd = today.minusDays(1);
		LocalDate ed = LocalDate.of(today.getYear(), 1, 1).plusYears(1).minusDays(1);
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		for (int x = 0; x < ja.length(); x++) {
			JSONObject jo = ja.getJSONObject(x);
			try {
				Airline a = SystemData.getAirline(jo.optString("airline"));
				if (a == null)
					throw new IllegalArgumentException(String.format("Unknown airline - %s (%d)", jo.optString("airline"), Integer.valueOf(x)));
				
				Airport aD = SystemData.getAirport(jo.getString("dpt_airport"));
				if (aD == null)
					throw new IllegalArgumentException(String.format("Unknown Airport - %s (%d)", jo.getString("dpt_airport"), Integer.valueOf(x)));
				
				Airport aA = SystemData.getAirport(jo.getString("arr_airport"));
				if (aA == null)
					throw new IllegalArgumentException(String.format("Unknown Airport - %s (%d)", jo.getString("arr_airport"), Integer.valueOf(x)));
				
				RawScheduleEntry se = new RawScheduleEntry(a, jo.getInt("flight_number"), 1);
				se.setAirportD(aD); se.setAirportA(aA);
				se.setLineNumber(_baseLine + x);
				se.setStartDate(sd);
				se.setEndDate(ed);
				se.setDaysOfWeek(jo.optString("days", "1234567"));
				
				// Load departure/arrival times
				Instant iD = ZonedDateTime.of(today, LocalTime.parse(jo.getString("dpt_time"), _tf), ZoneOffset.UTC).toInstant();
				Instant iA = ZonedDateTime.of(today, LocalTime.parse(jo.getString("arr_time"), _tf), ZoneOffset.UTC).toInstant();
				ZonedDateTime zD = ZonedDateTime.ofInstant(iD, se.getAirportD().getTZ().getZone());
				ZonedDateTime zA = ZonedDateTime.ofInstant(iA, se.getAirportA().getTZ().getZone());
				se.setTimeD(zD.toLocalDateTime());
				se.setTimeA(zA.toLocalDateTime());
				
				results.add(se);
			} catch (IllegalArgumentException iae) {
				log.warn(iae.getMessage());
			}
		}
		
		return results;
	}
}