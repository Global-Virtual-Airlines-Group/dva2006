// Copyright 2005, 2009, 2011, 2012, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.UserData;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.flight.FDRFlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to delete ACARS Flight Info entries.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class FlightInfoDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the flight IDs
		Collection<String> IDs = ctx.getParameters("flightID");
		Collection<String> flightIDs = new LinkedHashSet<String>();
		if (IDs == null)
			flightIDs.add(ctx.getParameter("id"));
		else
			flightIDs.addAll(IDs);			

		Collection<String> deletedIDs = new LinkedHashSet<String>();
		Collection<String> skippedIDs = new LinkedHashSet<String>();
		try {
			Connection con = ctx.getConnection();

			// Get the DAOs
			GetACARSLog dao = new GetACARSLog(con);
			GetFlightReports frdao = new GetFlightReports(con);
			SetACARSLog wdao = new SetACARSLog(con);
			GetUserData uddao = new GetUserData(con);

			// Start the transaction
			ctx.startTX();

			// Delete the connection entries
			for (Iterator<String> i = flightIDs.iterator(); i.hasNext();) {
				int id = StringUtils.parse(i.next(), 0);

				// Get the flight information entry - make sure the flight doesn't have a PIREP linked to it
				FlightInfo info = dao.getInfo(id);
				if (info == null)
					skippedIDs.add(StringUtils.formatHex(id));
				else {
				   UserData uloc = uddao.get(info.getAuthorID());
				   String dbName = (uloc == null) ? ctx.getDB() : uloc.getDB();
				   FDRFlightReport afr = frdao.getACARS(dbName, id);
				   if (afr == null) {
						wdao.deleteInfo(info.getID());
						deletedIDs.add(StringUtils.formatHex(id));
				   } else
				      skippedIDs.add(StringUtils.formatHex(id));   
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
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/acars/logEntryDelete.jsp");
		result.setSuccess(true);
	}
}