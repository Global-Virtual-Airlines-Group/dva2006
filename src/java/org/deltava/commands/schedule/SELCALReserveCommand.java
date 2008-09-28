// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.SelectCall;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CalendarUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to reserve and free SELCAL codes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SELCALReserveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the SELCAL code
		String code = (String) ctx.getCmdParameter(ID, "");

		// Check if we are reserving
		boolean isReserve = "reserve".equals(ctx.getCmdParameter(OPERATION, ""));
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the selcal data
			GetSELCAL dao = new GetSELCAL(con);
			SelectCall sc = dao.get(code);
			if (sc == null)
				throw notFoundException("No SELCAL data for " + code);

			SetSELCAL wdao = new SetSELCAL(con);
			if (isReserve) {
				int maxCodes = SystemData.getInt("users.selcal.max", 2);
				Collection<SelectCall> rSC = dao.getReserved(ctx.getUser().getID());
				if (rSC.size() > maxCodes) {
					CommandException ce = new CommandException("Cannot reserve more than " + maxCodes + " SELCAL codes");
					ce.setLogStackDump(false);
					throw ce;
				}

				// Reserve the code
				sc.setReservedOn(new Date());
				wdao.reserve(code, ctx.getUser().getID());

				// Save and calculate the release date
				Date releaseDate = CalendarUtils.adjust(sc.getReservedOn(), SystemData.getInt("users.selcal.reserve"));
				ctx.setAttribute("releaseDate", releaseDate, REQUEST);
				ctx.setAttribute("isReserve", Boolean.TRUE, REQUEST);
				ctx.setAttribute("codes", new Integer(rSC.size() + 1), REQUEST);
			} else {
				if (sc.getReservedBy() != ctx.getUser().getID()) {
					CommandException ce = new CommandException(sc.getAircraftCode() + " not reserved by "
							+ ctx.getUser().getName());
					ce.setLogStackDump(false);
					throw ce;
				}

				// Free the reservation
				wdao.free(sc.getCode());
				ctx.setAttribute("isFree", Boolean.TRUE, REQUEST);
			}

			// Save the SELCAL code in the request
			ctx.setAttribute("sc", sc, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/selcalUpdate.jsp");
		result.setSuccess(true);
	}
}