// Copyright 2011, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.*;
import java.time.Instant;
import java.util.*;

import org.json.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to fetch Air Charts data.
 * @author Luke
 * @version 8.6
 * @since 4.0
 */

public class GetAirCharts extends DAO {
	
	private static final Logger log = Logger.getLogger(GetAirCharts.class);
	
	private static final String[] TYPE_NAMES = {"General", "Approach", "SID", "STAR"};
	private static final Chart.Type[] CAT_TYPE_MAP = {Chart.Type.GROUND, Chart.Type.ILS, Chart.Type.SID, Chart.Type.STAR, Chart.Type.APR};
	
	/**
	 * Returns available charts for an airport.
	 * @param a an Airport
	 * @return a Collection of ExternalCharts
	 * @throws DAOException if an error occurs
	 */
	public Collection<ExternalChart> getCharts(Airport a) throws DAOException {
		try {
			init("https://api.aircharts.org/v2/Airport/" + a.getICAO());
			int statusCode = getResponseCode();
			if (statusCode != HTTP_OK)
				throw new HTTPDAOException("Invalid Response Code", statusCode);
			
			JSONObject jo = null;
			try (InputStream is = getIn()) {
				jo = new JSONObject(new InputStreamReader(is));
			} catch (Exception e) {
				throw new DAOException(e);
			}
			
			// Parse the XML
			Collection<ExternalChart> results = new ArrayList<ExternalChart>();
			JSONObject ao = jo.optJSONObject(a.getICAO());
			if (ao == null)
				return results;
			
			JSONObject aco = ao.getJSONObject("charts");
			for (int x = 0; x < TYPE_NAMES.length; x++) {
				JSONArray ca = aco.optJSONArray(TYPE_NAMES[x]);
				if (ca == null) continue;
				for (int type = 0; type < ca.length(); type++) {
					JSONObject co = ca.getJSONObject(type);
					String name = co.getString("chartname");
					if (name.length() > 94) {
						log.warn("Truncating chart name " + name);
						name = name.substring(0, 95);
					}
					
					ExternalChart c = new ExternalChart(name, a);
					c.setExternalID(co.optString("id"));
					c.setImgType(Chart.ImageType.PDF);
					c.setSource("AirCharts");
					c.setType(CAT_TYPE_MAP[type]);
					c.setLastModified(Instant.now());
					c.setURL(co.optString("proxy", co.optString("url")));
					if ((c.getType() == Chart.Type.ILS) && (!c.getName().contains("ILS")))
						c.setType(Chart.Type.APR);

					results.add(c);
				}
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
}