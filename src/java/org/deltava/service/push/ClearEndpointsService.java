// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.push;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.json.*;

import org.deltava.beans.*;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to clear all Push Notification subscriptions for a user.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class ClearEndpointsService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		int cnt = 0;
		try {
			Connection con = ctx.getConnection();
			
			// Get the subs - ensure this is read from cache, not session
			ctx.startTX();
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID());
			if (p == null)
				return SC_NOT_FOUND;
			
			// Clear the subs
			SetPilotPush ppwdao = new SetPilotPush(con);
			for (PushEndpoint ep : p.getPushEndpoints()) {
				ppwdao.delete(p.getID(), ep.getURL());
				cnt++;
			}
				
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create the JSON object
		JSONObject jo = new JSONObject();
		jo.put("isUnsubscribed", true);
		jo.put("count", cnt);
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
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