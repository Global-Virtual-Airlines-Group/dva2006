// Copyright 2006, 2010, 2011, 2012, 2014, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.schedule.Aircraft;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to enroll a Pilot in a Flight Academy course.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class EnrollCommand extends AbstractAcademyHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the course name
		String name = ctx.getParameter("courseName");
		Course c = null;
		try {
			Connection con = ctx.getConnection();
			
			// Init the history
			AcademyHistoryHelper academyHistory = initHistory(ctx.getUser(), con);
			
			// Get the Certification
			GetAcademyCertifications cdao = new GetAcademyCertifications(con); 
			Certification cert = cdao.get(name);
			if (cert == null)
				throw notFoundException("Unknown Certification - " + name);
			
			// Make sure we can take the test
			if (!academyHistory.canTake(cert))
				throw securityException("Cannot enroll in " + cert.getName());
			
			// Check if we have enough flights
			int minFlights = SystemData.getInt("academy.minFlights", 10); 
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID());
			if (p.getLegs() < minFlights)
				throw securityException("Must have " + minFlights + " flights to enroll");
			
			// Create the status entry
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			StatusUpdate upd = new StatusUpdate(ctx.getUser().getID(), UpdateType.ACADEMY);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Requested enrollment in " + cert.getName());
			upds.add(upd);
			
			// Convert the Certification into a Course bean
			c = new Course(cert.getName(), ctx.getUser().getID());
			c.setStatus(Status.PENDING);
			c.setStartDate(Instant.now());
			c.setRideCount(cert.getRideCount());
			for (CertificationRequirement req : cert.getRequirements()) {
				CourseProgress cp = new CourseProgress(0, req.getID());
				cp.setText(req.getText());
				cp.setExamName(req.getExamName());
				cp.setAuthorID(ctx.getUser().getID());
				if (academyHistory.passedExam(req.getExamName())) {
					cp.setComplete(true);
					cp.setCompletedOn(c.getStartDate());
				}
				
				c.addProgress(cp);
			}

			// Figure out if we have passed any stage 1 certs; if so, then immediately start
			boolean autoEnroll = cert.getAutoEnroll();
			if (cert.getStage() > 1)
				autoEnroll &= academyHistory.hasAny(1) ;
			
			// Start a transaction
			ctx.startTX();
				
			// If this is an autoenroll stage 1, then get them directly in
			if (autoEnroll) {
				c.setStatus(Status.STARTED);
				upd.setDescription("Enrolled in " + cert.getName());
				
				// Find any ratings we need to give
				GetAircraft acdao = new GetAircraft(con);
				Collection<String> academyEQ = new HashSet<String>();
				for (String r : cert.getRideEQ()) {
					Aircraft ac = acdao.get(r);
					if ((ac != null) && ac.getAcademyOnly() && !p.hasRating(r))
						academyEQ.add(r);
				}

				if (!academyEQ.isEmpty()) {
					SetPilot pwdao = new SetPilot(con);
					p.addRatings(academyEQ);
					pwdao.write(p);
					
					StatusUpdate upd2 = new StatusUpdate(ctx.getUser().getID(), UpdateType.ACADEMY);
					upd2.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Ratings added: " + StringUtils.listConcat(academyEQ, ", ") + " for " + c.getName());
					upds.add(upd2);
				}
			}
			
			// Get the write DAO and save the course
			SetAcademy wdao = new SetAcademy(con);
			wdao.write(c);
			
			// Write the status update
			SetStatusUpdate uwdao = new SetStatusUpdate(con);
			uwdao.write(upds);
			
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
		if (c.getStatus() == Status.STARTED) {
			result.setURL("course", null, c.getID());
			result.setType(ResultType.REDIRECT);
		} else {
			ctx.setAttribute("isPending", Boolean.TRUE, REQUEST);
			result.setURL("/jsp/academy/courseUpdate.jsp");
			result.setType(ResultType.REQREDIRECT);
		}
	}
}