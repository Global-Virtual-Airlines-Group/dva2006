// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.util.stream.Collectors;

import org.json.*;
import org.deltava.beans.navdata.Gate;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.GateUsage;

import org.deltava.comparators.GateComparator;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.JSONUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Gate usage statistics.
 * @author Luke
 * @version 11.0
 * @since 10.6
 */

public class GateUseService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		boolean isDeparture = Boolean.parseBoolean(ctx.getParameter("isDeparture"));
		Airport a = SystemData.getAirport(ctx.getParameter("a"));
		if (a == null)
			return SC_BAD_REQUEST;
		
		JSONObject jo = new JSONObject();
		jo.put("isDeparture", isDeparture);
		jo.put("airport", JSONUtils.format(a));
		try {
			GetGates gdao = new GetGates(ctx.getConnection());
			List<Gate> gates = gdao.getGates(a);
			Collection<Airline> assignedAirlines = gates.stream().flatMap(g -> g.getAirlines().stream()).collect(Collectors.toSet());
			
			// Get usage
			Airport a2 = SystemData.getAirport(ctx.getParameter("a2"));
			RoutePair rp = isDeparture ? RoutePair.of(a, a2) : RoutePair.of(a2, a);
			GateUsage gu = gdao.getUsage(rp, isDeparture, ctx.getDB());
				
			// Combine usage and filter
			gates.forEach(g -> g.setUseCount(gu.getTotalUsage(g.getName())));
			gates.sort(new GateComparator(GateComparator.USAGE).reversed());
			
			// Build JSON object
			jo.put("dayRange", GateUsage.GATE_USAGE_YEARS * 365);
			jo.put("airport", JSONUtils.format(a));
			jo.put("isDeparture", isDeparture);
			for (Gate g : gates) {
				JSONObject go = new JSONObject();
				go.put("name", g.getName());
				go.put("id", g.getUniqueID());
				go.put("zone", g.getZone().getDescription());
				go.put("ll", JSONUtils.format(g));
				
				Collection<Airline> gateAirlines = new LinkedHashSet<Airline>(g.getAirlines());
				gu.getAirlines().stream().filter(al -> !al.getHistoric() && !assignedAirlines.contains(al)).forEach(gateAirlines::add);
				
				int totalUse = 0;
				for (Airline al : gateAirlines) {
					GateUsage au = gu.filter(al);
					JSONObject ao = JSONUtils.format(al);
					int useCount = au.getTotalUsage(g.getName());
					if (useCount > 0) {
						ao.put("useCount", useCount);
						go.accumulate("airlines", ao);
						totalUse += useCount;
					}
				}
				
				go.put("useCount", totalUse);
				go.put("assignedAirlines", (gateAirlines.size() == g.getAirlines().size()));
				JSONUtils.ensureArrayPresent(go, "airlines");
				if (totalUse > 0)
					jo.accumulate("gates", go);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Dump to the output stream
		JSONUtils.ensureArrayPresent(jo, "gates");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(3600);
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
	
	@Override
	public final boolean isLogged() {
		return false;
	}
}