// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.push;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.service.*;

import org.deltava.util.JSONUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to test browser push notifications. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class TestService extends WebService {
	
	private static final List<NotifyActionType> ACTION_TYPES = List.of(NotifyActionType.HOME, NotifyActionType.PILOTCENTER);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		PushEndpoint pe = null; boolean doCurrent = false;
		try {
			JSONObject jo = new JSONObject(new JSONTokener(ctx.getBody()));
			pe = new PushEndpoint(ctx.getUser().getID(), jo.getString("endpoint"));
			doCurrent = jo.optBoolean("doCurrent");
		} catch (Exception e) {
			return SC_BAD_REQUEST;
		}
		
		Pilot p = null; Collection<PushEndpoint> eps = new ArrayList<PushEndpoint>(); 
		try {
			GetPilot pdao = new GetPilot(ctx.getConnection());
			p = pdao.get(ctx.getUser().getID());
			if (p == null)
				return SC_NOT_FOUND;
			
			eps.addAll(p.getPushEndpoints());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create the push message
		JSONObject mo = new JSONObject();
		mo.put("lang", "en");
		mo.put("requireInteraction", true);
		mo.put("title", SystemData.get("airline.name"));
		mo.put("body", "Push Notification Test");
		mo.put("icon", String.format("/%s/favicon/favicon-32x32.png", SystemData.get("path.img")));
		mo.put("url", String.format("https://%s", SystemData.get("airline.url")));
		for (NotifyActionType at : ACTION_TYPES) {
			NotifyAction act = NotifyAction.create(at, Integer.valueOf(p.getID()));
			JSONObject ao = new JSONObject();
			ao.put("title", act.getDescription());
			ao.put("action", at.getURL());
			ao.put("url", act.getURL());
			ao.put("id", p.getID());
			mo.accumulate("actions", ao);
		}

		// Send the push message
		JSONUtils.ensureArrayPresent(mo, "actions"); int sent = 0;
		for (PushEndpoint ep : eps) {
			if (!doCurrent || ep.equals(pe)) {
				VAPIDEnvelope env = new VAPIDEnvelope(ep);
				env.setBody(mo.toString());
				sent++;
				MailerDaemon.push(env);
			}
		}
		
		// Create response object
		JSONObject ro = new JSONObject(); final PushEndpoint fpe = pe;
		ro.put("size", eps.size());
		ro.put("sent", sent);
		ro.put("isSubscribed", eps.stream().anyMatch(ep -> fpe.equals(ep)));
		
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