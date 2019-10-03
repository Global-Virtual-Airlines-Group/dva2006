// Copyright 2007, 2009, 2010, 2012, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.Collection;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.acars.ACARSRouteEntry;
import org.deltava.beans.flight.*;
import org.deltava.beans.hr.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to approve Flight Reports and Check Rides across Airlines.
 * @author Luke
 * @version 8.7
 * @since 2.0
 */

public class ExternalPIREPApprovalCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());
		
		// Get the checkride / flight approval
		boolean flightApproved = Boolean.valueOf(ctx.getParameter("frApprove")).booleanValue();
		CheckRideScoreOptions scoreAction = CheckRideScoreOptions.values()[StringUtils.parse(ctx.getParameter("crApprove"), 0)];
		boolean isScored = (scoreAction != CheckRideScoreOptions.NONE);
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
			UserData ud = uddao.get(cr.getAuthorID());
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
			if (isScored)
				mctx.setTemplate(mtdao.get((scoreAction == CheckRideScoreOptions.PASS) ? "CRPASS" : "CRFAIL"));
			
			// Get the number of approved flights (we load it here since the disposed PIREP will be uncommitted)
			/* int pirepCount = p.getLegs();
			if (crApproved)
				pirepCount++; */
			
			// Set message context objects
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("flightLength", Double.valueOf(fr.getLength() / 10.0));
			mctx.addData("flightDate", StringUtils.format(fr.getDate(), "MM/dd/yyyy"));
			mctx.addData("pilot", p);
			
			// Update the checkride
			if (isScored) {
				cr.setScore(scoreAction == CheckRideScoreOptions.PASS);
				cr.setScoredOn(Instant.now());
				cr.setScorerID(ctx.getUser().getID());
				cr.setSubmittedOn(fr.getSubmittedOn());
				cr.setFlightID(fr.getDatabaseID(DatabaseID.ACARS));
				cr.setStatus(TestStatus.SCORED);
			}
			
			if (ctx.getParameter("dComments") != null)
				fr.setComments(ctx.getParameter("dComments"));
			
			// Start a JDBC transaction
			ctx.startTX();
			
			// Get the PIREP write DAO and approve the PIREP
			FlightStatus pirepStatus = flightApproved ? FlightStatus.OK : FlightStatus.REJECTED;
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.dispose(ud.getDB(), ctx.getUser(), fr, pirepStatus);
			
			// Update the checkride
			SetExam ewdao = new SetExam(con);
			ewdao.write(cr);
			
			// Archive the Position data
			if (fr instanceof ACARSFlightReport) {
				GetACARSPositions posdao = new GetACARSPositions(con);
				SetACARSArchive acdao = new SetACARSArchive(con);
				int acarsID = fr.getDatabaseID(DatabaseID.ACARS);
				Collection<ACARSRouteEntry> entries = posdao.getRouteEntries(acarsID, false);
				acdao.archive(acarsID, entries);
				ctx.setAttribute("acarsArchive", Boolean.TRUE, REQUEST);
			}
			
			// If we are approving the checkride, then approve the transfer request
			if (txreq != null) {
				mctx.addData("txReq", txreq);
				txreq.setStatus(cr.getPassFail() ? TransferStatus.COMPLETE : TransferStatus.PENDING);

				// Write the transfer request
				SetTransferRequest txwdao = new SetTransferRequest(con);
				txwdao.update(txreq);
			}
			
			// Commit the transaction
			ctx.commitTX();

			// Save the flight report/checkride in the request and the Message Context
			ctx.setAttribute("isApprove", Boolean.valueOf(flightApproved), REQUEST);
			ctx.setAttribute("isReject", Boolean.valueOf(!flightApproved), REQUEST);
			ctx.setAttribute("checkRideScored", Boolean.valueOf(scoreAction != CheckRideScoreOptions.NONE), REQUEST);
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