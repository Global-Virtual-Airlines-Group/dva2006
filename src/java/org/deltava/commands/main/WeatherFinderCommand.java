// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.beans.wx.METAR;
import org.deltava.beans.flight.ILSCategory;

import org.deltava.commands.*;
import org.deltava.dao.*;

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
		try {
			GetWeather wxdao = new GetWeather(ctx.getConnection());
			Collection<Airport> airports = wxdao.getILSAirports(ILSCategory.CATI);
			
			// Get the METARs
			List<METAR> metars = new ArrayList<METAR>();
			for (Iterator<Airport> i = airports.iterator(); i.hasNext(); ) {
				Airport a = i.next();
				METAR m = wxdao.getMETAR(a.getICAO());
				if (m != null)
					metars.add(m);
			}
			
			ctx.setAttribute("metars", metars, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save request attributes
		ctx.setAttribute("ilsClasses", ILSCATS, REQUEST);
		ctx.setAttribute("mapCenter", SystemData.getAirport(ctx.getUser().getHomeAirport()), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/wxFinder.jsp");
		result.setSuccess(true);
	}
}