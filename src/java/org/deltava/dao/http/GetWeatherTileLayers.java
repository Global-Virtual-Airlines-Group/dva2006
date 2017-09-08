// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.json.*;

import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;

import org.deltava.util.tile.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.tile.TileAddress;

/**
 * A Data Access Object to load weather tile layers.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class GetWeatherTileLayers extends DAO {
	
	private static final Cache<CacheableCollection<WeatherTileLayer>> _cache = CacheManager.getCollection(WeatherTileLayer.class, "twcSeriesList");
	private static final String KEY = "$TWCSeriesList";
	
	/**
	 * Loads available Weather tile layers.
	 * @return a Collection of WeatherTileLayer beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<WeatherTileLayer> getLayers() throws DAOException {
		
		// Check the cache
		CacheableCollection<WeatherTileLayer> layers = _cache.get(KEY);
		if (layers != null)
			return layers;
		
		StringBuilder buf = new StringBuilder("https://api.weather.com/v3/TileServer/series/productSet?apiKey=");
		buf.append(SystemData.get("security.key.twc"));
		buf.append("&productSet=twcAll");
		
		layers = new CacheableSet<WeatherTileLayer>(KEY);
		try {
			init (buf.toString());
			try (InputStream in = getIn()) {
				JSONObject jo = new JSONObject(new JSONTokener(in));
				if (!jo.has("seriesInfo"))
					throw new DAOException("No seriesInfo in response");
				
				for (String name : jo.keySet()) {
					JSONObject layerInfo = jo.getJSONObject(name);
					
					// Build bounding box
					Projection p = new MercatorProjection(layerInfo.getInt("nativeZoom"));
					JSONObject jtl = layerInfo.getJSONObject("bb").getJSONObject("tl");
					JSONObject jbr = layerInfo.getJSONObject("bb").getJSONObject("br");
					TileAddress tl = p.getAddress(new GeoPosition(jtl.getDouble("lat"), jtl.getDouble("lng")));
					TileAddress br = p.getAddress(new GeoPosition(jbr.getDouble("lat"), jbr.getDouble("lng")));
					
					JSONArray seriesTimes = layerInfo.getJSONArray("series");
					for (int x = 0; x < seriesTimes.length(); x++) {
						JSONObject st = seriesTimes.getJSONObject(x);
						boolean isFF = st.has("fts"); WeatherTileLayer layer = null;
						Instant dt = Instant.ofEpochSecond(st.getLong("ts"));
						if (isFF) {
							WeatherFutureTileLayer flayer = new WeatherFutureTileLayer(name, dt); layer = flayer;
							JSONArray ffa = st.getJSONArray("fts");
							for (int y = 0; y < ffa.length(); y++)
								flayer.addSlice(Instant.ofEpochSecond(ffa.getLong(y)));
						} else
							layer = new WeatherTileLayer(name, dt);
						
						layer.setZoom(layerInfo.getInt("nativeZoom"), layerInfo.getInt("maxZoom"));
						layer.setCoordinates(tl, br);
						layers.add(layer);
					}
				}
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		_cache.add(layers);
		return layers;
	}
}