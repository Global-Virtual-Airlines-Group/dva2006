// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;

import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a live ACARS Map.
 * @author Luke
 * @version 2.2
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
		GeoPosition gp = null;
		try {
			String lat = ctx.getCookie("acarsMapLat").getValue();
			String lng = ctx.getCookie("acarsMapLng").getValue();
			gp = new GeoPosition(Double.parseDouble(lat), Double.parseDouble(lng));
		} catch (Exception e) {
			gp = new GeoPosition(SystemData.getDouble("acars.livemap.lat", 40), SystemData.getDouble("acars.livemap.lng", -85));
		}

		// Save the map center
		ctx.setAttribute("mapCenter", gp, REQUEST);

		// Set the cache
		ctx.getCache().setMaxAge(120);
		
		// Check if we're retrieving this from the ACARS client
		boolean isACARSClient = Boolean.valueOf(ctx.getParameter("acarsClient")).booleanValue();
		ctx.setAttribute("isDispatch", Boolean.valueOf(ctx.getParameter("dispatchClient")), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL(isACARSClient ? "/jsp/acars/acarsClientMap.jsp" : "/jsp/acars/acarsMap.jsp");
		result.setSuccess(true);
	}
}