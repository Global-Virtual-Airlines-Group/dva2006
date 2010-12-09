// Copyright 2006, 2007, 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
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
 * @version 3.4
 * @since 1.0
 */

public class CourseDisposalCommand extends AbstractCommand {
	
	// Operation constants
	private static final String[] OPNAMES = {"start", "abandon", "complete", "restart"};
	private static final int RESTARTED = 3;

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the operation
		String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
		int opCode = StringUtils.arrayIndexOf(OPNAMES, opName);
		if (opCode < 0)
			throw new CommandException("Invalid Operation - " + opName, false);

		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		Collection<Person> usrs = new HashSet<Person>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Course c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Get the Message Template DAO
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			
			// Create the status update bean
			StatusUpdate upd = new StatusUpdate(c.getPilotID(), StatusUpdate.ACADEMY);
			upd.setAuthorID(ctx.getUser().getID());

			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			boolean canExec = false;
			switch (opCode) {
				case Course.STARTED :
					upd.setDescription("Enrolled in " + c.getName());
					canExec = access.getCanStart();
					break;
					
				case RESTARTED:
					upd.setDescription("Requested return to " + c.getName());
					ctx.setAttribute("isRestarted", Boolean.TRUE, REQUEST);
					c.setStartDate(new Date());
					canExec = access.getCanRestart();
					break;
					
				case Course.ABANDONED :
					upd.setDescription("Withdrew from " + c.getName());
					canExec = access.getCanCancel();
					mctx.setTemplate(mtdao.get("COURSECANCEL"));
					ctx.setAttribute("isAbandoned", Boolean.TRUE, REQUEST);
					break;
					
				case Course.COMPLETE :
					upd.setType(StatusUpdate.CERT_ADD);
					upd.setDescription("Obtained " + c.getName() + " Flight Academy certification");
					
					// Get our exams and init the academy helper
					GetExam exdao = new GetExam(con);
					GetAcademyCertifications cdao = new GetAcademyCertifications(con);
					AcademyHistoryHelper helper = new AcademyHistoryHelper(dao.getByPilot(c.getPilotID()), cdao.getAll());
					helper.addExams(exdao.getExams(c.getPilotID()));
					
					// Check our access
					canExec = access.getCanApprove() && helper.hasCompleted(c.getName());
					mctx.setTemplate(mtdao.get("COURSECOMPLETE"));
					ctx.setAttribute("isCompleted", Boolean.TRUE, REQUEST);
					break;
			}
			
			// If we can't execute the command, stop
			if (!canExec)
				throw securityException("Cannot " + opName + " - Not Authorized");

			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(c.getPilotID());
			ctx.setAttribute("pilot", usr, REQUEST);
			mctx.addData("pilot", usr);
			if (ctx.getUser().getID() != c.getPilotID())
				usrs.add(usr);
			
			// Start a transaction
			ctx.startTX();
			
			// Get the DAO and update the course 
			SetAcademy wdao = new SetAcademy(con);
			wdao.setStatus(c.getID(), opCode, c.getStartDate());
			
			// If we're canceling, cancel all Instruction Sessions
			if (opCode == Course.ABANDONED) {
				GetAcademyCalendar cdao = new GetAcademyCalendar(con);
				SetAcademyCalendar cwdao = new SetAcademyCalendar(con);
				
				// Cancel the sessions
				Collection<Integer> IDs = new HashSet<Integer>();
				Collection<InstructionSession> sessions = cdao.getSessions(c.getID());
				for (Iterator<InstructionSession> i = sessions.iterator(); i.hasNext(); ) {
					InstructionSession is = i.next();
					IDs.add(new Integer(is.getInstructorID()));
					is.setStatus(InstructionSession.CANCELED);
					cwdao.write(is);
				}
				
				// Delete any unflown check rides
				GetExam exdao = new GetExam(con);
				SetExam exwdao = new SetExam(con);
				List<CheckRide> rides = exdao.getAcademyCheckRides(c.getID());
				for (CheckRide cr : rides) {
					if (cr.getStatus() == Test.NEW)
						exwdao.delete(cr);
				}
				
				// Load the pilots
				usrs.addAll(pdao.getByID(IDs, "PILOTS").values());
			}
			
			// Write the Status Update
			SetStatusUpdate uwdao = new SetStatusUpdate(con);
			uwdao.write(upd);
			
			// Write Facebook update
			if (!StringUtils.isEmpty(SystemData.get("users.facebook.id")) && (opCode == Course.COMPLETE)) {
				MessageContext fbctxt = new MessageContext();
				fbctxt.addData("user", usr);
				fbctxt.addData("course", c);
				fbctxt.setTemplate(mtdao.get("FBCOURSECOMPLETE"));
				NewsEntry nws = new NewsEntry(fbctxt.getBody());
				
				// Write to user feed or app page
				SetFacebookData fbwdao = new SetFacebookData();
				fbwdao.setWarnMode(true);
				if (usr.hasIM(IMAddress.FBTOKEN)) {
					fbwdao.setToken(usr.getIMHandle(IMAddress.FBTOKEN));
					fbwdao.write(nws);
				} else {
					fbwdao.setAppID(SystemData.get("users.facebook.pageID"));
					fbwdao.setToken(SystemData.get("users.facebook.pageToken"));
					fbwdao.writeApp(nws);
				}
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