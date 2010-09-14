// Copyright 2005, 2006, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;
import org.deltava.beans.hr.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Testing Center.
 * @author Luke
 * @version 3.3
 * @since 1.0
 */

public class TestingCenterCommand extends AbstractTestHistoryCommand {

	private static final Logger log = Logger.getLogger(TestingCenterCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the user Profile
		Pilot usr = (Pilot) ctx.getUser();
		ctx.setAttribute("pilot", usr, REQUEST);

		try {
			Connection con = ctx.getConnection();

			// Initialize the Testing History
			TestingHistoryHelper testHistory = initTestHistory(usr, con);

			// Get all Examination Profiles
			GetExamProfiles epdao = new GetExamProfiles(con);
			List<ExamProfile> allExams = epdao.getExamProfiles();

			// Check if we have an examination open
			GetExam exdao = new GetExam(con);
			int activeExamID = exdao.getActiveExam(usr.getID());
			
			// Check if we failed an Examination recently
			boolean failedExam = testHistory.isLockedOut(SystemData.getInt("testing.lockout"));

			// Check if we have a Transfer Request open
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(usr.getID());
			if (txreq != null) {
				log.info("Pending Transfer Request - no Examinations available for " + usr.getName());
				allExams.clear();
				ctx.setAttribute("txreq", txreq, REQUEST);
			} else if (activeExamID != 0) {
				log.info("Pending Examination - no Examinations available for " + usr.getName());
				allExams.clear();
				ctx.setAttribute("examActive", Integer.valueOf(activeExamID), REQUEST);
			} else if (failedExam) {
				log.info("Recently failed exam - no Examinations available for " + usr.getName());
				allExams.clear();
				ctx.setAttribute("failedExam", Boolean.TRUE, REQUEST);
			} else {
				// Remove all examinations that we have passed or require a higher stage than us
				for (Iterator<ExamProfile> i = allExams.iterator(); i.hasNext();) {
					try {
						ExamProfile ep = i.next();
						testHistory.canWrite(ep);
					} catch (IneligibilityException ie) {
						i.remove();
					}
				}
			}

			// Save the remaining exam profiles in the request
			ctx.setAttribute("exams", testHistory.getExams(), REQUEST);
			ctx.setAttribute("availableExams", allExams, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/testCenter.jsp");
		result.setSuccess(true);
	}
}