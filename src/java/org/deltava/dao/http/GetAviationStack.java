// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;

import org.json.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to access AviationStack APIs.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class GetAviationStack extends DAO {
	
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H[H]:mm").parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
	
	private final Map<String, Aircraft> _iataMappings = new HashMap<String, Aircraft>();
	private String _accessKey;
	
	@Deprecated
	private InputStream _is;
	
	@Deprecated
	public void setStream(InputStream is) {
		_is = is;
	}
	
	/**
	 * Updates the AviationStack API access key to use.
	 * @param key the access key
	 */
	public void setAccessKey(String key) {
		_accessKey = key;
	}
	
	/**
	 * Initializes the IATA/ICAO aircraft code mappings.
	 * @param acInfo a collection of Aircraft profile beans
	 */
	public void setAircraft(Collection<Aircraft> acInfo) {
		for (Aircraft a : acInfo) {
			_iataMappings.put(a.getName().toUpperCase(), a);
			a.getIATA().forEach(iata -> _iataMappings.putIfAbsent(iata, a));
			if (!StringUtils.isEmpty(a.getICAO()))
				_iataMappings.putIfAbsent(a.getICAO(), a);
		}
	}
	
	/**
	 * Helper method to map an IATA equipment code to an aircraft type.
	 * @param iataCode the IATA code
	 * @return the Aircraft, or null if not found
	 */
	protected String getEquipmentType(String iataCode) {
		if (StringUtils.isEmpty(iataCode)) return null;
		Aircraft a = _iataMappings.get(iataCode.toUpperCase());
		return (a == null) ? null : a.getName();
	}
	
	/**
	 * Loads flights in or out of a particular Airport.
	 * @param a the Airport
	 * @param al the Airline
	 * @param ld the departure date
	 * @param isDeparture TRUE for departing flights, FALSE for arrivals
	 * @return a PaginatedList of RawSCheduleEntry beans
	 * @throws DAOException if an error occurs
	 */
	public PaginatedList<RawScheduleEntry> get(Airport a, Airline al, LocalDate ld, boolean isDeparture) throws DAOException {
		return get(a, al, ld, isDeparture, 0);
	}

	/**
	 * Loads flights in or out of a particular Airport.
	 * @param a the Airport
	 * @param al the Airline
	 * @param ld the departure date
	 * @param isDeparture TRUE for departing flights, FALSE for arrivals
	 * @param ofs the starting offset to use for paginated result sets 
	 * @return a PaginatedList of RawSCheduleEntry beans
	 * @throws DAOException if an error occurs
	 */
	public PaginatedList<RawScheduleEntry> get(Airport a, Airline al, LocalDate ld, boolean isDeparture, int ofs) throws DAOException {
		
		// Build the URL
		StringBuilder urlBuf = new StringBuilder("https://api.aviationstack.com/v1/flightsFuture?iataCode=");
		urlBuf.append(a.getIATA());
		urlBuf.append("&type=");
		urlBuf.append(isDeparture ? "departure" : "arrival");
		urlBuf.append("&airline_iata=");
		urlBuf.append(al.getCode());
		urlBuf.append("&date=");
		urlBuf.append(StringUtils.format(ld, "yyyy-MM-dd"));
		urlBuf.append("&accessKey=");
		urlBuf.append(_accessKey);
		if (ofs > 0) {
			urlBuf.append("&offset=");
			urlBuf.append(ofs);
		}

		PaginatedList<RawScheduleEntry> results = new PaginatedList<RawScheduleEntry>(ofs);
		try {
			//setCompression(Compression.GZIP, Compression.DEFLATE);
			//init(urlBuf.toString());
			try (BufferedReader br = new BufferedReader(new InputStreamReader((_is == null) ? getIn() : _is, "utf-8"))) {
				JSONObject jo = new JSONObject(new JSONTokener(br));
				
				// Load Pagination metadata
				JSONObject po = jo.getJSONObject("pagination");
				results.setTotal(po.getInt("total"));
				results.setCount(po.getInt("count"));
				
				// Load scehdule entries
				JSONArray da = jo.getJSONArray("data");
				for (int x = 0; x < da.length(); x++) {
					JSONObject fo = da.getJSONObject(x);
					JSONObject dpo = fo.getJSONObject("departure");
					JSONObject aro = fo.getJSONObject("arrival");
					
					RawScheduleEntry rse = new RawScheduleEntry(al, fo.getJSONObject("flight").getInt("number"), 1);
					rse.setAirportD(SystemData.getAirport(dpo.getString("iataCode")));
					rse.setAirportA(SystemData.getAirport(aro.getString("iataCode")));
					rse.setEquipmentType(getEquipmentType(fo.getJSONObject("aircraft").getString("modelCode")));
					rse.setDaysOfWeek(fo.optString("weekday", String.valueOf(ld.getDayOfWeek().getValue())));
					rse.setStartDate(ld);
					rse.setEndDate(ld.plusDays(1));
					
					// Get local departure/arrival times
					LocalTime tD = LocalTime.parse(dpo.getString("scheduledTime"), _tf);
					LocalTime tA = LocalTime.parse(aro.getString("scheduledTime"), _tf);
					rse.setTimeD(LocalDateTime.of(ld, tD));
					rse.setTimeA(LocalDateTime.of(ld, tA));
					
					// Check for codeshares - this is the 'real' flight number, so keep it if we know the airline
					JSONObject cso = fo.optJSONObject("codeshared"); boolean isOK = true;
					if (cso != null) {
						Airline ca = SystemData.getAirline(cso.getJSONObject("airline").getString("iataCode"));
						if (ca != null)
							rse.setCodeShare(cso.getJSONObject("flight").optString("iataNumber"));
						else
							isOK = false;
					}
						
					if (isOK)
						results.add(rse);
				}
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		return results;
	}
}