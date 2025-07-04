// Copyright 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.json.*;

import java.util.List;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.FlightStatsEntry;

/**
 * A utility class for dealing with JSON objects. 
 * @author Luke
 * @version 12.0
 * @since 7.3
 */

public class JSONUtils {

	// static class
	private JSONUtils() {
		super();
	}

	/**
	 * Ensures a JSON object has certain array properties populated. {@link JSONObject#accumulate} will not create an Array if only a single
	 * call is made, therefore this method will translate a JSON property that is not an Array into a single-value JSONArray. 
	 * @param o the JSONObject
	 * @param names the property names
	 */
	public static void ensureArrayPresent(JSONObject o, Object...names) {
		for (int x = 0; x < names.length; x++) {
			String name = String.valueOf(names[x]);
			Object oo = o.opt(name);
			if ((oo != null) && (!(oo instanceof JSONArray)))
				o.put(name, new JSONArray(List.of(oo)));
			else if (oo == null)
				o.put(name, new JSONArray());
		}
	}
	
	/**
	 * Converts a location into a Google Maps LatLngLiteral JSON object. 
	 * @param loc a GeoLocation
	 * @return a JSONObject
	 */
	public static JSONObject format(GeoLocation loc) {
		JSONObject jo = new JSONObject();
		jo.put("lat", loc.getLatitude());
		jo.put("lng", loc.getLongitude());
		return jo;
	}

	/**
	 * Converts a location into a MapbOX LngLat JSON array. 
	 * @param loc a GeoLocation
	 * @return a JSONArray
	 */
	public static JSONArray toLL(GeoLocation loc) {
		JSONArray ja = new JSONArray();
		ja.put(loc.getLongitude());
		ja.put(loc.getLatitude());
		return ja;
	}
	
	/**
	 * Formats an Airport into a JSON object.
	 * @param a an Airport
	 * @return a JSONObject
	 */
	public static JSONObject format(Airport a) {
		JSONObject jo = new JSONObject();
		jo.put("icao", a.getICAO());
		jo.put("iata", a.getIATA());
		jo.put("name", a.getName());
		jo.put("country", a.getCountry().getCode());
		jo.put("lat", a.getLatitude());
		jo.put("lng", a.getLongitude());
		jo.put("alt", a.getAltitude());
		jo.put("hasUSPFI", a.getHasPFI());
		jo.put("isSchengen", a.getIsSchengen());
		jo.put("utcOffset", a.getTZ().getZone().getRules().getOffset(Instant.now()).getTotalSeconds());
		return jo;
	}
	
	/**
	 * Formats an Airline into a JSON object.
	 * @param al an Airline
	 * @return a JSONObject
	 */
	public static JSONObject format(Airline al) {
		JSONObject jo = new JSONObject();
		jo.put("code", al.getCode());
		jo.put("name", al.getName());
		jo.put("historic", al.getHistoric());
		jo.put("color", al.getColor());
		return jo;
	}
	
	/**
	 * Formats a ScheduleEntry into a JSON object.
	 * @param se a ScheduleEntry
	 * @return a JSONObject
	 */
	public static JSONObject format(ScheduleEntry se) {
		JSONObject so = new JSONObject();
		so.put("airline", se.getAirline().getCode());
		so.put("flight", se.getFlightNumber());
		so.put("leg", se.getLeg());
		so.put("distance", se.getDistance());
		so.put("eqType", se.getEquipmentType());
		so.put("airportD", format(se.getAirportD()));
		so.put("airportA", format(se.getAirportA()));
		so.put("timeD", formatTime(se.getTimeD(), "HH:mm"));
		so.put("timeA", formatTime(se.getTimeA(), "HH:mm"));
		so.put("duration", se.getDuration().toMillis());
		so.put("historic", se.getHistoric());
		so.put("academy", se.getAcademy());
		so.putOpt("src", se.getSource());
		return so;
	}
	
	/**
	 * Converts an Instant into a JSON object with day/month/year components.
	 * @param dt an Instant
	 * @return a JSONobject
	 */
	public static JSONObject formatDate(Instant dt) {
		LocalDateTime ldt = LocalDateTime.ofInstant(dt, ZoneOffset.UTC);
		JSONObject dto = new JSONObject();
		dto.put("y", ldt.getYear()); dto.put("m", ldt.getMonthValue() - 1); dto.put("d", ldt.getDayOfMonth());
		return dto;
	}
	
	/**
	 * Converts a ZonedDateTime to a JSON object with hour, minute, second and millisecond components.
	 * @param zdt a ZonedDateTime
	 * @param fmtPattern an optional formatter for the text property
	 * @return a JSONObject
	 */
	public static JSONObject formatTime(ZonedDateTime zdt, String fmtPattern) {
		JSONObject jo = new JSONObject();
		jo.put("h", zdt.get(ChronoField.HOUR_OF_DAY)); jo.put("m", zdt.get(ChronoField.MINUTE_OF_HOUR)); 
		jo.put("s", zdt.get(ChronoField.SECOND_OF_MINUTE)); jo.put("ms", zdt.get(ChronoField.MILLI_OF_SECOND));
		if (fmtPattern != null)
			jo.put("text", DateTimeFormatter.ofPattern(fmtPattern).format(zdt));
		
		return jo;
	}
	
	/**
	 * Converts a FlightStatsEntry to JSON.
	 * @param fse a FlightStatsEntry
	 * @return a JSONOebject
	 */
	public static JSONObject format(FlightStatsEntry fse) {
		JSONObject jo = new JSONObject();
		jo.put("id", fse.getHexID());
		jo.put("legs", fse.getLegs());
		jo.put("distance", fse.getDistance());
		jo.put("acars", fse.getACARSLegs());
		jo.put("hours", fse.getHours());
		jo.put("pax", fse.getPax());
		jo.put("online", fse.getOnlineLegs());
		jo.put("historic", fse.getHistoricLegs());
		jo.put("dispatch", fse.getDispatchLegs());
		jo.put("simbrief", fse.getSimBriefLegs());
		jo.put("tour", fse.getTourLegs());
		jo.put("avgHours", fse.getAvgHours());
		jo.put("avgDistance", fse.getAvgDistance());
		return jo;
	}
}