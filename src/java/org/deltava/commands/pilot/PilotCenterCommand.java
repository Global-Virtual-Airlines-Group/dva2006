// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.academy.Course;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.ScheduleSearchCriteria;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.acars.ACARSClientInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to display the Pilot Center.
 * @author Luke
 * @version 2.7
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
		Pilot p = null;
		try {
			Connection con = ctx.getConnection();
			
			// Check if we have an address validation entry
			GetAddressValidation avdao = new GetAddressValidation(con);
			AddressValidation av = avdao.get(ctx.getUser().getID());
			if (av != null) {
				ctx.release();
				result.setType(ResultType.REDIRECT);
				result.setURL("validate.do");
				result.setSuccess(true);
				return;	
			}
			
			// Get the User data object
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(ctx.getUser().getID());
			UserDataMap udm = uddao.get(ud.getIDs());
			
			// Load pther Pilot profiles we have achieved
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> profiles = pdao.get(udm);
			
			// Calculate total legs/hours
			int totalLegs = 0; double totalHours = 0;
			for (Iterator<Pilot> i = profiles.values().iterator(); i.hasNext(); ) {
				Pilot usr = i.next();
				totalLegs += usr.getLegs();
				totalHours += usr.getHours();
			}
			
			// Save total legs/hours
			p = profiles.get(new Integer(ctx.getUser().getID()));
			p.setTotalLegs(totalLegs);
			p.setTotalHours(totalHours);

			// Stuff the pilot profile in the request and the session
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute(HTTPContext.USER_ATTR_NAME, p, SESSION);
			
			// Check for manual PIREP ability
			GetFlightReports frdao = new GetFlightReports(con);
			int heldPIREPs = frdao.getHeld(p.getID(), SystemData.get("airline.db"));
			boolean manualPIREP = (p.getACARSRestriction() != Pilot.ACARS_ONLY);
			manualPIREP &= (heldPIREPs < SystemData.getInt("users.pirep.maxHeld", 5));
			ctx.setAttribute("manualPIREP", Boolean.valueOf(manualPIREP), REQUEST);
			ctx.setAttribute("heldPIREPCount", Integer.valueOf(heldPIREPs), REQUEST);

			// Save the pilot location
			GetPilotBoard pbdao = new GetPilotBoard(con);
			ctx.setAttribute("geoLocation", pbdao.getLocation(p.getID()), REQUEST);

			// Check our access level
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);

			// Load all PIREPs and save the latest PIREP as a separate bean in the request
			frdao.setQueryMax(10);
			List<FlightReport> results = frdao.getByPilot(p.getID(), new ScheduleSearchCriteria("DATE DESC, PR.ID DESC"));
			for (Iterator<FlightReport> i = results.iterator(); i.hasNext();) {
				FlightReport fr = i.next();
				if ((fr.getStatus() != FlightReport.DRAFT) && (fr.getStatus() != FlightReport.REJECTED)) {
					ctx.setAttribute("lastFlight", fr, REQUEST);
					break;
				}
			}

			// Get online hours
			GetFlightReportRecognition prdao = new GetFlightReportRecognition(con);
			prdao.getOnlineTotals(p, SystemData.get("airline.db"));

			// Get the schedule size
			GetSchedule sdao = new GetSchedule(con);
			ctx.setAttribute("scheduleSize", Integer.valueOf(sdao.getFlightCount()), REQUEST);

			// Get the PIREP disposal queue sizes
			if (ctx.isUserInRole("PIREP")) {
				ctx.setAttribute("pirepQueueSize", Integer.valueOf(prdao.getDisposalQueueSize()), REQUEST);
				String eqType = ctx.isUserInRole("HR") ? null : p.getEquipmentType();
				ctx.setAttribute("checkRideQueueSize", Integer.valueOf(prdao.getCheckRideQueueSize(eqType)), REQUEST);
			}
			
			// Initialize the testing history helper and check for test lockout
			TestingHistoryHelper testHistory = initTestHistory(p, con);
			testHistory.setDebug(ctx.isSuperUser());
			ctx.setAttribute("examLockout", Boolean.valueOf(testHistory.isLockedOut(SystemData.getInt("testing.lockout"))), REQUEST);

			// Get the Assistant Chief Pilots (if any) for the equipment program
			ctx.setAttribute("asstCP", pdao.getPilotsByEQ(testHistory.getEquipmentType(), null, true, Ranks.RANK_ACP), REQUEST);

			// Save the pilot's equipment program and check if we can get promoted to Captain
			ctx.setAttribute("eqType", testHistory.getEquipmentType(), REQUEST);
			ctx.setAttribute("captPromote", Boolean.valueOf(testHistory.canPromote(testHistory.getEquipmentType())), REQUEST);

			// Count how many legs completed towards Promtion
			int promoLegs = prdao.getPromotionCount(p.getID(), p.getEquipmentType());
			ctx.setAttribute("isFO", Boolean.valueOf(Ranks.RANK_FO.equals(p.getRank())), REQUEST);
			ctx.setAttribute("promoteLegs", Integer.valueOf(promoLegs), REQUEST);

			// Get Exam profiles
			GetExamProfiles epdao = new GetExamProfiles(con);
			Map<String, ExamProfile> exams = CollectionUtils.createMap(epdao.getExamProfiles(false), "name");

			// Check if we are trying to switch equipment types
			GetTransferRequest txdao = new GetTransferRequest(con);
			boolean hasTX = txdao.hasTransfer(p.getID());
			TransferRequest txreq = hasTX ? txdao.get(p.getID()) : null;
			if (!hasTX && (p.getLegs() > 0)) {
				// Get all active equipment programs, and see which we can switch to
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Collection<EquipmentType> activeEQ = eqdao.getAvailable(SystemData.get("airline.code"));
				Collection<EquipmentType> needFOExamEQ = new TreeSet<EquipmentType>();
				for (Iterator<EquipmentType> i = activeEQ.iterator(); i.hasNext();) {
					EquipmentType eq = i.next();
					if (!testHistory.canSwitchTo(eq) && !testHistory.canRequestCheckRide(eq)) {
						Collection<String> eNames = eq.getExamNames(Ranks.RANK_FO);
						if (!testHistory.hasPassed(eNames)) {
							for (Iterator<String> ei = eNames.iterator(); ei.hasNext(); ) {
								String examName = ei.next();
								ExamProfile ep = exams.get(examName);
								if ((ep != null) && (testHistory.canWrite(ep)))
									needFOExamEQ.add(eq);
							}
						}
						
						i.remove();
					}
				}

				// Save the equipment types we can get promoted to
				ctx.setAttribute("eqSwitch", activeEQ, REQUEST);
				ctx.setAttribute("eqSwitchFOExam", needFOExamEQ, REQUEST);
			} else if (txreq != null) {
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
			} else if (hasTX)
				ctx.setAttribute("txPending", Boolean.TRUE, REQUEST);

			// See if we can write any examinations
			for (Iterator<ExamProfile> i = exams.values().iterator(); i.hasNext();) {
				ExamProfile ep = i.next();
				if (!testHistory.canWrite(ep))
					i.remove();
			}
			
			// If we have an e-mail box and mail is enabled, check for new mail
			if (SystemData.getBoolean("smtp.imap.enabled")) {
				GetPilotEMail pedao = new GetPilotEMail(con);
				EMailConfiguration mcfg = pedao.getEMailInfo(p.getID());
				if ((mcfg != null) && (!StringUtils.isEmpty(SystemData.get("smtp.imap.newmail"))))
					ctx.setAttribute("newMsgs", Integer.valueOf(pedao.hasNewMail(mcfg.getMailDirectory())), REQUEST);
			}
			
			// If we are in the HR/Examination roles, get transfer request and exam counts
			if (ctx.isUserInRole("HR") || ctx.isUserInRole("Examination")) {
				String myEQType = ctx.isUserInRole("HR") ? null : p.getEquipmentType();
				
				// Get promotion queue size
				GetPilotRecognition rdao = new GetPilotRecognition(con);
				ctx.setAttribute("promoQueueSize", Long.valueOf(rdao.hasPromotionQueue(myEQType)), REQUEST);
				
				// Get exam/transfer queue sizes
				GetExam exdao = new GetExam(con);
				ctx.setAttribute("examQueueSize", Integer.valueOf(exdao.getSubmitted().size()), REQUEST);
				ctx.setAttribute("txQueueSize", Integer.valueOf(txdao.getCount(myEQType)), REQUEST);
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
			ctx.setAttribute("availableExams", exams.values(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get latest acars version
		if (ctx.isUserInRole("Admin") && SystemData.getBoolean("acars.enabled")) {
			ACARSClientInfo cInfo = (ACARSClientInfo) SharedData.get(SharedData.ACARS_CLIENT_BUILDS);
			ctx.setAttribute("latestBuild", Integer.valueOf(cInfo.getLatest()), REQUEST);
		}

		// Figure out the image to display
		Map<?, ?> acImgs = (Map<?, ?>) SystemData.getObject("pcImages");
		if (acImgs != null)
			ctx.setAttribute("acImage", acImgs.get(p.getEquipmentType().toLowerCase()), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/pilot/pilotCenter.jsp");
		result.setSuccess(true);
	}
}