// Copyright 2005, 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;

import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a live ACARS Map.
 * @author Luke
 * @version 3.3
 * @since 1.0
 */

public class MapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Calcualte the settings cookie expiry date
		Date expiryDate = CalendarUtils.adjust(new Date(), 180);
		ctx.setAttribute("cookieExpiry", StringUtils.format(expiryDate, "yyyy, M, d"), REQUEST);
		
		// Create the map center
		GeoPosition gp = new GeoPosition(StringUtils.parse(ctx.getParameter("lat"), 0.0d), StringUtils.parse(ctx.getParameter("lng"), 0.0d));
		try {
			if (ctx.getParameter("lat") == null) {
				String lat = ctx.getCookie("acarsMapLat").getValue();
				String lng = ctx.getCookie("acarsMapLng").getValue();
				gp = new GeoPosition(Double.parseDouble(lat), Double.parseDouble(lng));
			}
		} catch (Exception e) {
			gp = new GeoPosition(SystemData.getDouble("acars.livemap.lat", 40), SystemData.getDouble("acars.livemap.lng", -85));
		}

		// Save the map center
		ctx.setAttribute("mapCenter", gp, REQUEST);
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);

		// Set the cache
		ctx.setExpiry(120);
		
		// Check if we're retrieving this from the ACARS client
		boolean isACARSClient = Boolean.valueOf(ctx.getParameter("acarsClient")).booleanValue();
		ctx.setAttribute("isDispatch", Boolean.valueOf(ctx.getParameter("dispatchClient")), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL(isACARSClient ? "/jsp/acars/acarsClientMap.jsp" : "/jsp/acars/acarsMap.jsp");
		result.setSuccess(true);
	}
}