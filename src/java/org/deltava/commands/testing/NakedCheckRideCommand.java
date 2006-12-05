// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.PilotAccessControl;

/**
 * A Web Site Command to assign Check Rides not linked to a Transfer Request.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NakedCheckRideCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
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
			for (Iterator<CheckRide> i = cRides.iterator(); i.hasNext() && !hasRide;) {
				CheckRide cr = i.next();
				if (cr.getStatus() != Test.SCORED) {
					hasRide = true;
					ctx.setAttribute("checkRide", cr, REQUEST);
				}
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

			// Get all equipment programs
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getAll(), REQUEST);
			
			// Get all aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("actypes", acdao.getAircraftTypes(), REQUEST);

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

			// Check if we are using the script
			String comments = ctx.getParameter("comments");
			boolean useScript = Boolean.valueOf(ctx.getParameter("useScript")).booleanValue();
			if (useScript) {
				GetExamProfiles epdao = new GetExamProfiles(con);
				CheckRideScript sc = epdao.getScript(ctx.getParameter("crType"));
				if (sc != null)
					comments = comments + "\n\n" + sc.getDescription();
			}

			// Create the checkride bean
			CheckRide cr = new CheckRide(acType + " Check Ride");
			cr.setDate(new java.util.Date());
			cr.setPilotID(ctx.getID());
			cr.setScorerID(ctx.getUser().getID());
			cr.setStatus(Test.NEW);
			cr.setStage(eqType.getStage());
			cr.setAircraftType(acType);
			cr.setEquipmentType(eqType.getName());
			cr.setComments(comments);

			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("RIDEASSIGN"));
			mctxt.addData("pilot", p);
			mctxt.addData("eqType", eqType);
			mctxt.addData("checkRide", cr);

			// Write the checkride to the database
			SetExam exwdao = new SetExam(con);
			exwdao.write(cr);

			// Save the checkride in the request
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
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/testing/cRideUpdate.jsp");
		result.setSuccess(true);
	}
}