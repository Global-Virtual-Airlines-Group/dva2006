// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.json.JSONObject;

import org.deltava.beans.*;
import org.deltava.beans.stats.Tour;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.TourAccessControl;

import org.deltava.util.*;

/**
 * A Web Service to display a JSON-serialized Flight Tour.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class TourInfoService extends TourService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		int id = StringUtils.parse(ctx.getParameter("id"), 0); JSONObject jo = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get Tour data
			GetTour dao = new GetTour(con);
			Tour t = dao.get(id, ctx.getDB());
			if (t == null)
				return SC_NOT_FOUND;
			
			// Check our access
			TourAccessControl ac = new TourAccessControl(ctx, t);
			ac.validate();
			if (!ac.getCanRead())
				return SC_FORBIDDEN;
			
			// Get audit info
			GetAuditLog aldao = new GetAuditLog(con);
			Collection<AuditLog> entries =  aldao.getEntries(t);
			Collection<Integer> IDs = entries.stream().map(AuthoredBean::getAuthorID).collect(Collectors.toSet());
			
			// Load author IDs
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> authors = pdao.getByID(IDs, "PILOTS");
			
			// Serialize the object and its audit log
			jo = serialize(t);
			for (AuditLog ae : entries) {
				Pilot p = authors.get(Integer.valueOf(ae.getAuthorID()));
				JSONObject ao = new JSONObject();
				ao.put("date", ae.getDate().toEpochMilli() / 1000);
				ao.put("description", ae.getDescription());
				if (p != null) {
					JSONObject po = new JSONObject();
					po.put("id", p.getID());
					po.put("pilotCode", p.getPilotCode());
					po.put("firstName", p.getFirstName());
					po.put("lastName", p.getLastName());
					ao.put("auhtor", po);
				}
				
				jo.accumulate("revisionLog", ao);
			}
			
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "revisionLog");
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
	public final boolean isLogged() {
		return false;
	}
}