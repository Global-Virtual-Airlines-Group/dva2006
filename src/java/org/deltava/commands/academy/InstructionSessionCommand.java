// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to handle Flight Academy instruction sessions. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionSessionCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Init the message context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		Pilot usr = null;
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			Course c = null;
			InstructionSession s = null;
			GetAcademyCourses dao = new GetAcademyCourses(con);
			if (!isNew) {
				// Get the Instruction Session
				s = dao.getSession(ctx.getID());
				if (s == null)
					throw notFoundException("Invalid Session - " + ctx.getID());
				
				// Get the Course
				c = dao.get(s.getCourseID());
				if (c == null)
					throw notFoundException("Invalid Course - " + s.getCourseID());
			} else {
				int courseID = StringUtils.parseHex(ctx.getParameter("courseID"));
				
				// Get the Course
				c = dao.get(courseID);
				if (c == null)
					throw notFoundException("Invalid Course - " + courseID);
				
				// Populate the bean
				s = new InstructionSession(0, c.getID());
				s.setInstructorID(ctx.getUser().getID());
			}
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			if (!access.getCanUpdateProgress())
				throw securityException("Cannot update Instruction Session");
			
			// Load from the request
			s.setStartTime(parseDateTime(ctx, "start"));
			s.setEndTime(parseDateTime(ctx, "end"));
			s.setNoShow(Boolean.valueOf(ctx.getParameter("noShow")).booleanValue());
			s.setRemarks(ctx.getParameter("remarks"));
			
			// Load the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get("INSESSION"));
			mctx.addData("session", s);
			
			// Load the Pilot to send to
			GetPilot pdao = new GetPilot(con);
			usr = pdao.get(c.getPilotID());
			ctx.setAttribute("pilot", usr, REQUEST);
			
			// Save the request
			SetAcademy wdao = new SetAcademy(con);
			wdao.write(s);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Determine if we send the message
		boolean noSend = Boolean.valueOf(ctx.getParameter("noSend")).booleanValue();
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
			if (!isNew) {
				// Get the Instruction Session
				InstructionSession s = dao.getSession(ctx.getID());
				if (s == null)
					throw notFoundException("Invalid Session - " + ctx.getID());

				// Get the Course
				c = dao.get(s.getCourseID());
				if (c == null)
					throw notFoundException("Invalid Course - " + s.getCourseID());
				
				// Save in the request
				ctx.setAttribute("session", s, REQUEST);
			} else {
				int courseID = StringUtils.parseHex((String) ctx.getCmdParameter(OPERATION, "0"));
				
				// Get the Course
				c = dao.get(courseID);
				if (c == null)
					throw notFoundException("Invalid Course - " + courseID);
			}
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			if (!access.getCanUpdateProgress())
				throw securityException("Cannot edit Instruction Session");

			// Save in the request
			ctx.setAttribute("course", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
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
			InstructionSession s = dao.getSession(ctx.getID());
			if (s == null)
				throw notFoundException("Invalid Session - " + ctx.getID());
			
			// Get the Course
			Course c = dao.get(s.getCourseID());
			if (c == null)
				throw notFoundException("Invalid Course - " + s.getCourseID());
			
			// Get our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);
			
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