// Copyright 2008, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.acars.Bandwidth;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to display ACARS bandwidth statistics to a Google chart.
 * @author Luke
 * @version 6.4
 * @since 2.1
 */

public class BandwidthInfoService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get hourly or daily
		boolean isDaily = Boolean.valueOf(ctx.getParameter("daily")).booleanValue();
		List<Bandwidth> stats = null;
		try {
			GetACARSBandwidth bwdao = new GetACARSBandwidth(ctx.getConnection());
			bwdao.setQueryMax(isDaily ? 30 : 24);
			stats = isDaily ? bwdao.getDaily() : bwdao.getHourly();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Generate the JSON document
		Collections.reverse(stats);
		JSONArray ja = new JSONArray();
		for (Bandwidth bw : stats) {
			JSONArray ea = new JSONArray();
			ea.put(StringUtils.format(bw.getDate(), isDaily ? "MMM dd yyyy" : "MMM dd yyyy HH:00"));
			ea.put(bw.getConnections());
			ea.put(bw.getMsgsIn() / 1000.0);
			ea.put(bw.getMsgsOut() / 1000.0);
			ea.put(Math.round(bw.getBytesIn() / 1048.576) / 1000.0) ;
			ea.put(Math.round(bw.getBytesOut() / 1048.576) / 1000.0);
			ea.put(bw.getMaxConnections());
			ea.put(bw.getMaxMsgs() / 1000.0);
			ea.put(Math.round(bw.getMaxBytes() / 1048.576) / 1000.0);
			ea.put(Math.round(bw.getBytesSaved() / 1048.576) / 1000.0);
			ja.put(ea);
		}
		
		// Dump to the output stream
		try {
			ctx.setContentType("text/javascript", "UTF-8");
			ctx.println(ja.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}