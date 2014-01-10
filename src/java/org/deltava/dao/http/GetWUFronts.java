// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.*;
import java.util.Calendar;

import org.json.*;

import org.deltava.beans.wx.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.DAOException;
import org.deltava.util.CalendarUtils;

/**
 * A Data Access Object to fetch front/cyclone data from Weather Underground.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class GetWUFronts extends DAO {
	
	private static final String[] HL = {"HIGHS", "LOWS"};
	
	/**
	 * Loads Front data.
	 * @return a Collection of WeatherLines
	 * @throws DAOException if an error occurs
	 */
	public WeatherMapData getFronts() throws DAOException {
		try {
			init("http://api.wunderground.com/api/e466a9feecedb389/fronts/view.json");
			if (getResponseCode() != HTTP_OK)
				return null;
			
			// Process the JSON document
			JSONTokener jt = new JSONTokener(new InputStreamReader(getIn()));
			JSONObject jo = (JSONObject) jt.nextValue();
			JSONObject sd = jo.getJSONObject("FRONTS");
			
			// Get the effective date
			JSONObject jd = sd.getJSONObject("UTCDATE");
			Calendar cld = CalendarUtils.getInstance(null, true);
			cld.set(Calendar.YEAR, jd.getInt("year"));
			cld.set(Calendar.MONTH, jd.getInt("mon") - 1);
			cld.set(Calendar.DAY_OF_MONTH, jd.getInt("day"));
			cld.set(Calendar.HOUR_OF_DAY, jd.getInt("hour"));
			
			// Get the fronts
			WeatherMapData result = new WeatherMapData(cld.getTime());
			JSONArray jf = sd.getJSONArray("FRONTS");
			for (int x = 0; x < jf.length(); x++) {
				JSONObject fo = jf.getJSONObject(x);
				Front f = Front.valueOf(fo.optString("type", "COLD").replace("OCFNT", "OCLD"));
				WeatherLine ft = new WeatherLine(f);
				result.add(ft);
				
				// Load the positions
				JSONArray pa = fo.getJSONArray("points");
				for (int p = 0; p < pa.length(); p++) {
					JSONObject po = pa.getJSONObject(p);
					ft.add(new GeoPosition(po.getDouble("lat"), po.getDouble("lon")));
				}
			}
			
			// Get the highs and lows
			for (String k : HL) {
				Cyclone.Type t = Cyclone.Type.valueOf(k.substring(0, k.length() - 2));
				JSONArray jhl = sd.getJSONArray(k);
				for (int x = 0; x < jhl.length(); x++) {
					JSONObject jc = jhl.getJSONObject(x);
					Cyclone c = new Cyclone(t, new GeoPosition(jc.getDouble("lat"), jc.getDouble("lon")));
					c.setPressure(jc.getInt("pressuremb"));
					result.add(c);
				}
			}
			
			return result;
		} catch (JSONException | IOException ie) {
			throw new DAOException(ie);
		}
	}
}