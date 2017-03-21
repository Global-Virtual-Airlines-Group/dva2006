// Copyright 2006, 2007, 2008, 2009, 2010, 2012, 2014, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Service to display Pilot Locations on a map.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class PilotLocationService extends WebService {
	
	private final Random rnd = new Random();

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		boolean isHR = ctx.isUserInRole("HR");

		// Get active pilots and their locations
		Collection<Pilot> pilots = null;
		Map<Integer, GeoLocation> locations = null;
		try {
			Connection con = ctx.getConnection();
			GetPilot dao = new GetPilot(con);
			GetPilotBoard pbdao = new GetPilotBoard(con);
			locations = pbdao.getActive();
			pilots = dao.getActivePilots(null);
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Loop through the GeoLocations, apply the random adjuster and combine with the Pilot
		JSONArray ja = new JSONArray();
		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext();) {
			Pilot usr = i.next();
			FuzzyPosition fp = (FuzzyPosition) locations.get(Integer.valueOf(usr.getID()));
			if (fp != null) {
				GeoLocation gl = isHR ? fp : GeoUtils.bearingPoint(fp, fp.getH(), rnd.nextInt(360));
					
				// Init the location bean
				PilotLocation loc = new PilotLocation(usr, gl);
				loc.setAllowDelete(isHR);
				
				// Build the element
				JSONObject po = new JSONObject();
				po.put("id", usr.getID());
				po.put("rank", usr.getRank().getName());
				po.put("eqType", usr.getEquipmentType());
				po.put("minZoom", 1);
				po.put("ll", JSONUtils.format(gl));
				po.put("color", loc.getIconColor());
				po.put("info", loc.getInfoBox());
				ja.put(po);
			}
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(1800);
			ctx.println(ja.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	@Override
	public boolean isLogged() {
		return false;
	}
}