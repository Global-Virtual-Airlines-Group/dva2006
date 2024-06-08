// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A Web Service to allow asynchronous pre-population of a Pilot's log book into a dedicated cache
 * to reduce Flight Report approval times.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class LogbookPreloadService extends WebService {

	private static final Cache<CacheableCollection<FlightReport>> _cache = CacheManager.getCollection(FlightReport.class, "Logbook");
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check for role
		if (!ctx.isUserInRole("PIREP") && !ctx.isUserInRole("Operations"))
			return SC_FORBIDDEN;
		
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(id);
			if (p == null)
				return SC_NOT_FOUND;
			
			// Check the cache
			CacheableCollection<FlightReport> data = _cache.get(p.cacheKey());
			if (data != null) {
				ctx.setHeader("X-Cache-Hit", 1);
				ctx.setHeader("X-Logbook-Size", data.size());
				return SC_OK;
			}
			
			// Load the log book and add to cache
			GetFlightReports rdao = new GetFlightReports(con);
			List<FlightReport> pireps = rdao.getByPilot(p.getID(), new LogbookSearchCriteria(null, ctx.getDB()));
			rdao.loadCaptEQTypes(p.getID(), pireps, ctx.getDB());
			_cache.add(new CacheableList<FlightReport>(p.cacheKey(), pireps));
			
			// Set response data
			ctx.setHeader("X-Cache-Hit", 0);
			ctx.setHeader("X-Logbook-Size", pireps.size());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}
	
	@Override
	public final boolean isLogged() {
		return false;
	}
}