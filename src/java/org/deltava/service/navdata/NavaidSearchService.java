// Copyright 2008, 2009, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to search for navigation aids in a particular area.
 * @author Luke
 * @version 7.3
 * @since 2.1
 */

public class NavaidSearchService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the navaid to search for
		int range = Math.min(1000, StringUtils.parse(ctx.getParameter("range"), 150));
		boolean includeAirports = Boolean.valueOf(ctx.getParameter("airports")).booleanValue();
		
		// Build the location
		GeoLocation loc = new GeoPosition(StringUtils.parse(ctx.getParameter("lat"), 0.0), StringUtils.parse(ctx.getParameter("lng"), 0.0));
		Collection<NavigationDataBean> results = new LinkedHashSet<NavigationDataBean>();
		try {
			GetNavData dao = new GetNavData(ctx.getConnection());
			dao.setQueryMax(1250);
          	results.addAll(dao.getObjects(loc, range));
          	results.addAll(dao.getIntersections(loc, range));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Format navaids
		JSONObject jo = new JSONObject();
		for (NavigationDataBean nd : results) {
			if (includeAirports || (nd.getType() != Navaid.AIRPORT)) {
				JSONObject wo = new JSONObject();
				wo.put("code", nd.getCode());
				wo.put("ll", GeoUtils.toJSON(nd));
				wo.put("color", nd.getIconColor());
				wo.put("pal", nd.getPaletteCode());
				wo.put("icon", nd.getIconCode());
				wo.put("type", nd.getType().getName());
				wo.put("info", nd.getInfoBox());
				jo.accumulate("items", wo);
			}
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(3600);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}