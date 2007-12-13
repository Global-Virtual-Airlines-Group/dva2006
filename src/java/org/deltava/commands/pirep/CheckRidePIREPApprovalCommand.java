// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.TransferRequest;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.ExamAccessControl;
import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to approve Flight Reports and Check Rides.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class CheckRidePIREPApprovalCommand extends AbstractCommand {

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

			// Get the DAO and the Flight Report to modify
			GetFlightReports rdao = new GetFlightReports(con);
			FlightReport fr = rdao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Flight Report Not Found");

			// Get the DAO and the CheckRide
			GetExam crdao = new GetExam(con);
			CheckRide cr = crdao.getACARSCheckRide(fr.getDatabaseID(FlightReport.DBID_ACARS));
			if (cr == null)
				cr = crdao.getCheckRide(fr.getDatabaseID(FlightReport.DBID_PILOT), fr.getEquipmentType(), Test.SUBMITTED);
			
			// Get the Pilot object
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserData ud = uddao.get(fr.getDatabaseID(FlightReport.DBID_PILOT));
			p = pdao.get(ud);
			if (p == null)
				throw notFoundException("Unknown Pilot - " + fr.getDatabaseID(FlightReport.DBID_PILOT));

			// Check our access levels
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();
			ExamAccessControl crAccess = new ExamAccessControl(ctx, cr, ud);
			crAccess.validate();
			if (!crAccess.getCanScore())
				throw securityException("Cannot score Check Ride");

			// Validate that we can approve the flight Report, OR its Academy and we can approve the checkride
			boolean canApprove = access.getCanApprove() || (cr.getAcademy() && crAccess.getCanScore());
			if (!canApprove)
				throw securityException("Cannot approve Flight Report");

			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.getByCheckRide(cr.getID());

			// Get the Message Template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get(crApproved ? "CRPASS" : "CRFAIL"));

			// Get the number of approved flights (we load it here since the disposed PIREP will be uncommitted
			int pirepCount = rdao.getCount(p.getID()) + 1;

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

			// Get the PIREP write DAO and perform the operation
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.dispose(SystemData.get("airline.db"), ctx.getUser(), fr, FlightReport.OK);

			// Archive the Position data
			if (fr instanceof ACARSFlightReport) {
				SetACARSLog acdao = new SetACARSLog(con);
				acdao.archivePositions(fr.getDatabaseID(FlightReport.DBID_ACARS));
				ctx.setAttribute("acarsArchive", Boolean.TRUE, REQUEST);
			}

			// Get the CheckRide write DAO and update the checkride
			SetExam ewdao = new SetExam(con);
			ewdao.write(cr);

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
			Map ccLevels = (Map) SystemData.getObject("centuryClubLevels");
			if (ccLevels.containsKey("CC" + pirepCount)) {
				StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.RECOGNITION);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Joined " + ccLevels.get("CC" + pirepCount));

				// Log Century Club name
				ctx.setAttribute("centuryClub", ccLevels.get("CC" + pirepCount), REQUEST);

				// Write the Status Update
				SetStatusUpdate swdao = new SetStatusUpdate(con);
				swdao.write(upd);
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
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}