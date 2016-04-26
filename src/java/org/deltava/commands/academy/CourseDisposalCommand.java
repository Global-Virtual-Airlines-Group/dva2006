// Copyright 2006, 2007, 2008, 2010, 2011, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.fb.NewsEntry;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.SetFacebookData;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to change a Flight Academy Course's status.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CourseDisposalCommand extends AbstractCommand {
	
	private static final String[] OPNAMES = {"start", "abandon", "complete", "restart"};

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the operation
		String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
		Status op = Status.COMPLETE;
		try {
			op = Status.values()[StringUtils.arrayIndexOf(OPNAMES, opName, -1)];
		} catch (Exception e) {
			throw new CommandException("Invalid Operation - " + opName, false);
		}

		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		Collection<EMailAddress> usrs = new HashSet<EMailAddress>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Course c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Get the certification
			GetAcademyCertifications cdao = new GetAcademyCertifications(con);
			Certification crt = cdao.get(c.getCode());
			if (crt == null)
				throw notFoundException("Invalid Certification - " + c.getCode());
			
			// Get the Pilot
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserData ud = uddao.get(c.getPilotID());
			Pilot usr = pdao.get(ud);
			
			// Load our exams
			GetExam exdao = new GetExam(con);
			List<CheckRide> rides = exdao.getAcademyCheckRides(c.getID());
			for (CheckRide cr : rides)
				c.addCheckRide(cr);
			
			// Get the Message Template DAO
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			
			// Create the status update bean
			StatusUpdate upd = new StatusUpdate(c.getPilotID(), StatusUpdate.ACADEMY);
			upd.setAuthorID(ctx.getUser().getID());

			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			boolean canExec = false;
			switch (op) {
				case STARTED :
					upd.setDescription("Enrolled in " + c.getName());
					canExec = access.getCanStart();
					break;
					
				case PENDING:
					upd.setDescription("Requested return to " + c.getName());
					ctx.setAttribute("isRestarted", Boolean.TRUE, REQUEST);
					c.setStartDate(Instant.now());
					canExec = access.getCanRestart();
					break;
					
				case ABANDONED :
					upd.setDescription("Withdrew from " + c.getName());
					canExec = access.getCanCancel();
					mctx.setTemplate(mtdao.get("COURSECANCEL"));
					ctx.setAttribute("isAbandoned", Boolean.TRUE, REQUEST);
					break;
					
				case COMPLETE :
					upd.setType(StatusUpdate.CERT_ADD);
					if (crt.getVisible())
						upd.setDescription("Obtained " + c.getName() + " Flight Academy certification");
					else
						upd.setDescription("Completed " + c.getName() + " Flight Academy course");
					
					// Get our exams and init the academy helper
					AcademyHistoryHelper helper = new AcademyHistoryHelper(usr, dao.getByPilot(c.getPilotID()), cdao.getAll());
					helper.addExams(exdao.getExams(c.getPilotID()));
					
					// Check our access
					canExec = access.getCanApprove() && helper.hasCompleted(c.getName());
					mctx.setTemplate(mtdao.get("COURSECOMPLETE"));
					ctx.setAttribute("isCompleted", Boolean.TRUE, REQUEST);
					break;
			}
			
			// If we can't execute the command, stop
			if (!canExec)
				throw securityException("Cannot " + opName + " Course - Not Authorized");

			// Save the pilot
			ctx.setAttribute("pilot", usr, REQUEST);
			mctx.addData("pilot", usr);
			if (ctx.getUser().getID() != c.getPilotID())
				usrs.add(usr);
			
			// Start a transaction
			ctx.startTX();
			
			// Get the DAO and update the course 
			SetAcademy wdao = new SetAcademy(con);
			wdao.setStatus(c.getID(), op, c.getStartDate());
			
			// If we're canceling, cancel all Instruction Sessions
			if (op == Status.ABANDONED) {
				GetAcademyCalendar cldao = new GetAcademyCalendar(con);
				SetAcademyCalendar cwdao = new SetAcademyCalendar(con);
				
				// Cancel the sessions
				Collection<Integer> IDs = new HashSet<Integer>();
				Collection<InstructionSession> sessions = cldao.getSessions(c.getID());
				for (InstructionSession is : sessions) {
					IDs.add(Integer.valueOf(is.getInstructorID()));
					is.setStatus(InstructionSession.CANCELED);
					cwdao.write(is);
				}
				
				// Delete any unflown check rides
				SetExam exwdao = new SetExam(con);
				for (CheckRide cr : rides) {
					if (cr.getStatus() == TestStatus.NEW)
						exwdao.delete(cr);
				}
				
				// Load the pilots
				UserDataMap udm = uddao.get(IDs);
				usrs.addAll(pdao.get(udm).values());
			} else if (op == Status.STARTED) {
				CourseComment cc = new CourseComment(c.getID(), ctx.getUser().getID());
				cc.setCreatedOn(Instant.now());
				cc.setText("Returned to Course");
				wdao.comment(cc);
			}
			
			// Write the Status Update
			SetStatusUpdate uwdao = new SetStatusUpdate(con);
			uwdao.write(ud.getDB(), upd);
			
			// Write Facebook update
			if (!StringUtils.isEmpty(SystemData.get("users.facebook.id")) && (op == Status.COMPLETE) && crt.getVisible()) {
				MessageContext fbctxt = new MessageContext();
				fbctxt.addData("user", usr);
				fbctxt.addData("course", c);
				fbctxt.setTemplate(mtdao.get("FBCOURSECOMPLETE"));
				NewsEntry nws = new NewsEntry(fbctxt.getBody());
				
				// Write to user feed or app page
				SetFacebookData fbwdao = new SetFacebookData();
				fbwdao.setWarnMode(true);
				fbwdao.setAppID(SystemData.get("users.facebook.pageID"));
				fbwdao.setToken(SystemData.get("users.facebook.pageToken"));
				fbwdao.writeApp(nws);
			}
			
			// Commit the transaction
			ctx.commitTX();
			
			// Save the course
			ctx.setAttribute("course", c, REQUEST);
			mctx.addData("course", c);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send a notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctx);
		mailer.send(usrs);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}