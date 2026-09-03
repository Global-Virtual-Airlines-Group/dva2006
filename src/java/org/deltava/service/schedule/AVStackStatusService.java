// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.time.ZonedDateTime;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display AviationStack real-time import status.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class AVStackStatusService extends WebService {
	
	private static final Cache<CacheableCollection<LogEntry>> _cache = CacheManager.getCollection(LogEntry.class, "AVStackStatus");

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the entries
		CacheableCollection<LogEntry> entries = _cache.get(SystemData.get("airline.code"));
		if (entries == null)
			return SC_NOT_FOUND;
		
		// Get user formatting info
		Pilot p = ctx.getUser();
		String fmt = String.format("%s %s", p.getDateFormat(), p.getTimeFormat());
		
		// Convert to JSON
		JSONObject jo = new JSONObject();
		for (LogEntry le : entries) {
			ZonedDateTime zdt = ZonedDateTime.ofInstant(le.getCreatedOn(), ctx.getUser().getTZ().getZone());
			JSONObject lo = new JSONObject();
			lo.put("created", le.getCreatedOn().toEpochMilli());
			lo.put("time", StringUtils.format(zdt, fmt));
			lo.put("level", le.getLeve().name());
			lo.put("msg", le.getMessage());
			jo.accumulate("entries", lo);
		}
		
		// Check if we're complete
		jo.put("isComplete", entries.stream().anyMatch(le -> "Complete".equals(le.getMessage())));
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "entries");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(10);
			ctx.println(jo.toString(2));
			ctx.commit();
		} catch (Exception ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}