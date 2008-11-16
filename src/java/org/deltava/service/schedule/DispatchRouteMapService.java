// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom.*;

import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display Dispatch flight routes with SID/STAR/Airway data.
 * @author Luke
 * @version 2.3
 * @since 2.2
 */

public class DispatchRouteMapService extends MapPlotService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		DispatchRoute rt = null;
		try {
			GetACARSRoute rdao = new GetACARSRoute(ctx.getConnection());
			rt = rdao.getRoute(StringUtils.parse(ctx.getParameter("id"), 0));
			if (rt == null)
				throw error(SC_NOT_FOUND, "Invalid Route - " + ctx.getParameter("id"), false);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Convert points to an XML document
		List<NavigationDataBean> points = new ArrayList<NavigationDataBean>(rt.getWaypoints());
		Document doc = formatPoints(points, true);
		Element re = doc.getRootElement();
		re.setAttribute("id", String.valueOf(rt.getID()));

		// Dump the XML to the output stream
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