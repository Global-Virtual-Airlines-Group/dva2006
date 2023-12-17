// Copyright 2017, 2018, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.time.format.DateTimeFormatter.*;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.*;

import org.json.*;

import org.deltava.beans.Flight;
import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.FlightCodeParser;
import org.deltava.util.system.SystemData;

/**
 * Loads airline schedule data from FlightAware.
 * @author Luke
 * @version 11.1
 * @since 8.0
 */

public class GetFASchedule extends FlightAwareDAO {
	
	private static final Logger log = LogManager.getLogger(GetFASchedule.class);
	
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
		String url = String.format("/schedules/%s/%s", ISO_DATE.format(startDate), ISO_DATE.format(startDate.plus(days, ChronoUnit.DAYS)));
		try {
			init(buildURL(url, Map.of("airline", a.getCode(), "include_codeshares", String.valueOf(includeCS)))); JSONObject jo = null;
			try (InputStream is = getIn()) {
				jo = new JSONObject(new JSONTokener(is));
			}
			
			JSONArray data = jo.optJSONArray("scheduled");
			if ((data == null) || (data.length() == 0))
				return results;
			
			for (int x = 0; x < data.length(); x++) {
				JSONObject fo = data.getJSONObject(x);
				Flight f = FlightCodeParser.parse(fo.getString("ident"));
				RawScheduleEntry sce = new RawScheduleEntry(f.getAirline(), f.getFlightNumber(), 1);
				if (f.getAirline() == null) {
					_unknownAirlines.add(fo.getString("ident"));
					log.warn("Unknown airline - {}", fo.getString("ident"));
					continue;
				}
				
				sce.setAirportD(SystemData.getAirport(fo.optString("origin")));
				sce.setAirportA(SystemData.getAirport(fo.optString("destination")));
				if (!sce.isPopulated()) {
					if (sce.getAirportD() == null) _unknownAirports.add(fo.optString("origin"));
					if (sce.getAirportA() == null) _unknownAirports.add(fo.optString("destination"));
					log.warn("Unknown airport pair - [ {} / {} ]", fo.optString("origin"), fo.optString("destination"));
					continue;
				}
				
				sce.setEquipmentType(fo.optString("aircraft_type"));
				sce.setCodeShare(fo.optString("actual_ident"));
				
				// Get the arrival/departure times
				ZonedDateTime zdd = ZonedDateTime.ofInstant(Instant.from(ISO_DATE_TIME.parse(fo.getString("scheduled_out"))), sce.getAirportD().getTZ().getZone());
				ZonedDateTime zda = ZonedDateTime.ofInstant(Instant.from(ISO_DATE_TIME.parse(fo.getString("scheduled_in"))), sce.getAirportA().getTZ().getZone());
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