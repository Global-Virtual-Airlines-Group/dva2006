// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.system.TransferRequest;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.*;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to approve Flight Reports and Check Rides across Airlines.
 * @author Luke
 * @version 2.7
 * @since 2.0
 */

public class ExternalPIREPApprovalCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());
		
		// Get the checkride approval
		boolean crApproved = Boolean.valueOf(ctx.getParameter("crApprove")).booleanValue();
		Pilot p = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Check Ride
			GetExam crdao = new GetExam(con);
			CheckRide cr = crdao.getCheckRide(ctx.getID());
			if (cr == null)
				throw notFoundException("Invalid Check Ride ID - " + ctx.getID());
			
			// Get the Pilot
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserData ud = uddao.get(cr.getPilotID());
			p = pdao.get(ud);
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.getACARS(ud.getDB(), cr.getFlightID());
			if (fr == null)
				throw notFoundException("Flight Report Not Found");
			
			// Check our access levels
			PIREPAccessControl access = new CrossAppPIREPAccessControl(ctx, fr, cr);
			access.validate();
			if (!access.getCanDispose())
				throw securityException("Cannot score Check Ride");
			
			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.getByCheckRide(cr.getID());
			
			// Get the Message Template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get(crApproved ? "CRPASS" : "CRFAIL"));
			
			// Get the number of approved flights (we load it here since the disposed PIREP will be uncommitted
			int pirepCount = p.getLegs() + 1;
			
			// Set message context objects
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("flightLength", new Double(fr.getLength() / 10.0));
			mctx.addData("flightDate", StringUtils.format(fr.getDate(), "MM/dd/yyyy"));
			mctx.addData("pilot", p);
			
			// Update the checkride
			cr.setScore(crApproved);
			cr.setScoredOn(new Date());
			cr.setScorerID(ctx.getUser().getID());
			cr.setSubmittedOn(fr.getSubmittedOn());
			cr.setFlightID(fr.getDatabaseID(FlightReport.DBID_ACARS));
			cr.setStatus(Test.SCORED);
			if (ctx.getParameter("dComments") != null)
				cr.setComments(ctx.getParameter("dComments"));
			
			// Start a JDBC transaction
			ctx.startTX();
			
			// Get the PIREP write DAO and approve the PIREP
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.dispose(ud.getDB(), ctx.getUser(), fr, FlightReport.OK);
			
			// Update the checkride
			SetExam ewdao = new SetExam(con);
			ewdao.write(cr);
			
			// Archive the Position data
			SetACARSLog acdao = new SetACARSLog(con);
			acdao.archivePositions(fr.getDatabaseID(FlightReport.DBID_ACARS));
			ctx.setAttribute("acarsArchive", Boolean.TRUE, REQUEST);
			
			// If we are approving the checkride, then approve the transfer request
			if (txreq != null) {
				mctx.addData("txReq", txreq);
				if (cr.getPassFail())
					txreq.setStatus(TransferRequest.OK);
				else
					txreq.setStatus(TransferRequest.PENDING);

				// Write the transfer request
				SetTransferRequest txwdao = new SetTransferRequest(con);
				txwdao.update(txreq);
			}
			
			// If we're approving and we have hit a century club milestone, log it
			Map<?, ?> ccLevels = (Map<?, ?>) SystemData.getObject("centuryClubLevels");
			if (ccLevels.containsKey("CC" + pirepCount)) {
				StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.RECOGNITION);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Joined " + ccLevels.get("CC" + pirepCount));

				// Log Century Club name
				ctx.setAttribute("centuryClub", ccLevels.get("CC" + pirepCount), REQUEST);

				// Write the Status Update
				SetStatusUpdate swdao = new SetStatusUpdate(con);
				swdao.write(ud.getDB(), upd);
			}
			
			// Commit the transaction
			ctx.commitTX();

			// Save the flight report/checkride in the request and the Message Context
			ctx.setAttribute("isApprove", Boolean.TRUE, REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("checkRide", cr, REQUEST);
			mctx.addData("pirep", fr);
			mctx.addData("checkRide", cr);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send a notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctx);
		mailer.send(p);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}