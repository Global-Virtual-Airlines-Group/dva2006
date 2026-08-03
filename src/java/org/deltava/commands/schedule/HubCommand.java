// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to edit Hub Airport data. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class HubCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Hub Airport.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Build the bean
		Hub h = new Hub(SystemData.getAirline(ctx.getParameter("airline")), SystemData.getAirport(ctx.getParameter("airport")));
		h.setDestinationCount(StringUtils.parse(ctx.getParameter("dstCount"), 0));

		// Write to the database
		try {
			SetSchedule wdao = new SetSchedule(ctx.getConnection());
			wdao.write(h);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		ctx.setAttribute("isHub", Boolean.TRUE, REQUEST);
		ctx.setAttribute("hub", h, REQUEST);
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Hub Airport.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get the lookup keys
		List<String> keys = StringUtils.split(ctx.getParameter("id"), "-");
		if ((keys != null) && (keys.size() >= 2)) {
			Airline a = SystemData.getAirline(keys.getFirst());
			Airport ap = SystemData.getAirport(keys.getLast());
			if ((a == null) || (ap == null))
				throw notFoundException("Invalid Airline/Airport", ctx.getParameter("id"));
		
			// Get the Hub
			try {
				GetRawScheduleInfo dao = new GetRawScheduleInfo(ctx.getConnection());
				Collection<Hub> hubs = dao.getHubs();
				Optional<Hub> oh = hubs.stream().filter(h -> h.matches(a, ap)).findAny();
				ctx.setAttribute("hub", oh.orElse(null), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}
		
		// Get current airlines
		ctx.setAttribute("airlines", SystemData.getAirlines().stream().filter(al -> !al.getHistoric()).collect(Collectors.toList()), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/hubEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Airport. <i>NOT IMPLEMENTED - Edits the Hub Airport</i>
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}