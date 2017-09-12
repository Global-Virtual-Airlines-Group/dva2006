// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;

import org.json.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.FlightCodeParser;
import org.deltava.util.system.SystemData;

/**
 * Loads airline schedule data from FlightAware.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class GetFASchedule extends FlightAwareDAO {
	
	private static final Logger log = Logger.getLogger(GetFASchedule.class);
	
	private final Collection<String> _unknownAirlines = new HashSet<String>();
	private final Collection<String> _unknownAirports = new HashSet<String>();

	/**
	 * Returns any unknown Airport codes from the import.
	 * @return a Collection of Airport codes
	 */
	public Collection<String> getUnknownAirports() {
		return _unknownAirports;
	}
	
	/**
	 * Returns any unknown Airline codes from the import.
	 * @return a Collection of Airline codes
	 */
	public Collection<String> getUnknownAirlines() {
		return _unknownAirlines;
	}
	
	/**
	 * Loads flight schedule entries from FlightAware. 
	 * @param a the Airline
	 * @param start the result start offset
	 * @param startDate the start date
	 * @param days the number of days to search
	 * @param includeCS TRUE to include codeshare flights, otherwise FALSE
	 * @return a Collection of RawScheduleEntry beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<RawScheduleEntry> getSchedule(Airline a, int start, Instant startDate, int days, boolean includeCS) throws DAOException {
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		
		// Build the URL
		Map<String, String> params = new HashMap<String, String>();
		params.put("airline", a.getCode());
		params.put("offset", String.valueOf(start));
		Instant sd = startDate.truncatedTo(ChronoUnit.DAYS);
		Instant ed = sd.plus(days, ChronoUnit.DAYS);
		params.put("start_date", String.valueOf(sd.getEpochSecond()));
		params.put("end_date", String.valueOf(ed.getEpochSecond()));
		if (!includeCS)
			params.put("exclude_codeshare", "true");
			
		try {
			init(buildURL("AirlineFlightSchedules", params)); JSONObject jo = null;
			try (InputStream is = getIn()) {
				jo = new JSONObject(new JSONTokener(is));
			}
			
			JSONObject ro = jo.getJSONObject("AirlineFlightSchedulesResult");
			JSONArray data = ro.getJSONArray("data");
			for (int x = 0; x < data.length(); x++) {
				JSONObject fo = data.getJSONObject(x);
				
				ScheduleEntry se = FlightCodeParser.parse(fo.getString("ident"));
				RawScheduleEntry sce = new RawScheduleEntry(se.getAirline(), se.getFlightNumber());
				if (se.getAirline() == null) {
					_unknownAirlines.add(fo.getString("ident"));
					log.warn("Unknown airline " + fo.getString("ident"));
					continue;
				}
				
				sce.setAirportD(SystemData.getAirport(fo.optString("origin")));
				sce.setAirportA(SystemData.getAirport(fo.optString("destination")));
				if ((sce.getAirportD() == null) || (sce.getAirportA() == null)) {
					if (sce.getAirportD() == null) _unknownAirports.add(fo.optString("origin"));
					if (sce.getAirportA() == null) _unknownAirports.add(fo.optString("destination"));
					log.warn("Unknown airport pair - [ " + fo.optString("origin") + " / " + fo.optString("destination") + " ]");
					continue;
				}
				
				sce.setEquipmentType(fo.optString("aircrafttype"));
				sce.setCodeShare(fo.optString("actual_ident"));
				sce.setCapacity(fo.optInt("seats_cabin_first"), fo.optInt("seats_cabin_business"), fo.optInt("seats_cabin_coach"));
				
				// Get the arrival/departure times
				ZonedDateTime zdd = ZonedDateTime.ofInstant(Instant.ofEpochSecond(fo.optLong("departuretime")), sce.getAirportD().getTZ().getZone());
				ZonedDateTime zda = ZonedDateTime.ofInstant(Instant.ofEpochSecond(fo.optLong("arrivaltime")), sce.getAirportA().getTZ().getZone());
				sce.setTimeD(zdd.toLocalDateTime());
				sce.setTimeA(zda.toLocalDateTime());
				results.add(sce);
			}
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		return results;
	}
}