// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.time.DayOfWeek;
import java.util.*;

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
				
				String days = jo.optString("days", "1234567");
				for (int o = 0; o < days.length(); o++) {
					int d = Integer.parseInt(days.substring(o, o + 1));
					DayOfWeek dw = DayOfWeek.values()[d];
					se.addDayOfWeek(dw);
				}
				
				
				results.add(se);
			} catch (IllegalArgumentException iae) {
				log.warn(iae.getMessage());
			}
		}
		
		return results;
	}
}