// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.flight.HistoryType;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamAccessControl;

/**
 * A Web Site Command to delete Pilot Examinations and Check Rides.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ExamDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we're deleting an exam or a checkride
		boolean isCheckRide = "checkride".equals(ctx.getCmdParameter(Command.OPERATION, null));
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Examination or Check Ride
			GetExam dao = new GetExam(con);
			Test t = isCheckRide ? dao.getCheckRide(ctx.getID()) : dao.getExam(ctx.getID());
			if (t == null)
				throw notFoundException("Invalid " + (isCheckRide ? "Check Ride - " : "Examination - ") + ctx.getID());

			// Get the user data
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(t.getAuthorID());

			// Check our access
			ExamAccessControl access = new ExamAccessControl(ctx, t, ud);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Examination/Check Ride");

			// Start a transaction
			ctx.startTX();

			// Get the write DAO and delete the test
			SetExam wdao = new SetExam(con);
			wdao.delete(t);
			if (!isCheckRide)
				wdao.updateStats((Examination) t);

			// If it's a check ride, find the PIREP
			if (isCheckRide && (t.getStatus() != TestStatus.NEW)) {
				int acarsID = ((CheckRide) t).getFlightID();
				GetFlightReports frdao = new GetFlightReports(con);
				FlightReport fr = frdao.getACARS(ud.getDB(), acarsID);
				if (fr != null) {
					fr.setAttribute(FlightReport.ATTR_CHECKRIDE, false);
					fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Check Ride deleted");
					SetFlightReport frwdao = new SetFlightReport(con);
					frwdao.write(fr, ud.getDB());
				}
			}

			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status for the JSP
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		ctx.setAttribute("isCheckRide", Boolean.valueOf(isCheckRide), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/examUpdate.jsp");
		result.setSuccess(true);
	}
}