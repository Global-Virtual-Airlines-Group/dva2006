// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class RouteMapService extends MapPlotService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		List<NavigationDataBean> routePoints = null;
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			routePoints = dao.getRouteWaypoints(ctx.getParameter("route"), null);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Convert to an XML document
		Document doc = formatPoints(routePoints, false);
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}
}