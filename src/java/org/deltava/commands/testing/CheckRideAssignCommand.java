// Copyright 2005, 2006, 2007, 2011, 2012, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.hr.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.TransferAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to assign Check Rides.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class CheckRideAssignCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
			CheckRide cr = exdao.getCheckRide(txreq.getLatestCheckRideID());
			if ((cr != null) && (cr.getStatus() == TestStatus.NEW))
				throw securityException("Check Ride " + txreq.getLatestCheckRideID() + " already exists");

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
			
			// Determine the check ride type
			RideType rt = RideType.CHECKRIDE;
			if (p.getProficiencyCheckRides()) {
				TestingHistoryHelper testHelper = initTestHistory(p, con);
				if (testHelper.hasCheckRide(eq, RideType.CHECKRIDE))
					rt = RideType.CURRENCY;
			}
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("RIDEASSIGN"));
			mctxt.addData("pilot", p);
			mctxt.addData("eqType", eq);

			// Check if we are using the script
			String comments = ctx.getParameter("comments");
			boolean useScript = Boolean.valueOf(ctx.getParameter("useScript")).booleanValue();
			if (useScript) {
				EquipmentRideScriptKey key = new EquipmentRideScriptKey(eq.getName(), ctx.getParameter("crType"), false);
				GetExamProfiles epdao = new GetExamProfiles(con);
				CheckRideScript sc = epdao.getScript(key);
				if (sc != null) {
					comments = comments + "\n\n" + sc.getDescription();
					ctx.setAttribute("script", sc, REQUEST);
				}
			}

			// Generate the checkride
			cr = new CheckRide(ctx.getParameter("crType") + " Check Ride");
			cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			cr.setDate(Instant.now());
			cr.setAircraftType(ctx.getParameter("crType"));
			cr.setEquipmentType(txreq.getEquipmentType());
			cr.setAuthorID(ctx.getID());
			cr.setScorerID(ctx.getUser().getID());
			cr.setStatus(TestStatus.NEW);
			cr.setStage(eq.getStage());
			cr.setType(rt);
			cr.setComments(comments);

			// Use a SQL Transaction
			ctx.startTX();

			// Write the checkride to the database
			SetExam exwdao = new SetExam(con);
			exwdao.write(cr);

			// Update the transfer request
			txreq.addCheckRideID(cr.getID());
			txreq.setStatus(TransferStatus.ASSIGNED);

			// Save the transfer request
			SetTransferRequest txwdao = new SetTransferRequest(con);
			txwdao.update(txreq);

			// Commit the transaction
			ctx.commitTX();

			// Save the checkride in the request
			mctxt.addData("checkRide", cr);
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