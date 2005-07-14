// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.FileUpload;
import org.deltava.beans.testing.CheckRide;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.WriteBuffer;
import org.deltava.mail.*;

import org.deltava.security.command.ExamAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to submit Check Ride data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRideSubmitCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Pilot sendTo = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Check Ride
			GetExam rdao = new GetExam(con);
			CheckRide cr = rdao.getCheckRide(ctx.getID());
			mctxt.addData("checkRide", cr);

			// Check our access level
			ExamAccessControl access = new ExamAccessControl(ctx, cr);
			access.validate();
			if (!access.getCanSubmit())
				throw new CommandSecurityException("Cannot submit Check Ride");

			// Get the Pilot read DAO
			GetPilot prdao = new GetPilot(con);

			// Check if we are grading at the same time (ie. submitted by a staff member)
			if (access.getCanScore()) {
				cr.setComments(ctx.getParameter("comments"));
				cr.setScorerID(ctx.getUser().getID());
				cr.setScoredOn(new java.util.Date());
				cr.setPassFail("1".equals(ctx.getParameter("passFail")));
				
				// Get the message tempate
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("RIDESCORE"));

				// Get the pilot profile
				sendTo = prdao.get(cr.getPilotID());
				ctx.setAttribute("pilot", sendTo, REQUEST);

				// Set status for the JSP
				ctx.setAttribute("isScore", Boolean.valueOf(true), REQUEST);
			}

			// Check if we have an attached video
			FileUpload vidData = ctx.getFile("video");
			if (vidData != null) {
				cr.setFileName(vidData.getName());
				cr.setSubmittedOn(new java.util.Date());

				// Write the video to the filesystem
				WriteBuffer dao = new WriteBuffer(SystemData.get("path.video"), vidData.getName());
				dao.write(vidData.getBuffer());
				
				// Get the assigner profile
				sendTo = prdao.get(cr.getScorerID());
				
				// Get the message tempate
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("RIDESUBMIT"));

				// Set status for the JSP
				ctx.setAttribute("isSubmit", Boolean.valueOf(true), REQUEST);
				ctx.setAttribute("assignedBy", sendTo, REQUEST);
			}
			
			// Use a SQL Transaction
			ctx.startTX();

			// Save the video in the database
			SetExam wdao = new SetExam(con);
			wdao.write(cr);

			// If we're scoring, update the transfer request
			if (access.getCanScore()) {
				GetTransferRequest txdao = new GetTransferRequest(con);
				TransferRequest txreq = txdao.getByCheckRide(cr.getID());
				if (txreq != null) {
					txreq.setStatus(TransferRequest.OK);

					// Write the transfer request
					SetTransferRequest txwdao = new SetTransferRequest(con);
					txwdao.write(txreq);
				}
			}
			
			// Commit the transaction
			ctx.commitTX();

			// Save the checkride in the request
			ctx.setAttribute("checkRide", cr, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send e-mail message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(sendTo);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/testing/cRideUpdate.jsp");
		result.setSuccess(true);
	}
}