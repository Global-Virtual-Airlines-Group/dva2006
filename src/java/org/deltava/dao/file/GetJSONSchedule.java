// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.time.*;
import java.util.*;
import java.io.InputStream;

import org.json.*;
import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.util.EnumUtils;

/**
 * A Data Access Object to read JSON-formatted raw Schedule entries.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class GetJSONSchedule extends ScheduleLoadDAO {
	
	private static final Logger log = LogManager.getLogger(GetJSONSchedule.class);

	/**
	 * Creates the Data Access Object.
	 * @param src the ScheduleSource
	 * @param is the InputStream to read
	 */
	public GetJSONSchedule(ScheduleSource src, InputStream is) {
		super(src, is);
	}

	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		
		// LOad the JSON Document
		JSONObject jo = new JSONObject(new JSONTokener(getReader()));
		if (!jo.has("entries"))
			throw new DAOException("Invalid JSON Structure");
		
		JSONArray ea = jo.getJSONArray("entries");
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>(ea.length() + 2);
		for (int x = 0; x < ea.length(); x++) {
			JSONObject so = ea.getJSONObject(x);
			try {
				RawScheduleEntry rse = new RawScheduleEntry(getAirline(so.getString("airline"), x), so.getInt("flight"), so.optInt("leg", 1));
				rse.setSource(EnumUtils.parse(ScheduleSource.class, so.optString("src"), _status.getSource()));
				rse.setLineNumber(x + 1);
				rse.setAirportD(getAirport(so.getString("airportD"), x));
				rse.setAirportA(getAirport(so.getString("airportA"), x));
				rse.setEquipmentType(getEquipmentType(so.getString("eqType"), x));
				rse.setStartDate(parseDate(so.getJSONObject("startDate")));
				rse.setEndDate(parseDate(so.getJSONObject("endDate")));
				rse.setTimeD(LocalDateTime.of(rse.getStartDate(), parseTime(so.getJSONObject("timeD"))));
				rse.setTimeA(LocalDateTime.of(rse.getStartDate(), parseTime(so.getJSONObject("timeA"))));
				rse.setAcademy(so.getBoolean("academy"));
				rse.setHistoric(so.getBoolean("historic"));
				rse.setForceInclude(so.getBoolean("forceInclude"));
				rse.setRemarks(so.optString("remarks"));
				rse.setComments(so.optString("comments"));
				results.add(rse);
			} catch (InvalidDataException ide) {
				log.warn(ide.getMessage());
			}
		}
		
		return results;
	}
	
	/*
	 * Helper method to parse JSON time objects.
	 */
	private static LocalTime parseTime(JSONObject to) {
		return LocalTime.of(to.getInt("h"), to.getInt("m"));
	}
	
	/*
	 * Helper method to parse JSON date objects.
	 */
	private static LocalDate parseDate(JSONObject dto) {
		return LocalDate.of(dto.getInt("y"), dto.getInt("m") + 1, dto.getInt("d"));
	}
}