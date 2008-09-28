// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ACARSFlightReport;
import org.deltava.beans.UserData;
import org.deltava.beans.acars.FlightInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

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
		Collection<String> IDs = ctx.getParameters("flightID");
		List<String> flightIDs = new ArrayList<String>();
		if (IDs == null) {
			flightIDs.add(ctx.getParameter("id"));
		} else {
			flightIDs.addAll(IDs);			
		}

		Set<String> deletedIDs = new HashSet<String>();
		Set<String> skippedIDs = new HashSet<String>();
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
			for (Iterator i = flightIDs.iterator(); i.hasNext();) {
				int id = Integer.parseInt((String) i.next());

				// Get the flight information entry - make sure the flight doesn't have a PIREP linked to it
				FlightInfo info = dao.getInfo(id);
				if (info == null) {
					skippedIDs.add(StringUtils.formatHex(id));
				} else {
				   UserData uloc = uddao.get(info.getPilotID());
				   String dbName = (uloc == null) ? SystemData.get("airline.db") : uloc.getDB();
				   ACARSFlightReport afr = frdao.getACARS(dbName, id);
				   if (afr == null) {
						wdao.deleteInfo(info.getID());
						deletedIDs.add(StringUtils.formatHex(id));
				   } else {
				      skippedIDs.add(StringUtils.formatHex(id));   
				   }
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