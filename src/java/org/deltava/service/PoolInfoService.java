// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.Serializable;

import org.json.*;

import org.deltava.util.*;

import org.gvagroup.pool.*;
import org.gvagroup.common.SharedData;

/**
 * A Web Service to display connection pool statistics.
 * @author Luke
 * @version 11.3
 * @since 11.3
 */

public class PoolInfoService extends WebService {

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
		
		// Get Connection Pool data
		Collection<String> appNames = SharedData.getApplications();
		Map<String, ConnectionPool<?>> pools = new TreeMap<String, ConnectionPool<?>>();
		for (String appName : appNames) {
			Serializable rawDBPool = SharedData.get(SharedData.JDBC_POOL + appName);
			Serializable rawJedisPool = SharedData.get(SharedData.JEDIS_POOL + appName);
			ConnectionPool<?> jdbcPool = (ConnectionPool<?>) IPCUtils.reserialize(rawDBPool);
			ConnectionPool<?> jedisPool = (ConnectionPool<?>) IPCUtils.reserialize(rawJedisPool);
			pools.put("JDBC$" + appName, jdbcPool);
			pools.put("JEDIS$" + appName, jedisPool);
		}

		// Create the JSON document
		JSONObject jo = new JSONObject();
		jo.put("apps", appNames);
		for (ConnectionPool<?> pool : pools.values()) {
			JSONObject po = new JSONObject();
			po.put("name", pool.getName());
			po.put("type", pool.getType());
			po.put("reqCount", pool.getTotalRequests());
			po.put("expandCount", pool.getExpandCount());
			po.put("errorCount", pool.getErrorCount());
			po.put("waitCount", pool.getWaitCount());
			po.put("fullCount", pool.getFullCount());
			for (ConnectionInfo inf : pool.getPoolInfo()) {
				JSONObject cpo = new JSONObject();
				cpo.put("id", inf.getID());
				cpo.put("type", inf.getTypeName());
				cpo.put("inUse", inf.getInUse());
				cpo.put("isDynamic", inf.getDynamic());
				cpo.put("isConnected", inf.getConnected());
				cpo.put("useCount", inf.getUseCount());
				cpo.put("useTime", inf.getCurrentUse());
				cpo.put("totalUse", inf.getTotalUse());
				cpo.put("checkCount", inf.getCheckCount());
				if (inf.getLastUsed() != null)
					cpo.put("lastUsed", inf.getLastUsed().toEpochMilli());
				
				po.accumulate("entries", cpo);
			}
			
			JSONUtils.ensureArrayPresent(po, "entries");
			jo.accumulate("pools", po);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "pools");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(2);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}