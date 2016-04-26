// Copyright 2006, 2007, 2010, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.bbcode.BBCode;
import org.deltava.util.bbcode.BBCodeHandler;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to assign Check Rides linked to Flight Academy courses.
 * @author Luke
 * @version 7.0
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
			UserData ud = uddao.get(c.getPilotID());
			p = pdao.get(ud);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + c.getPilotID());
			
			// Load Check Rides for this Course
			GetExam exdao = new GetExam(con);
			List<CheckRide> exams = exdao.getAcademyCheckRides(c.getID());
			for (CheckRide cr : exams)
				c.addCheckRide(cr);
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			if (!access.getCanAssignCheckRide())
				throw securityException("Cannot assign Academy Check Ride");
			
			// Make sure a check ride isn't already assigned
			boolean hasRide = false;
			exams = exdao.getCheckRides(c.getPilotID());
			for (CheckRide t : exams) {
				if (t.getStatus() != TestStatus.SCORED) {
					hasRide = true;
					ctx.setAttribute("checkRide", t, REQUEST);
				}
			}
			
			// Get the check ride
			AcademyRideID id = new AcademyRideID(c.getName(), c.getNextCheckRide());
			ctx.setAttribute("rideNumber", Integer.valueOf(id.getIndex()), REQUEST);
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
			
			// Load the check ride script and certification
			GetAcademyCertifications crtdao = new GetAcademyCertifications(con);
			Certification cert = crtdao.get(c.getName());
			AcademyRideScript sc = crtdao.getScript(id);
			ctx.setAttribute("rideScript", sc, REQUEST);
			
			// Get all available aircraft types
			if (!cert.getRideEQ().isEmpty() && isOurs) {
				Collection<String> availableEQ = p.getRatings().stream().filter(eq -> (cert.getRideEQ().contains(eq))).collect(Collectors.toList());
				if (availableEQ.isEmpty())
					throw notFoundException("No available aircraft for Check Ride in " + StringUtils.listConcat(cert.getRideEQ(), ", "));
			
				ctx.setAttribute("actypes", availableEQ, REQUEST);
			} else if (isOurs)
				ctx.setAttribute("actypes", p.getRatings(), REQUEST);
			else {
				GetAircraft acdao = new GetAircraft(con);
				List<String> allEQ = acdao.getAircraftTypes().stream().map(Aircraft::getName).collect(Collectors.toList());
				allEQ = allEQ.stream().filter(eq -> (cert.getRideEQ().isEmpty() || cert.getRideEQ().contains(eq))).collect(Collectors.toList());
				ctx.setAttribute("actypes", allEQ, REQUEST);
			}
			
			if (ctx.getParameter("acType") == null) {
				ctx.release();
				result.setURL("/jsp/academy/crAssign.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Create a new Check Ride bean
			CheckRide cr = new CheckRide(c.getName() + " " + id.getIndex());
			cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			cr.setDate(Instant.now());
			cr.setAcademy(true);
			cr.setAuthorID(p.getID());
			cr.setScorerID(ctx.getUser().getID());
			cr.setStatus(TestStatus.NEW);
			cr.setComments(isOurs && (sc != null) ? sc.getDescription() : ctx.getParameter("comments"));
			cr.setStage(c.getStage());
			cr.setAircraftType(ctx.getParameter("acType"));
			cr.setCourseID(c.getID());
			
			// Set the equipment type
			GetEquipmentType eqdao = new GetEquipmentType(con);
			cr.setEquipmentType(eqdao.getDefault(ud.getDB()));
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("COURSERIDEASSIGN"));
			mctxt.addData("pilot", p);
			mctxt.addData("course", c);
			mctxt.addData("checkRide", cr);

			// Convert BBCode to HTML for email message
			if (sc != null) {
				String desc = sc.getDescription();	
				boolean hasBBCode = ((desc.indexOf('[') > -1) && (desc.indexOf(']') > -1));
				if (hasBBCode) {
					mctxt.getTemplate().setIsHTML(true);
					BBCodeHandler bbHandler = new BBCodeHandler();
					bbHandler.init();
					for (BBCode bb : bbHandler.getAll())
						desc = desc.replaceAll(bb.getRegex(), bb.getReplace());
				}

				mctxt.addData("crScript", desc);
			} else
				mctxt.addData("crScript", cr.getComments());
			
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