// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Testing Center.
 * @author Luke
 * @version 1.0
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
			initTestHistory(usr, con);
			_testHistory.setDebug(ctx.isSuperUser());
			boolean examsLocked = _testHistory.isLockedOut(SystemData.getInt("testing.lockout"));
			if (examsLocked)
				throw securityException("Testing Center locked out");

			// Get all Examination Profiles
			GetExamProfiles epdao = new GetExamProfiles(con);
			List<ExamProfile> allExams = epdao.getExamProfiles();

			// Check if we have an examination open
			GetExam exdao = new GetExam(con);
			int activeExamID = exdao.getActiveExam(usr.getID());

			// Check if we have a Transfer Request open
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(usr.getID());
			if (txreq != null) {
				log.warn("Pending Transfer Request - no Examinations available for " + usr.getName());
				allExams.clear();
				ctx.setAttribute("txreq", txreq, REQUEST);
			} else if (activeExamID != 0) {
				log.warn("Pending Examination - no Examinations available for " + usr.getName());
				allExams.clear();
				ctx.setAttribute("examActive", new Integer(activeExamID), REQUEST);
			} else {
				// Remove all examinations that we have passed or require a higher stage than us
				for (Iterator<ExamProfile> i = allExams.iterator(); i.hasNext();) {
					ExamProfile ep = i.next();
					if (!_testHistory.canWrite(ep))
						i.remove();
				}
			}

			// Save the remaining exam profiles in the request
			ctx.setAttribute("exams", _testHistory.getExams(), REQUEST);
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