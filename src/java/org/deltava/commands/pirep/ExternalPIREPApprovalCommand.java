// Copyright 2007, 2009, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.Collection;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.acars.ACARSRouteEntry;
import org.deltava.beans.flight.*;
import org.deltava.beans.hr.TransferRequest;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to approve Flight Reports and Check Rides across Airlines.
 * @author Luke
 * @version 7.0
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
			mctx.setTemplate(mtdao.get(crApproved ? "CRPASS" : "CRFAIL"));
			
			// Get the number of approved flights (we load it here since the disposed PIREP will be uncommitted)
			/* int pirepCount = p.getLegs();
			if (crApproved)
				pirepCount++; */
			
			// Set message context objects
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("flightLength", new Double(fr.getLength() / 10.0));
			mctx.addData("flightDate", StringUtils.format(fr.getDate(), "MM/dd/yyyy"));
			mctx.addData("pilot", p);
			
			// Update the checkride
			cr.setScore(crApproved);
			cr.setScoredOn(Instant.now());
			cr.setScorerID(ctx.getUser().getID());
			cr.setSubmittedOn(fr.getSubmittedOn());
			cr.setFlightID(fr.getDatabaseID(DatabaseID.ACARS));
			cr.setStatus(TestStatus.SCORED);
			if (ctx.getParameter("dComments") != null)
				fr.setComments(ctx.getParameter("dComments"));
			
			// Start a JDBC transaction
			ctx.startTX();
			
			// Get the PIREP write DAO and approve the PIREP
			int pirepStatus = crApproved ? FlightReport.OK : FlightReport.REJECTED;
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
				txreq.setStatus(cr.getPassFail() ? TransferRequest.OK : TransferRequest.PENDING);

				// Write the transfer request
				SetTransferRequest txwdao = new SetTransferRequest(con);
				txwdao.update(txreq);
			}
			
			// If we're approving and we have hit a century club milestone, log it
			// FIXME: Accomplishments do not work across airlines
			
			// Commit the transaction
			ctx.commitTX();

			// Save the flight report/checkride in the request and the Message Context
			ctx.setAttribute("isApprove", Boolean.valueOf(crApproved), REQUEST);
			ctx.setAttribute("isReject", Boolean.valueOf(!crApproved), REQUEST);
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