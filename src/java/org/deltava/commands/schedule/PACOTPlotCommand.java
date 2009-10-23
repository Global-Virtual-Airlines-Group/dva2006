// Copyright 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.navdata.OceanicTrackInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Site Command to display the Pacific Track plotting map.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class PACOTPlotCommand extends AbstractCommand {
	
	private static final Collection<ComboAlias> TYPES = ComboUtils.fromArray(new String[]{"Eastbound", "Westbound"}, 
			new String[] {"E", "W"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		Collection<Date> dates = null;
		try {
			GetOceanicRoute dao = new GetOceanicRoute(ctx.getConnection());
			dao.setQueryMax(31);
			dates = dao.getOceanicTrackDates(OceanicTrackInfo.Type.PACOT);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Convert the dates
		Collection<String> fmtDates = new LinkedHashSet<String>();
		for (Iterator<Date> i = dates.iterator(); i.hasNext(); ) {
			Date dt = i.next();
			fmtDates.add(StringUtils.format(dt, ctx.getUser().getDateFormat()));
		}
		
		// Save the dates in the request
		ctx.setAttribute("dates", fmtDates, REQUEST);
		ctx.setAttribute("trackTypes", TYPES, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/pacotPlot.jsp");
		result.setSuccess(true);
	}
}