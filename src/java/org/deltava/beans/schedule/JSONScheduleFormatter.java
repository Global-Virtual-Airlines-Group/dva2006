// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.*;
import java.io.StringWriter;

import org.json.JSONObject;

import org.deltava.util.JSONUtils;

/**
 * A class to generate JSON-formatted raw schedule entries.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class JSONScheduleFormatter implements ScheduleFormatter {

	@Override
	public ScheduleFormat getFormat() {
		return ScheduleFormat.JSON;
	}

	@Override
	public String format(ScheduleEntry se) {
		RawScheduleEntry rse = (RawScheduleEntry) se;
		
		JSONObject jo = new JSONObject();
		jo.put("src", se.getSource().toString());
		jo.put("airline", se.getAirline().getCode());
		jo.put("flight", se.getFlightNumber());
		jo.put("leg", se.getLeg());
		jo.put("line", rse.getLineNumber());
		jo.put("equipment", se.getEquipmentType());
		jo.put("airportD", se.getAirportD().getICAO());
		jo.put("airportA", se.getAirportA().getICAO());
		jo.put("distance", se.getDistance());
		jo.put("days", rse.getDayMap());
		if (se.getHistoric()) jo.put("historic", se.getHistoric());
		if (rse.getForceInclude()) jo.put("forceInclude", rse.getForceInclude());
		if (se.getAcademy()) jo.put("academy", se.getAcademy());
		if (rse.getIsUTC()) jo.put("isUTC", rse.getIsUTC());
		jo.put("startDate", JSONUtils.formatDate(LocalDateTime.of(rse.getStartDate(), LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC)));
		jo.put("endDate", JSONUtils.formatDate(LocalDateTime.of(rse.getEndDate(), LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC)));
		jo.putOpt("remarks", se.getRemarks());
		jo.putOpt("comments", rse.getComments());
		jo.put("timeD", formatTime(rse.getTimeD()));
		jo.put("timeA", formatTime(rse.getTimeA()));
		if (rse.isCodeShare())
			jo.put("codeshare", rse.getCodeShare());
		if (se.getArrivalPlusDays() != 0)
			jo.put("plusDays", se.getArrivalPlusDays());
		
		return jo.toString();
	}

	@Override
	public String getHeader() {
		JSONObject jo = new JSONObject();
		jo.put("created", Instant.now().toEpochMilli());
		
		StringWriter out = new StringWriter();
		out.write("{\"info\":");
		out.write(jo.toString(2));
		out.write(",\n");
		out.write("\"entries\":[");
		return out.toString();
	}

	@Override
	public String getFooter() {
		return "]}";
	}
	
	@Override
	public String getSeparator() {
		return ",";
	}
	
	/*
	 * Helper method to format a time object.
	 */
	private static JSONObject formatTime(ZonedDateTime zdt) {
		JSONObject jo = new JSONObject();
		jo.put("h", zdt.getHour());
		jo.put("m", zdt.getMinute());
		return jo;
	}
}