// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.push;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.Collection;

import org.json.*;
import org.deltava.beans.Pilot;
import org.deltava.beans.PushEndpoint;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to return push notification subscription status. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class StatusService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Parse the payload
		PushEndpoint pe = null;
		try {
			JSONObject jo = new JSONObject(new JSONTokener(ctx.getBody()));
			pe = new PushEndpoint(ctx.getUser().getID(), jo.getString("endpoint"));
		} catch (Exception e) {
			return SC_BAD_REQUEST;
		}
		
		JSONObject ro = new JSONObject(); final PushEndpoint fpe = pe;
		try {
			GetPilot pdao = new GetPilot(ctx.getConnection());
			Pilot p = pdao.get(ctx.getUser().getID());
			if (p == null)
				return SC_NOT_FOUND;
			
			Collection<PushEndpoint> eps = p.getPushEndpoints();
			ro.put("subscribed", eps.stream().anyMatch(ep -> fpe.equals(ep)));
			ro.put("count", eps.size());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(ro.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
	
	@Override
	public boolean isLogged() {
		return false;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}