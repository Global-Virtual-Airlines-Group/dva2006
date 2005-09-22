// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ACARSFlightReport;
import org.deltava.beans.acars.FlightInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to delete ACARS Flight Info entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightInfoDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the flight IDs
		String IDs[] = ctx.getRequest().getParameterValues("flightID");
		List flightIDs = new ArrayList();
		if (IDs == null) {
			flightIDs.add(ctx.getParameter("id"));
		} else {
			flightIDs.addAll(Arrays.asList(IDs));			
		}

		Set deletedIDs = new HashSet();
		Set skippedIDs = new HashSet();
		try {
			Connection con = ctx.getConnection();

			// Get the DAOs
			GetACARSLog dao = new GetACARSLog(con);
			GetFlightReports frdao = new GetFlightReports(con);
			SetACARSLog wdao = new SetACARSLog(con);

			// Start the transaction
			ctx.startTX();

			// Delete the connection entries
			for (Iterator i = flightIDs.iterator(); i.hasNext();) {
				int id = Integer.parseInt((String) i.next());

				// Get the flight information entry - make sure the flight doesn't have a PIREP linked to it
				FlightInfo info = dao.getInfo(id);
				ACARSFlightReport afr = frdao.getACARS(id);
				if (info == null) {
					skippedIDs.add(StringUtils.formatHex(id));
				} else if (afr != null) {
					skippedIDs.add(StringUtils.formatHex(id));
				} else {
					wdao.deleteInfo(info.getID());
					deletedIDs.add(StringUtils.formatHex(id));
				}
			}

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attributes
		ctx.setAttribute("infoDelete", Boolean.TRUE, REQUEST);
		ctx.setAttribute("deletedIDs", deletedIDs, REQUEST);
		ctx.setAttribute("skippedIDs", skippedIDs, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/acars/logEntryDelete.jsp");
		result.setSuccess(true);
	}
}