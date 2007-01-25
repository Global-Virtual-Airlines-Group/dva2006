// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Flight Academy instruction sessions. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionSessionCommand extends AbstractFormCommand {
	
	private static final List STATUSES = ComboUtils.fromArray(InstructionSession.STATUS_NAMES);

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Init the message context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());
		
		// Check if the message should be sent
		boolean noSend = Boolean.valueOf(ctx.getParameter("noSend")).booleanValue();

		Pilot usr = null;
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			Course c = null;
			InstructionSession s = null;
			GetAcademyCourses dao = new GetAcademyCourses(con);
			GetAcademyCalendar cdao = new GetAcademyCalendar(con);
			if (!isNew) {
				// Get the Instruction Session
				s = cdao.getSession(ctx.getID());
				if (s == null)
					throw notFoundException("Invalid Session - " + ctx.getID());
				
				// Get the Course
				c = dao.get(s.getCourseID());
				if (c == null)
					throw notFoundException("Invalid Course - " + s.getCourseID());
			} else {
				int courseID = StringUtils.parseHex(ctx.getParameter("course"));
				
				// Get the Course
				c = dao.get(courseID);
				if (c == null)
					throw notFoundException("Invalid Course - " + courseID);
				
				// Populate the bean
				s = new InstructionSession(0, c.getID());
				s.setInstructorID(Integer.parseInt(ctx.getParameter("instructor")));
				s.setPilotID(c.getPilotID());
			}
			
			// Check our access
			InstructionAccessControl access = new InstructionAccessControl(ctx, s);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot update Instruction Session");
			
			// Load from the request
			s.setInstructorID(Integer.parseInt(ctx.getParameter("instructor")));
			s.setStartTime(parseDateTime(ctx, "start", SystemData.get("time.date_format"), "HH:mm"));
			s.setEndTime(parseDateTime(ctx, "end", SystemData.get("time.date_format"), "HH:mm"));
			s.setStatus(StringUtils.arrayIndexOf(InstructionSession.STATUS_NAMES, ctx.getParameter("status")));
			s.setNoShow(Boolean.valueOf(ctx.getParameter("noShow")).booleanValue());
			s.setComments(ctx.getParameter("remarks"));
			
			// Load the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get("INSESSION"));
			mctx.addData("session", s);
			noSend |= (s.getStatus() == InstructionSession.COMPLETE); 
			
			// Load the Pilot to send to
			GetPilot pdao = new GetPilot(con);
			usr = pdao.get(c.getPilotID());
			ctx.setAttribute("pilot", usr, REQUEST);
			mctx.addData("instructor", pdao.get(s.getInstructorID()));
			
			// Save the request
			SetAcademyCalendar wdao = new SetAcademyCalendar(con);
			wdao.write(s);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Determine if we send the message
		if (!noSend) {
			ctx.setAttribute("emailSent", Boolean.TRUE, REQUEST);
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctx);
			mailer.setCC(ctx.getUser());
			mailer.send(usr);
		}
		
		// Set status attribute
		ctx.setAttribute("isSessionUpdate", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/academy/courseUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			Course c = null;
			GetAcademyCourses dao = new GetAcademyCourses(con);
			GetAcademyCalendar cdao = new GetAcademyCalendar(con);
			if (!isNew) {
				// Get the Instruction Session
				InstructionSession s = cdao.getSession(ctx.getID());
				if (s == null)
					throw notFoundException("Invalid Session - " + ctx.getID());

				// Get the Course
				c = dao.get(s.getCourseID());
				if (c == null)
					throw notFoundException("Invalid Course - " + s.getCourseID());
				
				// Check our access
				InstructionAccessControl access = new InstructionAccessControl(ctx, s);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot update Instruction Session");
				
				// Save in the request
				ctx.setAttribute("session", s, REQUEST);
				
				// Get the user's local time zone
				TZInfo tz = ctx.getUser().getTZ();
				
				// Convert the dates to local time for the input fields
				ctx.setAttribute("startTime", DateTime.convert(s.getStartTime(), tz), REQUEST);
				ctx.setAttribute("endTime", DateTime.convert(s.getEndTime(), tz), REQUEST);
			} else {
				int courseID = StringUtils.parseHex(ctx.getParameter("course"));
				
				// Get the Course
				c = dao.get(courseID);
				if (c == null)
					throw notFoundException("Invalid Course - " + courseID);
				
				// Check our access
				CourseAccessControl access = new CourseAccessControl(ctx, c);
				access.validate();
				if (!access.getCanSchedule())
					throw securityException("Cannot schedule Instruction Session");
			}
			
			// Get Instructor Pilots
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			ctx.setAttribute("instructors", pdao.getByRole("Instructor", SystemData.get("airline.db")), REQUEST);
			ctx.setAttribute("pilot", pdao.get(c.getPilotID()), REQUEST);
			
			// Save in the request
			ctx.setAttribute("course", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set options
		ctx.setAttribute("statuses", STATUSES, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/insSessionEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Instruction Session
			GetAcademyCourses dao = new GetAcademyCourses(con);
			GetAcademyCalendar cdao = new GetAcademyCalendar(con);
			InstructionSession s = cdao.getSession(ctx.getID());
			if (s == null)
				throw notFoundException("Invalid Session - " + ctx.getID());
			
			// Get the Course
			Course c = dao.get(s.getCourseID());
			if (c == null)
				throw notFoundException("Invalid Course - " + s.getCourseID());
			
			// Get the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(s.getPilotID()));
			IDs.add(new Integer(s.getInstructorID()));
			
			// Get the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// Get our access
			InstructionAccessControl access = new InstructionAccessControl(ctx, s);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);
			
			// Get access to the course
			AccessControl cAccess = new CourseAccessControl(ctx, c);
			try {
				cAccess.validate();
				ctx.setAttribute("viewCourse", Boolean.TRUE, REQUEST);
			} catch (AccessControlException ace) {
				ctx.setAttribute("viewCourse", Boolean.FALSE, REQUEST);
			}
			
			// Save in the request
			ctx.setAttribute("session", s, REQUEST);
			ctx.setAttribute("course", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/insSessionView.jsp");
		result.setSuccess(true);
	}
}