// Copyright 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.json.*;

import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load weather tile layers.
 * @author Luke
 * @version 10.0
 * @since 8.0
 */

public class GetWeatherTileLayers extends DAO {
	
	/**
	 * Loads available Weather tile layers.
	 * @return a Collection of WeatherTileLayer beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<WeatherTileLayer> getLayers() throws DAOException {
		
		StringBuilder buf = new StringBuilder("https://api.weather.com/v3/TileServer/series/productSet?apiKey=");
		buf.append(SystemData.get("security.key.twc"));
		buf.append("&productSet=twcAll");
		
		Collection<WeatherTileLayer> layers = new LinkedHashSet<WeatherTileLayer>();
		try {
			init (buf.toString());
			try (InputStream in = getIn()) {
				JSONObject jo = new JSONObject(new JSONTokener(in));
				JSONObject so = jo.optJSONObject("seriesInfo");
				if (so == null)
					throw new DAOException("No seriesInfo in response");
				
				for (String name : so.keySet()) {
					JSONObject layerInfo = so.getJSONObject(name);
					JSONArray seriesTimes = layerInfo.optJSONArray("series");
					if (seriesTimes == null) continue;
					
					// Build the bean
					boolean isFF = ((seriesTimes.length() > 0) && seriesTimes.getJSONObject(0).has("fts"));
					WeatherTileLayer layer = null;
					if (isFF)
						layer = new WeatherFutureTileLayer(name);
					else
						layer = new WeatherTileLayer(name);
					
					// Build bounding box
					layer.setZoom(layerInfo.getInt("nativeZoom"), layerInfo.getInt("maxZoom"));
					
					for (int x = 0; x < seriesTimes.length(); x++) {
						JSONObject st = seriesTimes.getJSONObject(x);
						Instant dt = Instant.ofEpochSecond(st.getLong("ts"));
						layer.addDate(dt);
						if (isFF) {
							WeatherFutureTileLayer flayer = (WeatherFutureTileLayer) layer;
							JSONArray ffa = st.getJSONArray("fts");
							for (int y = 0; y < ffa.length(); y++)
								flayer.addSlice(dt, Instant.ofEpochSecond(ffa.getLong(y)));
						}
					}
					
					layers.add(layer);
				}
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		return layers;
	}
}