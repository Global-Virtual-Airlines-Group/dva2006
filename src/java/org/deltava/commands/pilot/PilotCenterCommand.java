// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import javax.servlet.http.HttpSession;

import org.deltava.beans.*;
import org.deltava.beans.academy.Course;
import org.deltava.beans.testing.ExamProfile;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Pilot Center.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotCenterCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();

		// Check if we have a invalid address session attribute
		HttpSession s = ctx.getSession();
		Boolean attrValue = (Boolean) s.getAttribute(CommandContext.ADDRINVALID_ATTR_NAME);
		boolean invalidAddr = (attrValue == null) ? false : attrValue.booleanValue();
		if (invalidAddr) {
			result.setType(CommandResult.REDIRECT);
			result.setURL("validate.do");
			result.setSuccess(true);
			return;
		}

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the pilot profile from the database and stuff it in the request and the session
			GetPilot pdao = new GetPilot(con);
			p = pdao.get(ctx.getUser().getID());
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute(CommandContext.USER_ATTR_NAME, p, SESSION);
			ctx.setAttribute("manualPIREP", Boolean.valueOf(p.getACARSRestriction() != Pilot.ACARS_ONLY), REQUEST);

			// Save the pilot location
			ctx.setAttribute("geoLocation", pdao.getLocation(p.getID()), REQUEST);

			// Check our access level
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);

			// Load all PIREPs and save the latest PIREP as a separate bean in the request
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.setQueryMax(10);
			List<FlightReport> results = frdao.getByPilot(p.getID(), "DATE DESC");
			for (Iterator<FlightReport> i = results.iterator(); i.hasNext();) {
				FlightReport fr = i.next();
				if ((fr.getStatus() != FlightReport.DRAFT) && (fr.getStatus() != FlightReport.REJECTED)) {
					ctx.setAttribute("lastFlight", fr, REQUEST);
					break;
				}
			}

			// Get online hours
			GetFlightReportRecognition prdao = new GetFlightReportRecognition(con);
			prdao.getOnlineTotals(p);

			// Get the schedule size
			GetSchedule sdao = new GetSchedule(con);
			ctx.setAttribute("scheduleSize", new Integer(sdao.getFlightCount()), REQUEST);

			// Get the PIREP disposal queue size
			if (ctx.isUserInRole("PIREP"))
				ctx.setAttribute("pirepQueueSize", new Integer(prdao.getDisposalQueueSize()), REQUEST);

			// Get the Assistant Chief Pilots (if any) for the equipment program
			ctx.setAttribute("asstCP", pdao.getPilotsByEQRank(Ranks.RANK_ACP, p.getEquipmentType()), REQUEST);

			// Initialize the testing history helper and check for test lockout
			initTestHistory(p, con);
			_testHistory.setDebug(ctx.isSuperUser());
			ctx.setAttribute("examLockout", Boolean.valueOf(_testHistory.isLockedOut(SystemData
					.getInt("testing.lockout"))), REQUEST);

			// Save the pilot's equipment program and check if we can get promoted to Captain
			ctx.setAttribute("eqType", _testHistory.getEquipmentType(), REQUEST);
			ctx.setAttribute("captPromote", Boolean.valueOf(_testHistory.canPromote(_testHistory.getEquipmentType())),
					REQUEST);

			// Count how many legs completed towards Promtion
			if (Ranks.RANK_FO.equals(p.getRank())) {
				int promoLegs = prdao.getPromotionCount(p.getID(), p.getEquipmentType());
				ctx.setAttribute("isFO", Boolean.TRUE, REQUEST);
				ctx.setAttribute("promoteLegs", new Integer(promoLegs), REQUEST);
			}

			// Get Exam profiles
			GetExamProfiles epdao = new GetExamProfiles(con);
			Map<String, ExamProfile> exams = CollectionUtils.createMap(epdao.getExamProfiles(false), "name");

			// Check if we are trying to switch equipment types
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(p.getID());
			if (txreq == null) {
				// Get all active equipment programs, and see which we can switch to
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Collection<EquipmentType> activeEQ = eqdao.getActive();
				Collection<EquipmentType> needFOExamEQ = new TreeSet<EquipmentType>();
				for (Iterator<EquipmentType> i = activeEQ.iterator(); i.hasNext();) {
					EquipmentType eq = i.next();
					if (!_testHistory.canSwitchTo(eq) && !_testHistory.canRequestCheckRide(eq)) {
						ExamProfile ep = exams.get(eq.getExamName(Ranks.RANK_FO));
						if ((ep != null) && (_testHistory.canWrite(ep)))
							needFOExamEQ.add(eq);

						i.remove();
					}
				}

				// Save the equipment types we can get promoted to
				ctx.setAttribute("eqSwitch", activeEQ, REQUEST);
				ctx.setAttribute("eqSwitchFOExam", needFOExamEQ, REQUEST);
			} else {
				// Check our access
				TransferAccessControl txAccess = new TransferAccessControl(ctx, txreq);
				txAccess.validate();
				ctx.setAttribute("txreq", txreq, REQUEST);
				ctx.setAttribute("txAccess", txAccess, REQUEST);
				
				// Load the checkride if any
				if (txreq.getCheckRideID() != 0) {
					GetExam exdao = new GetExam(con);
					ctx.setAttribute("checkRide", exdao.getCheckRide(txreq.getCheckRideID()), REQUEST);
				}
			}

			// See if we can write any examinations
			Collection<ExamProfile> allExams = exams.values();
			for (Iterator<ExamProfile> i = allExams.iterator(); i.hasNext();) {
				ExamProfile ep = i.next();
				if (!_testHistory.canWrite(ep))
					i.remove();
			}

			// See if we are enrolled in a Flight Academy course
			if (SystemData.getBoolean("academy.enabled")) {
				GetAcademyCourses fadao = new GetAcademyCourses(con);
				List<Course> courses = new ArrayList<Course>(fadao.getByPilot(ctx.getUser().getID()));
				ctx.setAttribute("courses", courses, REQUEST);
				if (!courses.isEmpty()) {
					Course c = courses.get(courses.size() - 1);
					if (c.getStatus() == Course.STARTED)
						ctx.setAttribute("course", c, REQUEST);
				}

				// Check if we have instruction flights
				GetAcademyCalendar facdao = new GetAcademyCalendar(con);
				facdao.setQueryMax(1);
				boolean hasFlights = !facdao.getFlightCalendar(null, 0, ctx.getUser().getID()).isEmpty();
				ctx.setAttribute("academyInsFlights", Boolean.valueOf(hasFlights || ctx.isUserInRole("HR")), REQUEST);
			}

			// Save the examinations
			ctx.setAttribute("availableExams", allExams, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Figure out the image to display
		Map acImgs = (Map) SystemData.getObject("pcImages");
		if (acImgs != null)
			ctx.setAttribute("acImage", acImgs.get(p.getEquipmentType().toLowerCase()), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/pilot/pilotCenter.jsp");
		result.setSuccess(true);
	}
}