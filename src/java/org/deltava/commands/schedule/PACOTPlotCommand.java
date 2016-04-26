// Copyright 2007, 2008, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.navdata.OceanicTrackInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Site Command to display the Pacific Track plotting map.
 * @author Luke
 * @version 7.0
 * @since 2.6
 */

public class PACOTPlotCommand extends AbstractCommand {
	
	private static final Collection<ComboAlias> TYPES = ComboUtils.fromArray(new String[]{"Eastbound", "Westbound"}, new String[] {"E", "W"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		Collection<Instant> dates = null;
		try {
			GetOceanicRoute dao = new GetOceanicRoute(ctx.getConnection());
			dao.setQueryMax(31);
			dates = dao.getOceanicTrackDates(OceanicTrackInfo.Type.PACOT);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the dates in the request
		Collection<String> fmtDates = dates.stream().map(dt -> StringUtils.format(dt, ctx.getUser().getDateFormat())).collect(Collectors.toList());
		ctx.setAttribute("dates", fmtDates, REQUEST);
		ctx.setAttribute("trackTypes", TYPES, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/pacotPlot.jsp");
		result.setSuccess(true);
	}
}