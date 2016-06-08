// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.time.Instant;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a live ACARS Map.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class MapCommand extends AbstractCommand {
	
	private static final GeoLocation DEFAULT_CTR = new GeoPosition(SystemData.getDouble("acars.livemap.lat", 40), SystemData.getDouble("acars.livemap.lng", -85));

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Calcualte the settings cookie expiry date
		Instant expDate = Instant.now().plusSeconds(86400 * 180);
		ctx.setAttribute("cookieExpiry", StringUtils.format(expDate, "yyyy, M, d"), REQUEST);
		
		// Create the map center
		GeoPosition ctr = new GeoPosition(StringUtils.parse(ctx.getParameter("lat"), DEFAULT_CTR.getLatitude()), StringUtils.parse(ctx.getParameter("lng"), DEFAULT_CTR.getLongitude()));
		try {
			if (ctx.getParameter("lat") == null) {
				String lat = ctx.getCookie("acarsMapLat").getValue();
				String lng = ctx.getCookie("acarsMapLng").getValue();
				ctr = new GeoPosition(StringUtils.parse(lat, DEFAULT_CTR.getLatitude()), StringUtils.parse(lng, DEFAULT_CTR.getLongitude()));
			}
		} catch (Exception e) {
			ctr = new GeoPosition(DEFAULT_CTR);
		}

		// Save the map center
		ctx.setAttribute("mapCenter", ctr, REQUEST);

		// Check if we're retrieving this from the ACARS client
		boolean isACARSClient = Boolean.valueOf(ctx.getParameter("acarsClient")).booleanValue();
		ctx.setAttribute("isDispatch", Boolean.valueOf(ctx.getParameter("dispatchClient")), REQUEST);
		ctx.setAttribute("mapAPIVersion", Integer.valueOf(StringUtils.parse(ctx.getParameter("api"), 3)), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL(isACARSClient ? "/jsp/acars/acarsClientMap.jsp" : "/jsp/acars/acarsMap.jsp");
		result.setSuccess(true);
	}
}