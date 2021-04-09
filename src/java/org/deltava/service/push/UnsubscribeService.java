// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.push;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.Collection;
import java.sql.Connection;

import org.json.*;
import org.deltava.beans.Pilot;
import org.deltava.beans.PushEndpoint;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to handle push notification subscription cancelations.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class UnsubscribeService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		PushEndpoint pe = null;
		try {
			JSONObject jo = new JSONObject(new JSONTokener(ctx.getBody()));
			pe = new PushEndpoint(ctx.getUser().getID(), jo.getString("endpoint"));
		} catch (Exception e) {
			return SC_BAD_REQUEST;
		}
		
		JSONObject ro = new JSONObject(); final PushEndpoint fpe = pe;
		try {
			Connection con = ctx.getConnection();
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID());
			if (p == null)
				return SC_NOT_FOUND;
			
			Collection<PushEndpoint> eps = p.getPushEndpoints();
			boolean isSub = eps.stream().anyMatch(ep -> ep.equals(fpe));
			if (isSub) {
				SetPilotPush pnwdao = new SetPilotPush(con);
				pnwdao.delete(pe.getID(), pe.getURL());
				eps.remove(pe);
			}
			
			ro.put("isUnsubscribed", isSub);
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
	public final boolean isSecure() {
		return true;
	}
}