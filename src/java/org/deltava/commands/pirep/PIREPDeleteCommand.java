// Copyright 2005, 2006, 2009, 2010, 2015, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.io.File;
import java.sql.Connection;

import org.deltava.beans.acars.ArchiveHelper;

import org.deltava.beans.flight.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to delete Flight Reports.
 * @author Luke
 * @version 8.1
 * @since 1.0
 */

public class PIREPDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Flight Report
			GetFlightReports dao = new GetFlightReports(con);
			FlightReport fr = dao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());

			// Check our access level
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Flight Report");

			// Forward to the confirmation page
			boolean isOK = Boolean.valueOf("force".equals(ctx.getCmdParameter(OPERATION, null))).booleanValue();
			if (!isOK) {
				ctx.release();
				
				ctx.setAttribute("pirep", fr, REQUEST);
				result.setURL("/jsp/pilot/pirepDeleteConfirm.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Save status
			ctx.setAttribute("isOurs", Boolean.valueOf(fr.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID()), REQUEST);

			// Start a JDBC transaction
			ctx.startTX();

			// Get the DAO and delete the PIREP from the database
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.delete(ctx.getID());
			
			// Update statistics
			if (fr.getStatus() == FlightStatus.OK) {
				SetAggregateStatistics stwdao = new SetAggregateStatistics(con);
				stwdao.update(fr);
			}

			// If this is an ACARS PIREP, delete the data
			if (fr instanceof ACARSFlightReport) {
				SetACARSLog awdao = new SetACARSLog(con);
				awdao.deleteInfo(fr.getDatabaseID(DatabaseID.ACARS));
				
				// Delete position data
				File f = ArchiveHelper.getPositions(fr.getID());
				if (f.exists())
					f.delete();
			}
			
			// Delete online track data
			File of = ArchiveHelper.getOnline(fr.getID());
			if (of.exists())
				of.delete();
			
			// Delete route data
			File rf = ArchiveHelper.getRoute(fr.getID());
			if (rf.exists())
				rf.delete();

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Update the status for the JSP
		ctx.setAttribute("isDeleted", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}