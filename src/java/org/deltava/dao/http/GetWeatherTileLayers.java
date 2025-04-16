// Copyright 2017, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.json.*;

import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to load weather tile layers.
 * @author Luke
 * @version 11.6
 * @since 8.0
 */

public class GetWeatherTileLayers extends DAO {
	
	/**
	 * Loads available Weather tile layers.
	 * @return a Collection of WeatherTileLayer beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<WeatherTileLayer> getLayers() throws DAOException {
		
		Collection<WeatherTileLayer> layers = new LinkedHashSet<WeatherTileLayer>();
		try {
			init("https://api.rainviewer.com/public/weather-maps.json");
			setCompression(Compression.GZIP, Compression.DEFLATE);
			try (InputStream in = getIn()) {
				JSONObject jo = new JSONObject(new JSONTokener(in));
				JSONObject ro = jo.optJSONObject("radar");
				if (ro != null) {
					JSONArray la = ro.getJSONArray("past");
					if ((la == null) || (la.length() == 0))
						throw new DAOException("No radar/past in response");

					WeatherTileLayer layer = new WeatherTileLayer("radar");
					layer.setZoom(9, 16);
					layer.setPaletteCode(4);
					layers.add(layer);
					for (int x = 0; x < la.length(); x++) {
						JSONObject lso = la.getJSONObject(x);
						Instant dt = Instant.ofEpochSecond(lso.getLong("time"));
						layer.addDate(new TileDate(dt, lso.getString("path")));
					}
				}
				
				JSONObject so = jo.getJSONObject("satellite");
				if (so != null) {
					JSONArray la = so.getJSONArray("infrared");
					if ((la == null) || (la.length() == 0))
						throw new DAOException("No satellite/infrared in response");
					
					WeatherTileLayer layer = new WeatherTileLayer("infrared");
					layer.setZoom(9, 16);
					layers.add(layer);
					for (int x = 0; x < la.length(); x++) {
						JSONObject lso = la.getJSONObject(x);
						Instant dt = Instant.ofEpochSecond(lso.getLong("time"));
						layer.addDate(new TileDate(dt, lso.getString("path")));
					}
				}
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		return layers;
	}
}