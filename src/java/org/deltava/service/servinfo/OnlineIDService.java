// Copyright 2007, 2008, 2009, 2012, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download Online Network IDs.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public class OnlineIDService extends JSONDataService {
	
	/**
	 * Executes the Web Service, returning Pilot names and IDs.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the network
		OnlineNetwork net = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("network"), OnlineNetwork.valueOf(SystemData.get("online.default_network")));
		final Collection<Pilot> pilots = new ArrayList<Pilot>();
		try {
			GetPilotOnline pdao = new GetPilotOnline(ctx.getConnection());
			pilots.addAll(pdao.getPilots(net));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Write out the pilot list
		JSONObject jo = new JSONObject();
		jo.put("network", net.toString());
		pilots.stream().map(p -> format(p, net)).forEach(jpo -> jo.accumulate("pilots", jpo));
		
		// Dump out the document
		JSONUtils.ensureArrayPresent(jo, "pilots");
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
}