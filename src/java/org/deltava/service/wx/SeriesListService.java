// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.time.Instant;

import org.json.*;

import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.http.GetWeatherTileLayers;

import org.deltava.service.*;

/**
 * A Web Service to display TWC weather tile data.
 * @author Luke
 * @version 8.2
 * @since 8.0
 */

public class SeriesListService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Collection<WeatherTileLayer> layers = null;
		try {
			GetWeatherTileLayers dao = new GetWeatherTileLayers();
			dao.setReadTimeout(7500);
			layers = dao.getLayers();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		}
		
		// Build the JSON object
		JSONObject jo = new JSONObject(); JSONObject so = new JSONObject();
		jo.put("timestamp", System.currentTimeMillis());
		jo.put("seriesInfo", so);
		for (WeatherTileLayer l : layers) {
			jo.accumulate("seriesNames", l.getName());
			JSONObject lo = new JSONObject();
			so.put(l.getName(), lo);
			lo.put("nativeZoom", l.getNativeZoom());
			lo.put("maxZoom", l.getMaxZoom());
			for (Instant dt : l.getDates()) {
				JSONObject dto = new JSONObject();
				dto.put("unixDate", dt.toEpochMilli());
				if (l instanceof WeatherFutureTileLayer) {
					WeatherFutureTileLayer fl = (WeatherFutureTileLayer) l;
					for (Instant fdt : fl.getSliceDates(dt)) {
						JSONObject ffo = new JSONObject();
						ffo.put("unixDate", fdt.toEpochMilli());
						dto.accumulate("ff", ffo);
					}
				}
				
				lo.accumulate("series", dto);
			}
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(30);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception ex) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	@Override
	public boolean isLogged() {
		return false;
	}
}