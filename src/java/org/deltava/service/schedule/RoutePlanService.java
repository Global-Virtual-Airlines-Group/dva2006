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
		
		// Get the airports and altitude
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		String alt = ctx.getParameter("cruiseAlt");
		if (StringUtils.parse(alt, -1) < 1000)
			alt = "35000";
		
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
		Document doc = null; Element fpe = null;
		
		// Determine simulator version and write flight plan header
		boolean isFSX = "FSX".equals(ctx.getParameter("simVersion"));
		if (isFSX) {
			doc = new Document();
			Element re = new Element("SimBase.Document");
			re.setAttribute("Type", "AceXML");
			re.setAttribute("version", "1,0");
			doc.setRootElement(re);
			re.addContent(XMLUtils.createElement("Descr", "AceXML Document"));
			
			// Create flight plan info
			fpe = new Element("FlightPlan.FlightPlan");
			fpe.addContent(XMLUtils.createElement("Title", aD.getICAO() + " to " + aA.getICAO()));
			fpe.addContent(XMLUtils.createElement("FPType", "IFR"));
			fpe.addContent(XMLUtils.createElement("CruisingAlt", alt));
			fpe.addContent(XMLUtils.createElement("DepartureID", aD.getICAO()));
			fpe.addContent(XMLUtils.createElement("DepartureLLA", GeoUtils.formatFSX(aD) + ",+" + StringUtils.format(aD.getAltitude(), "000000.00")));
		    fpe.addContent(XMLUtils.createElement("DestinationID", aA.getICAO()));
		    fpe.addContent(XMLUtils.createElement("DestinationLLA", GeoUtils.formatFSX(aA) + ",+" + StringUtils.format(aA.getAltitude(), "000000.00")));
		    fpe.addContent(XMLUtils.createElement("Descr", aD.getICAO() + ", " + aA.getICAO()));		
		    fpe.addContent(XMLUtils.createElement("DeparturePosition", "GATE ?"));
		    fpe.addContent(XMLUtils.createElement("DepartureName", aD.getName()));
		    fpe.addContent(XMLUtils.createElement("DestinationName", aA.getName()));
		    re.addContent(fpe);
		    
		    // Add app version
		    Element ve = new Element("AppVersion");
		    ve.addContent(XMLUtils.createElement("AppVersoinMajor", "10"));
		    ve.addContent(XMLUtils.createElement("AppVersoinBuild", "61472"));
		    fpe.addContent(ve);
		} else {
			ctx.println("[flightplan]");
			ctx.println("AppVersion=9.0.30612");
			ctx.println("title=" + aD.getICAO() + " to " + aA.getICAO());
			ctx.println("description=" + aD.getICAO() + ", " + aA.getICAO());
			ctx.println("type=IFR");
			ctx.println("routetype=0");
			ctx.println("cruising_altitude=" + alt);
			ctx.println("departure_id=" + aD.getICAO() + ", " + GeoUtils.formatFS9(aD) + ", +000000.00,");
			ctx.println("departure_name=" + aD.getName());
			ctx.println("departure_position=GATE ?");
			ctx.println("destination_id=" + aA.getICAO() + ", " + GeoUtils.formatFS9(aA) + ", +000000.00,");
			ctx.println("destination_name=" + aA.getName());
		}
		
		// Write the route entries
		int waypointIdx = 0;
		for (Iterator<NavigationDataBean> i = routePoints.iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
			if (isFSX) {
				Element nde = new Element("ATCWaypoint");
				nde.setAttribute("id", nd.getCode());
				nde.addContent(XMLUtils.createElement("ATCWaypointType", nd.getTypeName()));
				nde.addContent(XMLUtils.createElement("WorldPosition", GeoUtils.formatFSX(nd) + ",+000000.00"));
				Element ie = new Element("ICAO");
				ie.addContent(XMLUtils.createElement("ICAORegion", (nd.getRegion() == null) ? "K7" : nd.getRegion()));
				ie.addContent(XMLUtils.createElement("ICAOIdent", nd.getCode()));
				nde.addContent(ie);
				fpe.addContent(nde);
			} else {
				ctx.print("waypoint." + String.valueOf(waypointIdx) + "=");
				if (nd.getRegion() != null)
					ctx.print(nd.getRegion());
				ctx.print(", ");
				ctx.print(nd.getCode());
				ctx.print(", ,");
				ctx.print(nd.getCode());
				switch (nd.getType()) {
					case NavigationDataBean.AIRPORT:
						ctx.print(", A, ");
						break;
						
					case NavigationDataBean.NDB:
						ctx.print(", N, ");
						break;
						
					case NavigationDataBean.VOR:
						ctx.print(", V, ");
						break;
				
					default:
						ctx.print(", I, ");
				}
				
				ctx.print(GeoUtils.formatFS9(nd));
				ctx.println(", +000000.00,");
			}
			
			waypointIdx++;
		}
		
		// If we're FSX, write the XML document to the output buffer
		if (isFSX) {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("windows-1252");
			ctx.println(XMLUtils.format(doc, "windows-1252"));
		} else
			ctx.getResponse().setContentType("text/plain");
		
		// Flush the output buffer
		ctx.getResponse().setHeader("Content-disposition", "attachment; filename=" + aD.getICAO() +"-" + aA.getICAO() + ".pln");
		try {
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return SC_OK;
	}
}