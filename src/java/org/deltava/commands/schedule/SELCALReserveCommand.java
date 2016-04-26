// Copyright 2005, 2006, 2007, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;
import java.time.ZonedDateTime;

import org.deltava.beans.schedule.SelectCall;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to reserve and free SELCAL codes.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class SELCALReserveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
				if (rSC.size() > maxCodes)
					throw new CommandException("Cannot reserve more than " + maxCodes + " SELCAL codes", false);

				// Reserve the code
				ZonedDateTime zdt = ZonedDateTime.now();
				sc.setReservedOn(zdt.toInstant());
				wdao.reserve(code, ctx.getUser().getID());

				// Save and calculate the release date
				ctx.setAttribute("releaseDate", zdt.plusDays(SystemData.getInt("users.selcal.reserve", 7)).toInstant(), REQUEST);
				ctx.setAttribute("isReserve", Boolean.TRUE, REQUEST);
				ctx.setAttribute("codes", Integer.valueOf(rSC.size() + 1), REQUEST);
			} else {
				if ((sc.getReservedBy() != ctx.getUser().getID()) && !ctx.isUserInRole("HR"))
					throw new CommandException(sc.getAircraftCode() + " not reserved by " + ctx.getUser().getName(), false);

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