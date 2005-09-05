// Copyright 2005 Luke J. Kolin. All Rights Reserved.
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
 * @version 1.0
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
				throw new CommandException("Flight Report Not Found");

			// Check our access level
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();
			if (!access.getCanApprove())
				throw securityException("Cannot approve Flight Report");

			// Get the DAO and the CheckRide
			GetExam crdao = new GetExam(con);
			CheckRide cr = crdao.getCheckRide(fr.getDatabaseID(FlightReport.DBID_PILOT), fr.getEquipmentType());

			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.getByCheckRide(cr.getID());
			if (txreq == null)
				throw new CommandException("Transfer Request not found");

			// Check our access level
			ExamAccessControl crAccess = new ExamAccessControl(ctx, cr);
			crAccess.validate();
			if (!crAccess.getCanScore())
				throw securityException("Cannot score Check Ride");

			// Get the Pilot object
			GetPilot pdao = new GetPilot(con);
			p = pdao.get(fr.getDatabaseID(FlightReport.DBID_PILOT));
			if (p == null)
				throw new CommandException("Unknown Pilot - " + fr.getDatabaseID(FlightReport.DBID_PILOT));

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
			cr.setSubmittedOn(fr.getSubmittedOn());
			cr.setFlightID(fr.getDatabaseID(FlightReport.DBID_ACARS));
			cr.setComments(ctx.getParameter("comments"));
			cr.setStatus(Test.SCORED);

			// Start a JDBC transaction
			ctx.startTX();

			// Get the PIREP write DAO and perform the operation
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.dispose(ctx.getUser(), fr.getID(), FlightReport.OK);

			// Get the CheckRide write DAO and update the checkride
			SetExam ewdao = new SetExam(con);
			ewdao.write(cr);

			// If we are approving the checkride, then approve the transfer request
			if (cr.getPassFail()) {
				txreq.setStatus(TransferRequest.OK);

				// Write the transfer request
				SetTransferRequest txwdao = new SetTransferRequest(con);
				txwdao.write(txreq);
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

			// Invalidate the cached pilot entry
			GetPilot.cache().remove(new Integer(p.getID()));

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