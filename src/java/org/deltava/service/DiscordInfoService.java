// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.dao.*;

import org.deltava.util.JSONUtils;

/**
 * A Web Service to display information about a user based on their Discord UUID.
 * @author Luke
 * @version 10.4
 * @since 10.4
 */

public class DiscordInfoService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		String uuid = ctx.getParameter("id"); Pilot p = null;
		try {
			GetPilotDirectory pdao = new GetPilotDirectory(ctx.getConnection());
			p = pdao.getByIMAddress(uuid);
			if (p == null)
				return SC_NOT_FOUND;
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create the JSON object
		JSONObject jo = new JSONObject();
		jo.put("id", p.getID());
		jo.put("uuid", p.getExternalID(ExternalID.DISCORD));
		jo.put("firstName", p.getFirstName());
		jo.put("lastName", p.getLastName());
		jo.put("eqType", p.getEquipmentType());
		jo.put("rank", p.getRank().getName());
		jo.put("pilotCode", p.getPilotCode());
		jo.put("legs", p.getLegs());
		jo.put("hours", p.getHours());
		p.getRoles().forEach(r -> jo.accumulate("roles", r));

		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "roles");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(300);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public boolean isLogged() {
		return false;
	}
}