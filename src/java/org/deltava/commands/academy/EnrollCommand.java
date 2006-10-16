// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.StatusUpdate;
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
		String name = ctx.getParameter("courseName");
		
		Course c = null;
		try {
			Connection con = ctx.getConnection();
			
			// Init the history
			AcademyHistoryHelper academyHistory = initHistory(ctx.getUser(), con);
			
			// Get the DAO and the certification
			GetAcademyCertifications dao = new GetAcademyCertifications(con);
			Certification cert = dao.get(name);
			if (cert == null)
				throw notFoundException("Unknown Certification - " + name);
			
			// Make sure we can take the test
			if (!academyHistory.canTake(cert))
				throw securityException("Cannot enroll in " + cert.getName());
			
			// Create the status entry
			StatusUpdate upd = new StatusUpdate(ctx.getUser().getID(), StatusUpdate.ACADEMY);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Requested enrollment in " + cert.getName());
			
			// Convert the Certification into a Course bean
			c = new Course(cert.getName(), ctx.getUser().getID());
			c.setStatus(Course.PENDING);
			c.setStartDate(new Date());
			for (Iterator<CertificationRequirement> i = cert.getRequirements().iterator(); i.hasNext(); ) {
				CertificationRequirement req = i.next();
				CourseProgress cp = new CourseProgress(0, req.getID());
				cp.setText(req.getText());
				cp.setAuthorID(ctx.getUser().getID());
				c.addProgress(cp);
			}

			// Figure out if we have passed any stage 1 certs; if so, then immediately start
			if (academyHistory.hasAny(1) && cert.getAutoEnroll()) {
				c.setStatus(Course.STARTED);
				upd.setDescription("Enrolled in " + cert.getName());
			}
			
			// Start a transaction
			ctx.startTX();
				
			// Get the write DAO and save the course
			SetAcademy wdao = new SetAcademy(con);
			wdao.write(c);
			
			// Write the status update
			SetStatusUpdate uwdao = new SetStatusUpdate(con);
			uwdao.write(upd);
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
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