// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to enroll a Pilot in a Flight Academy course.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EnrollCommand extends AbstractAcademyHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the course name
		String name = (String) ctx.getCmdParameter(ID, "");
		
		Course c = null;
		try {
			Connection con = ctx.getConnection();
			
			// Init the history
			initHistory(ctx.getUser(), con);
			
			// Get the DAO and the certification
			GetAcademyCertifications dao = new GetAcademyCertifications(con);
			Certification cert = dao.get(name);
			if (cert == null)
				throw notFoundException("Unknown Certification - " + name);
			
			// Make sure we can take the test
			if (!_academyHistory.canTake(cert))
				throw securityException("Cannot enroll in " + cert.getName());
			
			// Convert the Certification into a Course bean
			c = new Course(cert.getName(), ctx.getUser().getID());
			c.setStatus(Course.PENDING);
			c.setStartDate(new Date());
			for (Iterator<CertificationRequirement> i = cert.getRequirements().iterator(); i.hasNext(); ) {
				CertificationRequirement req = i.next();
				CourseProgress cp = new CourseProgress(0, req.getID());
				cp.setText(req.getText());
				c.addProgress(cp);
			}

			// Figure out if we have passed any stage 1 certs; if so, then immediately start
			if (_academyHistory.hasAny(1))
				c.setStatus(Course.STARTED);
				
			// Get the write DAO and save the course
			SetAcademy wdao = new SetAcademy(con);
			wdao.write(c);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the course in the request
		ctx.setAttribute("course", c, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setSuccess(true);
		if (c.getStatus() == Course.STARTED) {
			result.setURL("course", null, c.getID());
			result.setType(CommandResult.REDIRECT);
		} else {
			ctx.setAttribute("isPending", Boolean.TRUE, REQUEST);
			result.setURL("/jsp/academy/courseUpdate.jsp");
			result.setType(CommandResult.REQREDIRECT);
		}
	}
}