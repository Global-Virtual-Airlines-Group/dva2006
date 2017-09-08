// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.http.GetWeatherTileLayers;

import org.deltava.service.*;

/**
 * A Web Service to display TWC weather tile data.
 * @author Luke
 * @version 8.0
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
			layers = dao.getLayers();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		}
		
		// Build the JSON object
		JSONObject jo = new JSONObject(); JSONObject lo = null; String lastLayer = null;
		for (WeatherTileLayer l : layers) {
			if (!l.getName().equals(lastLayer)) {
				lo = new JSONObject();
				jo.put(l.getName(), lo);
				lastLayer = l.getName();
				lo.put("nativeZoom", l.getNativeZoom());
				lo.put("maxZoom", l.getMaxZoom());
				lo.accumulate("slices", Long.valueOf(l.getDate().toEpochMilli() / 1000));
				if (l.hasFuture()) {
					JSONArray fa = new JSONArray();
					WeatherFutureTileLayer fl = (WeatherFutureTileLayer) l;
					fl.getSliceDates().forEach(dt -> fa.put((int)(dt.toEpochMilli() / 1000)));
					lo.accumulate("futureSlices", fa);
				}
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