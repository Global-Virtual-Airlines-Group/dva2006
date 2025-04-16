// Copyright 2017, 2018, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.*;

import org.json.*;
import org.apache.logging.log4j.*;

import org.deltava.beans.system.*;
import org.deltava.beans.wx.*;
import org.deltava.dao.DAOException;
import org.deltava.dao.http.GetWeatherTileLayers;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Web Service to display TWC weather tile data.
 * @author Luke
 * @version 11.6
 * @since 8.0
 */

public class SeriesListService extends WebService {
	
	private static final Logger log = LogManager.getLogger(SeriesListService.class);
	
	private static final Cache<SeriesList> _cache = CacheManager.get(SeriesList.class, "RVSeriesList");
	private static final String KEY = "$RVSeriesList";
	
	private final ExecutorService _exec = Executors.newVirtualThreadPerTaskExecutor();
	private Future<SeriesList> _f;
	
	// Implement in the service so we can do prefetch
	private static class SeriesList extends CacheableSet<WeatherTileLayer> {
		private final Instant _created = Instant.now();
		private final Instant _expDate;
		
		SeriesList(int softExpirationTime) {
			super(KEY);
			_expDate = _created.plusSeconds(softExpirationTime);
		}
		
		public boolean isSoftExpired() {
			return Instant.now().isAfter(_expDate);
		}
	}
	
	private static SeriesList doLoad(boolean isAuth) {

		SeriesList sl = new SeriesList(30);
		try {
			APILogger.add(new APIRequest(API.RainViewer.createName("TILESERIES"), !isAuth));
			GetWeatherTileLayers dao = new GetWeatherTileLayers();
			dao.setReadTimeout(15000);
			TaskTimer tt = new TaskTimer();
			Collection<WeatherTileLayer> layers = dao.getLayers();
			sl.addAll(layers);
			log.info("Loaded RainViewer Series data in {}ms", Long.valueOf(tt.stop()));
			_cache.add(sl);
		} catch (DAOException de) {
			log.warn("Error loading RainViewer data - {}", de.getMessage());
		}
		
		return sl;
	}
	
	private Future<SeriesList> load(boolean isAuth) {
		return _exec.submit(() -> { return doLoad(isAuth); });
	}

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check the cache
		SeriesList sl = _cache.get(KEY);
		if ((sl == null) || sl.isSoftExpired()) {
			if ((_f == null) || _f.isDone())
				_f = load(ctx.isAuthenticated());

			if (sl == null) {
				try {
					sl = _f.get(16, TimeUnit.SECONDS);
				} catch (Exception e) {
					throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
				}
			}
		}
		
		// Build the JSON object
		Instant now = Instant.now();
		JSONObject jo = new JSONObject(); JSONObject so = new JSONObject();
		jo.put("date", now.toEpochMilli());
		jo.put("age", now.toEpochMilli() - sl._created.toEpochMilli());
		jo.put("seriesInfo", so);
		for (WeatherTileLayer l : sl) {
			jo.accumulate("seriesNames", l.getName());
			JSONObject lo = new JSONObject();
			so.put(l.getName(), lo);
			lo.put("nativeZoom", l.getNativeZoom());
			lo.put("maxZoom", l.getMaxZoom());
			lo.put("palette", l.getPaletteCode());
			for (TileDate td : l.getDates()) {
				JSONObject dto = new JSONObject();
				dto.put("unixDate", td.getDate().toEpochMilli());
				dto.put("path", td.getPath());
				lo.accumulate("series", dto);
			}
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "seriesNames");
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