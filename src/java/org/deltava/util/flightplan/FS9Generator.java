// Copyright 2009, 2010, 2012, 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.io.*;
import java.util.*;

import org.deltava.beans.navdata.*;

import org.deltava.util.*;

/**
 * A Flight Plan Generator for Microsoft Flight Simulator 2004.
 * @author Luke
 * @version 9.1
 * @since 2.4
 */

public class FS9Generator extends MSFSGenerator {
	
	@Override
	public String generate(Collection<NavigationDataBean> waypoints) {
		StringWriter out = new StringWriter();
		try (PrintWriter ctx = new CustomNewlineWriter(out)) {
			ctx.println("[flightplan]");
			ctx.println("AppVersion=9.0.30612");
			ctx.println("title=" + _aD.getICAO() + " to " + _aA.getICAO());
			ctx.println("description=" + _aD.getICAO() + ", " + _aA.getICAO());
			ctx.println("type=IFR");
			ctx.println("routetype=3");
			ctx.println("cruising_altitude=" + _altitude);
			ctx.println("departure_id=" + _aD.getICAO() + ", " + GeoUtils.formatFS9(_aD) + ", +000000.00,");
			ctx.println("departure_name=" + _aD.getName());
			if (_gateD != null) {
				ctx.println("departure_position=" + _gateD.getName());
				ctx.println("departure_lat=" + StringUtils.format(_gateD.getLatitude(), "#0.00000"));
				ctx.println("departure_lng=" + StringUtils.format(_gateD.getLongitude(), "##0.00000"));
				ctx.println("departure_hdg=" + _gateD.getHeading());
			} else
				ctx.println("departure_position=GATE ?");
			
			ctx.println("destination_id=" + _aA.getICAO() + ", " + GeoUtils.formatFS9(_aA) + ", +000000.00,");
			ctx.println("destination_name=" + _aA.getName());
			if (_al != null)
				ctx.println("airline=" + _al.getCode());
			if (_sid != null)
				ctx.println("sid=" + _sid.getCode());
			if (_star != null)
				ctx.println("star=" + _star.getCode());
			if (!StringUtils.isEmpty(_route))
				ctx.println("route=" + _route);

			// Write the route entries
			int waypointIdx = 0;
			for (Iterator<NavigationDataBean> i = waypoints.iterator(); i.hasNext();) {
				NavigationDataBean nd = i.next();
				ctx.print("waypoint." + String.valueOf(waypointIdx++) + "=");

				// Display region only if we have one and it's not the first/last waypoint
				if ((nd.getRegion() != null) && (waypointIdx > 0) && i.hasNext())
					ctx.print(nd.getRegion());

				ctx.print(", ");
				ctx.print(nd.getCode());
				ctx.print(", , ");
				ctx.print(nd.getCode());
				switch (nd.getType()) {
				case AIRPORT:
				case NDB:
				case VOR:
					ctx.print(", ");
					ctx.print(nd.getType().getName().charAt(0));
					ctx.print(", ");
					break;

				default:
					ctx.print(", I, ");
				}

				ctx.print(GeoUtils.formatFS9(nd));
				ctx.println(", +000000.00,");
			}
		}

		return out.toString();
	}
	
	@Override
	public String getEncoding() {
		return "windows-1252";
	}
}