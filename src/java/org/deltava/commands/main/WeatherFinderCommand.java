// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;
import org.deltava.beans.wx.METAR;
import org.deltava.beans.flight.ILSCategory;

import org.deltava.comparators.GeoComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to find airports with poor visibility. 
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class WeatherFinderCommand extends AbstractCommand {
	
	private static final Collection<ILSCategory> ILSCATS = Arrays.asList(ILSCategory.values()).subList(1, ILSCategory.CATIIIc.ordinal() + 1);

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the minimum criteria
		ILSCategory ils = ILSCategory.get(ctx.getParameter("ils"));
		if (ils == ILSCategory.NONE)
			ils = ILSCategory.CATII;
		
		// Get map center
		double lat = StringUtils.parse(ctx.getParameter("lat"), 0.0d);
		double lng = StringUtils.parse(ctx.getParameter("lng"), 0.0d);
		GeoLocation mapCenter = new GeoPosition(lat, lng);
		if ((lat < 0.001) && (lng < 0.001))
			mapCenter = SystemData.getAirport(ctx.getUser().getHomeAirport());
		
		// Get maximum results
		List<METAR> metars = new ArrayList<METAR>();
		int maxResults = Math.min(200, StringUtils.parse(ctx.getParameter("maxResults"), 60));
		try {
			GetWeather wxdao = new GetWeather(ctx.getConnection());
			wxdao.setQueryMax(maxResults);
			Collection<Airport> airports = wxdao.getILSAirports(ils);
			
			// Get the METARs
			for (Iterator<Airport> i = airports.iterator(); i.hasNext(); ) {
				Airport a = i.next();
				METAR m = wxdao.getMETAR(a.getICAO());
				if (m != null)
					metars.add(m);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Sort and save the METARs
		Collections.sort(metars, new GeoComparator(mapCenter));
		ctx.setAttribute("metars", metars, REQUEST);
		
		// Save request attributes
		ctx.setAttribute("ils", ils, REQUEST);
		ctx.setAttribute("ilsClasses", ILSCATS, REQUEST);
		ctx.setAttribute("mapCenter", mapCenter, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/wxFinder.jsp");
		result.setSuccess(true);
	}
}