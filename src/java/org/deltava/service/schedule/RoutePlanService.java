// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to create Microsoft Flight Simulator flight plans. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class RoutePlanService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		routePoints.add(new AirportLocation(aD));
		try {
			GetNavRoute dao = new GetNavRoute(ctx.getConnection());
			
			// Load the SID
			TerminalRoute sid = dao.getRoute(ctx.getParameter("sid"));
			if (sid != null)
				routePoints.addAll(sid.getWaypoints());
			
			// Add the route waypoints
			if (!StringUtils.isEmpty(ctx.getParameter("route"))) {
				List<NavigationDataBean> points = dao.getRouteWaypoints(ctx.getParameter("route"), aD);
				routePoints.addAll(points);
			}
			
			// Load the STAR
			TerminalRoute star = dao.getRoute(ctx.getParameter("star"));
			if (star != null)
				routePoints.addAll(star.getWaypoints());
			
			// Add the destination airport
			routePoints.add(new AirportLocation(aA));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Define XML document in case we're FSX
		Document doc;
		Element re;
		
		// Determine simulator version and write flight plan header
		boolean isFSX = "FSX".equals(ctx.getParameter("simVersion"));
		if (isFSX) {
			doc = new Document();
			re = new Element("SimBase.Document");
			re.setAttribute("Type", "AceXML");
			re.setAttribute("version", "1,0");
			doc.setRootElement(re);
			
			
		} else {
			ctx.println("[flightplan]");
			ctx.println("AppVersion=9.0.30612");
			ctx.println("title=" + aD.getICAO() + " to " + aA.getICAO());
			ctx.println("description=" + aD.getICAO() + ", " + aA.getICAO());
			ctx.println("type=IFR");
			ctx.println("routetype=0");
			ctx.println("cruising_altitude=35000");
		}
		
		// Write the route entries
		int waypointIdx = 0;
		for (Iterator<NavigationDataBean> i = routePoints.iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
			if (isFSX) {
				
				
				
			} else {
				
				
				
			}
			
		}
		
		
		// TODO Auto-generated method stub
		return 0;
	}
}