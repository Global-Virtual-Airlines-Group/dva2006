// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import org.json.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.DAOException;

import org.deltava.util.EnumUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to fetch Airport ATIS data. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class GetATIS extends DAO {
	
	private static final Cache<CacheableCollection<String>> _facilityCache = CacheManager.getCollection(String.class, "ATISCodes");
	private static final Cache<ATIS> _cache = CacheManager.get(ATIS.class, "ATIS");

	/**
	 * Returns the available ATIS facilities.
	 * @return a list of airport codes.
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<String> getFacilityCodes() throws DAOException {
		
		// Check the cache
		CacheableCollection<String> codes = _facilityCache.get(ATIS.class);
		if (codes != null)
			return codes.clone();
		
		try {
			init("https://datis.clowd.io/api/facilities");
			if (getResponseCode() != 200) return Collections.emptySet();
			Collection<String> results = new TreeSet<String>();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(getIn(), "UTF-8"), 16384)) {
				JSONArray ja = new JSONArray(new JSONTokener(br));
				ja.forEach(c -> { results.add(c.toString()); });
			}

			// Add to cache
			codes = new CacheableSet<String>(ATIS.class);
			codes.addAll(results);
			_facilityCache.add(codes);
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	/**
	 * Fetches an Airport ATIS.
	 * @param ap the Airport
	 * @param type the ATISType
	 * @return an ATIS bean, or null if not found
	 * @throws DAOException if an I/O error occurs
	 */
	public ATIS get(Airport ap, ATISType type) throws DAOException {
		
		// Check the cache
		ATIS a = _cache.get(ap.getICAO()); // cominbed ATIS check
		if (a == null)
			a = _cache.get(String.format("%s/%s", ap.getICAO(), type.name()));
		if (a != null)
			return a;
		
		try {
			ATIS result = null;
			init(String.format("https://datis.clowd.io/api/%s", ap.getICAO()));
			if (getResponseCode() != 200) return null;
			try (BufferedReader br = new BufferedReader(new InputStreamReader(getIn(), "UTF-8"), 8192)) {
				JSONTokener jt = new JSONTokener(br);
				Object o = jt.nextValue();
				if (!(o instanceof JSONArray))
					return null;
				
				DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("HHmm").toFormatter();
				JSONArray ja = (JSONArray) o;
				for (int x = 0; x < ja.length(); x++) {
					JSONObject jo = ja.getJSONObject(x);
					a = new ATIS(ap, EnumUtils.parse(ATISType.class, jo.getString("type").toUpperCase(), null));
					String data = jo.getString("datis");
					a.setData(data);
					int cpos = data.indexOf(" INFO ");
					a.setCode((cpos < 0) ? '?' : data.charAt(cpos + 6));
					
					// Parse the date
					int dpos = data.indexOf(' ', cpos + 8);
					while (!Character.isDigit(data.charAt(dpos - 1)))
						dpos--;
					
					String dt = data.substring(cpos + 8, dpos);
					LocalTime lt = LocalTime.parse(dt, dtf);
					LocalDateTime ldt = LocalDateTime.of(LocalDate.now(), lt);
					a.setEffectiveDate(ldt.toInstant(ZoneOffset.UTC));
					_cache.add(a);
					if ((a.getType() == type) || (a.getType() == ATISType.COMBINED))
						result = a;
				}
			}
			
			return result;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}