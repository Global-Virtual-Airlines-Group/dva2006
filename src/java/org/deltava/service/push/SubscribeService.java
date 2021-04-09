// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.push;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.Instant;

import org.json.*;

import org.deltava.beans.PushEndpoint;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to handle push notification subscriptions.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class SubscribeService extends WebService {
	
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
			pe.setCreatedOn(Instant.now());
			pe.setAuth(jo.getJSONObject("keys").getString("auth"));
			pe.setPub256DH(jo.getJSONObject("keys").getString("p256dh"));
		} catch (Exception e) {
			return SC_BAD_REQUEST;
		}
		
		try {
			SetPilotPush pwdao = new SetPilotPush(ctx.getConnection());
			pwdao.write(pe);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		JSONObject ro = new JSONObject();
		ro.put("isSubscribed", true);
		
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