// Copyright 2005, 2006, 2010, 2011, 2016, 2017, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;
import org.deltava.beans.hr.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.TestComparator;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Testing Center.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class TestingCenterCommand extends AbstractTestHistoryCommand {

	private static final Logger log = LogManager.getLogger(TestingCenterCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the user from the database, to avoid cache
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(ctx.getUser().getID());
			ctx.setAttribute("pilot", usr, REQUEST);

			// Initialize the Testing History
			TestingHistoryHelper testHistory = initTestHistory(usr, con);
			ctx.setAttribute("hasPendingCR", Boolean.valueOf(testHistory.getCheckRides(0).stream().anyMatch(cr -> (cr.getStatus() != TestStatus.SCORED))), REQUEST);
			
			// If we have currency check rides, see what is going to expire
			if (usr.getProficiencyCheckRides()) {
				int expDays = Math.min(30, Math.max(15, SystemData.getInt("testing.currency.validity", 365)));
				Collection<CheckRide> expRides = testHistory.getCheckRides(expDays);
				ctx.setAttribute("expiringRides", expRides, REQUEST);
				ctx.setAttribute("expiryDate", Instant.now().plus(expDays, ChronoUnit.DAYS), REQUEST);
			}

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

			// Save the remaining exam profiles in the request - sort by date rather than scored on
			ctx.setAttribute("exams", CollectionUtils.sort(testHistory.getExams(), new TestComparator(TestComparator.DATE)), REQUEST);
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