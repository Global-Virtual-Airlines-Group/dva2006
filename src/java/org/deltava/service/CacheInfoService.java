// Copyright 2015, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;

/**
 * A Web Service to display application cache information.
 * @author Luke
 * @version 9.1
 * @since 6.2
 */

public class CacheInfoService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		if (!ctx.isUserInRole("Admin"))
			throw error(SC_UNAUTHORIZED, "Not in Admin role", false);
		
		// Get cache info
		List<CacheInfo> info = new ArrayList<CacheInfo>(CacheManager.getCacheInfo());
		String fmt = ctx.getUser().getNumberFormat();
		
		// Tell ACARS to update its cache
		if (SystemData.getBoolean("acars.enabled")) {
			EventDispatcher.send(new SystemEvent(SystemEvent.Type.CACHE_STATS));
			ThreadUtils.sleep(50);
			
			@SuppressWarnings("unchecked")
			Collection<CacheInfo> acarsInfo = (Collection<CacheInfo>) IPCUtils.reserialize(SharedData.get(SharedData.ACARS_CACHEINFO));
			if (acarsInfo != null)
				acarsInfo.forEach(ci -> { info.add(new CacheInfo("acars", ci)); });
		}
		
		// Sort and Format results
		Collections.sort(info);
		JSONObject jo = new JSONObject();
		for (CacheInfo c : info) {
			JSONObject co = new JSONObject();
			co.put("id", c.getID());
			co.put("type", c.getType());
			co.put("remote", c.getIsRemote());
			co.put("geo", c.getIsGeo());
			co.put("reqs", c.getRequests());
			co.put("hits", c.getHits());
			co.put("size", c.getSize());
			co.put("errors",c.getErrors());
			if (c.getRequests() > 0) co.put("hitRate", StringUtils.format(c.getHits() * 100d / c.getRequests(), fmt)  + "%");
			if (!c.getIsRemote()) {
				co.put("maxSize", c.getMaxSize());
				co.put("fill", StringUtils.format(c.getSize() * 100d / c.getMaxSize(), fmt) + "%");
			} else
				co.put("latency", StringUtils.format(c.getMaxSize() / 1000000d, fmt + "00"));
			
			jo.accumulate("caches", co);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "caches");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(2);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}