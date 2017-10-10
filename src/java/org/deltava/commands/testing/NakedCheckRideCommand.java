// Copyright 2005, 2006, 2007, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.hr.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to assign Check Rides not linked to a Transfer Request.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class NakedCheckRideCommand extends AbstractTestHistoryCommand {

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

		// Get command result
		CommandResult result = ctx.getResult();
		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot we are assigning the Check Ride to
			GetPilot pdao = new GetPilot(con);
			p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// Check if we already have a pending checkride
			boolean hasRide = false;
			GetExam exdao = new GetExam(con);
			Collection<CheckRide> cRides = exdao.getCheckRides(p.getID());
			for (CheckRide cr : cRides) {
				if (cr.getStatus() != TestStatus.SCORED) {
					hasRide = true;
					ctx.setAttribute("checkRide", cr, REQUEST);
					break;
				}
			}
			
			// Check if we already have a pending transfer request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest tx = txdao.get(p.getID());
			if (tx != null) {
				hasRide = true;
				ctx.setAttribute("tx", tx, REQUEST);
			}

			// Save the pilot in the request
			ctx.setAttribute("pilot", p, REQUEST);

			// If we already have a pending checkride, then send back an error
			if (hasRide) {
				ctx.release();
				ctx.setAttribute("isRideAlreadyAssigned", Boolean.TRUE, REQUEST);

				// Forward to the JSP
				result.setURL("/jsp/testing/cRideUpdate.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Check if we can assign the ride
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			if (!access.getCanAssignRide())
				throw securityException("Cannot assign Check Ride");

			// Get all equipment programs and their aircraft
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<EquipmentType> eqTypes = eqdao.getActive();
			Map<String, Collection<String>> allEQ = new HashMap<String, Collection<String>>();
			eqTypes.forEach(eq -> allEQ.put(eq.getName(), eq.getPrimaryRatings()));
			ctx.setAttribute("eqTypes", eqTypes, REQUEST);
			ctx.setAttribute("eqAircraft", allEQ, REQUEST);
			
			// If we are not doing a POST, then redirect
			if (ctx.getParameter("eqType") == null) {
				ctx.release();

				// Forward to the JSP
				result.setURL("/jsp/testing/cRideAssign.jsp");
				result.setSuccess(true);
				return;
			}

			// Get the equipment type for the Check Ride
			EquipmentType eqType = eqdao.get(ctx.getParameter("eqType"));
			if (eqType == null)
				throw notFoundException("Invalid Equipment Program - " + ctx.getParameter("eqType"));

			// Make sure the assigned type is part of the primary ratings
			String acType = ctx.getParameter("crType");
			if (!eqType.getPrimaryRatings().contains(acType)) {
				ctx.release();
				ctx.setAttribute("eqType", eqType, REQUEST);

				// Forward to the JSP
				result.setURL("/jsp/testing/cRideAssign.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Check if this is recurrent or initial
			RideType rt = RideType.CHECKRIDE;
			if (p.getProficiencyCheckRides()) {
				TestingHistoryHelper history = initTestHistory(p, con);
				if (history.hasCheckRide(eqType, RideType.CHECKRIDE))
					rt = RideType.CURRENCY;
			}
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("RIDEASSIGN"));
			mctxt.addData("pilot", p);
			mctxt.addData("eqType", eqType);

			// Check if we are using the script
			String comments = ctx.getParameter("comments");
			boolean useScript = Boolean.valueOf(ctx.getParameter("useScript")).booleanValue() || (rt == RideType.CURRENCY);
			if (useScript) {
				EquipmentRideScriptKey key = new EquipmentRideScriptKey(eqType.getName(), ctx.getParameter("crType"), (rt == RideType.CURRENCY));
				GetExamProfiles epdao = new GetExamProfiles(con);
				CheckRideScript sc = epdao.getScript(key);
				if (sc != null)
					comments = comments + "\n\n" + sc.getDescription();
			}

			// Create the checkride bean
			CheckRide cr = new CheckRide(acType + " Check Ride");
			cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			cr.setDate(Instant.now());
			cr.setAuthorID(ctx.getID());
			cr.setScorerID(ctx.getUser().getID());
			cr.setStatus(TestStatus.NEW);
			cr.setStage(eqType.getStage());
			cr.setAircraftType(acType);
			cr.setEquipmentType(eqType.getName());
			cr.setType(rt);
			cr.setComments(comments);

			// Write the checkride to the database
			SetExam exwdao = new SetExam(con);
			exwdao.write(cr);

			// Save the checkride in the request
			mctxt.addData("checkRide", cr);
			ctx.setAttribute("checkRide", cr, REQUEST);
		} catch (DAOException de) {
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
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/cRideUpdate.jsp");
		result.setSuccess(true);
	}
}