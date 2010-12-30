// Copyright 2006, 2007, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.academy.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to assign Check Rides linked to Flight Academy courses.
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public class CourseCheckRideCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command results
		CommandResult result = ctx.getResult();
		
		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Course
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			Course c = cdao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Load the Pilot object
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			p = pdao.get(uddao.get(c.getPilotID()));
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + c.getPilotID());
			
			// Load Check Rides for this Course
			GetExam exdao = new GetExam(con);
			List<CheckRide> exams = exdao.getAcademyCheckRides(c.getID());
			c.setCheckRide(exams.isEmpty() ? null : exams.get(0));
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			if (!access.getCanAssignCheckRide())
				throw securityException("Cannot assign Academy Check Ride");
			
			// Make sure a check ride isn't already assigned
			boolean hasRide = false;
			exams = exdao.getCheckRides(c.getPilotID());
			for (Iterator<CheckRide> i = exams.iterator(); i.hasNext(); ) {
				CheckRide t = i.next();
				if (t.getStatus() != Test.SCORED) {
					hasRide = true;
					ctx.setAttribute("checkRide", t, REQUEST);
				}
			}
			
			// Save request attributes
			ctx.setAttribute("course", c, REQUEST);
			ctx.setAttribute("pilot", p, REQUEST);
			
			// If we already have a pending checkride, then send back an error
			if (hasRide) {
				ctx.release();
				ctx.setAttribute("isRideAlreadyAssigned", Boolean.TRUE, REQUEST);

				// Forward to the JSP
				result.setURL("/jsp/academy/courseUpdate.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Check if we're assigning something to ourselves
			boolean isOurs = (p.getID() == ctx.getUser().getID());
			ctx.setAttribute("isMine", Boolean.valueOf(isOurs), REQUEST);
			
			// Get all aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("actypes", isOurs ? p.getRatings() : acdao.getAircraftTypes(), REQUEST);
			
			// Load the check ride script
			GetAcademyCertifications crtdao = new GetAcademyCertifications(con);
			AcademyRideScript sc = crtdao.getScript(c.getName());
			ctx.setAttribute("rideScript", sc, REQUEST);
			
			// If we're new, forward to the JSP
			if (ctx.getParameter("acType") == null) {
				ctx.release();
				result.setURL("/jsp/academy/crAssign.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Create a new Check Ride bean
			CheckRide cr = new CheckRide(c.getName());
			cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			cr.setDate(new Date());
			cr.setAcademy(true);
			cr.setPilotID(p.getID());
			cr.setScorerID(ctx.getUser().getID());
			cr.setStatus(Test.NEW);
			cr.setComments(isOurs && (sc != null) ? sc.getDescription() : ctx.getParameter("comments"));
			cr.setStage(c.getStage());
			cr.setEquipmentType(SystemData.get("academy.eqType"));
			cr.setAircraftType(ctx.getParameter("acType"));
			cr.setCourseID(c.getID());
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("COURSERIDEASSIGN"));
			mctxt.addData("pilot", p);
			mctxt.addData("course", c);
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
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/academy/courseUpdate.jsp");
		result.setSuccess(true);
	}
}