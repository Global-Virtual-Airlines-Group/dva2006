// Copyright 2007, 2008, 2009, 2012, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download Online Network IDs.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public class OnlineIDService extends WebService {
	
	/**
	 * Executes the Web Service, returning Pilot names and IDs.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Determine if we format using JSON
		boolean isJSON = Boolean.parseBoolean(ctx.getParameter("json"));
		
		// Get the network
		OnlineNetwork net = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("network"), OnlineNetwork.valueOf(SystemData.get("online.default_network")));
		final Collection<Pilot> pilots = new ArrayList<Pilot>();
		try {
			GetPilotOnline pdao = new GetPilotOnline(ctx.getConnection());
			pilots.addAll(pdao.getPilots(net));
		} catch (DAOException de) {
			throw new ServiceException(500, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// If we're using XML, init the document
		JSONObject jo = new JSONObject();
		jo.put("network", net.toString());
		if (!isJSON)
			ctx.setContentType("text/plain", "utf-8");
		
		// Write out the pilot list
		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
			Pilot p = i.next();
			if (isJSON) {
				JSONObject po = new JSONObject();
				po.put("id", p.getID());
				po.put("name", p.getName());
				po.put("networkID", p.getNetworkID(net));
				jo.accumulate("pilots", po);
			} else {
				ctx.print(p.getNetworkID(net));
				ctx.print(" ");
				ctx.println(p.getName());
			}
		}
		
		// If we're writing using XML, dump out the document
		if (isJSON) {
			JSONUtils.ensureArrayPresent(jo, "pilots");
			ctx.setContentType("application/json", "utf-8");
			ctx.println(jo.toString());
		}
		
		try {
			ctx.setExpiry(3600);
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}