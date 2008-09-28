// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.EquipmentType;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.TransferAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to assign Check Rides.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRideAssignCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(ctx.getID());
			if (txreq == null)
				throw notFoundException("Invalid Transfer Request - " + ctx.getID());
			
			// Check for an existing check ride
			GetExam exdao = new GetExam(con);
			CheckRide cr = exdao.getCheckRide(txreq.getCheckRideID());
			if ((cr != null) && (cr.getStatus() == Test.NEW))
				throw securityException("Check Ride " + txreq.getCheckRideID() + " already exists");

			// Check our access level
			TransferAccessControl access = new TransferAccessControl(ctx, txreq);
			access.validate();
			if (!access.getCanAssignRide())
				throw securityException("Cannot assign Check Ride");

			// Get the Pilot profile
			GetPilot dao = new GetPilot(con);
			p = dao.get(txreq.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot - " + txreq.getID());

			// Get the Equipment Type for the check ride
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(ctx.getParameter("eqType"));
			if (eq == null)
				throw notFoundException("Invalid Equipment Program - " + ctx.getParameter("eqType"));
			
			// Check if we are using the script
			String comments = ctx.getParameter("comments");
			boolean useScript = Boolean.valueOf(ctx.getParameter("useScript")).booleanValue();
			if (useScript) {
			   GetExamProfiles epdao = new GetExamProfiles(con);
			   CheckRideScript sc = epdao.getScript(ctx.getParameter("crType"));
			   if (sc != null)
			      comments = comments + "\n\n" + sc.getDescription();
			}
			
			// Generate the checkride
			cr = new CheckRide(ctx.getParameter("crType") + " Check Ride");
			cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			cr.setDate(new java.util.Date());
			cr.setAircraftType(ctx.getParameter("crType"));
			cr.setEquipmentType(txreq.getEquipmentType());
			cr.setPilotID(ctx.getID());
			cr.setScorerID(ctx.getUser().getID());
			cr.setStatus(Test.NEW);
			cr.setStage(eq.getStage());
			cr.setComments(comments);
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("RIDEASSIGN"));
			mctxt.addData("pilot", p);
			mctxt.addData("eqType", eq);
			mctxt.addData("checkRide", cr);
			
			// Use a SQL Transaction
			ctx.startTX();

			// Write the checkride to the database
			SetExam exwdao = new SetExam(con);
			exwdao.write(cr);

			// Update the transfer request
			txreq.setCheckRideID(cr.getID());
			txreq.setStatus(TransferRequest.ASSIGNED);

			// Save the transfer request
			SetTransferRequest txwdao = new SetTransferRequest(con);
			txwdao.update(txreq);
			
			// Commit the transaction
			ctx.commitTX();

			// Save the checkride in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("checkRide", cr, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(p);

		// Update status for the JSP
		ctx.setAttribute("isAssign", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/cRideUpdate.jsp");
		result.setSuccess(true);
	}
}