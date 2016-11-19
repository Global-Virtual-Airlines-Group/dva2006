// Copyright 2006, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to assign an Instructor to a Flight Academy Course.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class CourseAssignCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		Course c = null;
		Collection<EMailAddress> addrs = new ArrayList<EMailAddress>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Check our access
			CourseAccessControl ac = new CourseAccessControl(ctx, c);
			ac.validate();
			if (!ac.getCanAssignInstructor())
				throw securityException("Cannot assign Instructor");
			
			// Load student info
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			addrs.add(pdao.get(uddao.get(c.getPilotID())));
			
			// Parse the instructor ID
			int id = StringUtils.parse(ctx.getParameter("instructor"), 0);
			
			// Get the instructor
			Pilot ins = null;
			if (id != 0) {
				ins = pdao.get(uddao.get(id));
				if (ins == null)
					throw notFoundException("Invalid Pilot ID - " + ctx.getParameter("instructor"));
				else if (!ins.isInRole("Instructor"))
					throw securityException(ins.getName() + " not an Instructor");
			}
			
			// Update the course
			c.setInstructorID(id);
			if (ins != null)
				addrs.add(ins);
			
			// Create a comment
			CourseComment cc = new CourseComment(c.getID(), ctx.getUser().getID());
			cc.setCreatedOn(Instant.now());
			cc.setText((ins == null) ? "Cleared assigned Instructor" : "Assigned " + ins.getName() + " as Instructor");
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("COURSECOMMENT"));
			mctxt.addData("comment", cc);
			mctxt.addData("course", c);
			
			// Start a transaction
			ctx.startTX();
			
			// Save the course
			SetAcademy wdao = new SetAcademy(con);
			wdao.write(c);
			wdao.comment(cc);
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
        // Create the e-mail message
        Mailer mailer = new Mailer(ctx.getUser());
        mailer.setContext(mctxt);
        mailer.send(addrs);
		
		// Forward back to the Course
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("course", null, c.getID());
		result.setSuccess(true);
	}
}