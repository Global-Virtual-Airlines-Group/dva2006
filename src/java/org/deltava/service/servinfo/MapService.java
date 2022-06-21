// Copyright 2010, 2012, 2014, 2016, 2017, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;

import org.json.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display an online network map. 
 * @author Luke
 * @version 10.2
 * @since 3.2
 */

public class MapService extends WebService {

	/**
	 * Executes the Web Service, returning ServInfo route data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the network data
		OnlineNetwork net = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("network"), OnlineNetwork.VATSIM);
		NetworkInfo info = ServInfoHelper.getInfo(net);
		
		// Populate pilot IDs if required
		if (!info.hasPilotIDs()) {
			try {
				GetPilotOnline dao = new GetPilotOnline(ctx.getConnection());
				Map<String, Integer> idMap = dao.getIDs(net);
				info.setPilotIDs(idMap);
			} catch (DAOException de) {
				throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
			} finally {
				ctx.release();
			}
		}
		
		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("date", info.getValidDate().toEpochMilli());
		
		// Display the pilots
		List<?> codes = (List<?>) SystemData.getObject("online.highlightCodes");
		for (Pilot usr : info.getPilots()) {
			for (Iterator<?> ci = codes.iterator(); (ci.hasNext() && !usr.isHighlighted()); ) {
				String code = (String) ci.next();
				if (usr.getCallsign().startsWith(code))
					usr.setHighlighted(true);
			}
			
			JSONObject po = new JSONObject();
			po.put("id", usr.getID());
			po.put("callsign", usr.getCallsign());
			po.put("ll", JSONUtils.format(usr));
			po.put("color", usr.getIconColor());
			po.put("info", usr.getInfoBox());
			jo.append("pilots", po);
		}
		
		// Display the controllers if required
		boolean doATC = Boolean.parseBoolean(ctx.getParameter("atc"));
		if (doATC) {
			for (Iterator<Controller> i = info.getControllers().iterator(); i.hasNext(); ) {
				Controller usr = i.next();
				if ((usr.getPosition() == null) || ((usr.getFacility() != Facility.FSS) && (usr.getFacility() != Facility.CTR) && (usr.getFacility() != Facility.APP)))
					continue;

				JSONObject ao = new JSONObject();
				ao.put("id", usr.getID());
				ao.put("callsign", usr.getCallsign());
				ao.put("type", String.valueOf(usr.getFacility()));
				ao.put("ll", JSONUtils.format(usr));
				ao.put("color", usr.getIconColor());	
				ao.put("range", usr.getFacility().getRange());
				ao.put("info", usr.getInfoBox());
				jo.append("atc", ao);
			}
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "pilots", "atc");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(30);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
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