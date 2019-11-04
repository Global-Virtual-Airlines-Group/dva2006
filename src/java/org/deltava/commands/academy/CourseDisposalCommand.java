// Copyright 2006, 2007, 2008, 2010, 2011, 2012, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.servinfo.PilotRating;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to change a Flight Academy Course's status.
 * @author Luke
 * @version 9.0
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
			rides.forEach(c::addCheckRide);
			
			// Get the Message Template DAO
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			
			// Create the status update bean
			StatusUpdate upd = new StatusUpdate(c.getPilotID(), (op == Status.COMPLETE) ? UpdateType.CERT_ADD : UpdateType.ACADEMY);
			upd.setAuthorID(ctx.getUser().getID());

			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.setCertification(crt);
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
					if (crt.getVisible())
						upd.setDescription("Obtained " + c.getName() + " Flight Academy certification");
					else
						upd.setDescription("Completed " + c.getName() + " Flight Academy course");
					
					// Get our exams and init the academy helper
					AcademyHistoryHelper helper = new AcademyHistoryHelper(usr, dao.getByPilot(c.getPilotID()), cdao.getAll());
					helper.setDebug(ctx.isSuperUser());
					for (Integer xdbID : ud.getIDs())
						helper.addExams(exdao.getExams(xdbID.intValue()));
					
					// Check our access
					canExec = access.getCanApprove() && helper.hasCompleted(c.getName());
					mctx.setTemplate(mtdao.get("COURSECOMPLETE"));
					ctx.setAttribute("isCompleted", Boolean.TRUE, REQUEST);
					break;
					
				default:
					throw new CommandException("Unknown Operation - " + op);
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
			
			// Send the completion to VATSIM
			if ((op == Status.COMPLETE) && (crt.getNetwork() != null)) {
				ctx.setAttribute("cert", crt, REQUEST);
				PilotRating pr = new PilotRating(StringUtils.parse(usr.getNetworkID(crt.getNetwork()), 0), crt.getNetworkRatingCode());
				pr.setInstructorID(StringUtils.parse(ctx.getUser().getNetworkID(crt.getNetwork()), 0));
				pr.setIssueDate(Instant.now());
				if (crt.getNetwork() == OnlineNetwork.VATSIM) {
					try {
						SetVATSIMData vwdao = new SetVATSIMData();
						vwdao.addRating(pr);
						ctx.setAttribute("networkRatingAdded", Boolean.TRUE, REQUEST);
					} catch (DAOException rde) {
						ctx.setAttribute("networkRatingError", rde, REQUEST);
					}
				}
			}
			
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