// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2020, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.time.temporal.ChronoUnit;

import com.newrelic.api.agent.NewRelic;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.acars.Restriction;
import org.deltava.beans.econ.*;
import org.deltava.beans.hr.TransferRequest;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Pilot Center.
 * @author Luke
 * @version 11.5
 * @since 1.0
 */

public class PilotCenterCommand extends AbstractTestHistoryCommand {
	
	/**
	 * Executes the command
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();
		Pilot p = null; final Instant now = Instant.now();
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
			for (Pilot usr : profiles.values()) {
				totalLegs += usr.getLegs();
				totalHours += usr.getHours();
			}
			
			// Save total legs/hours
			p = profiles.get(Integer.valueOf(ctx.getUser().getID()));
			p.setTotalLegs(totalLegs);
			p.setTotalHours(totalHours);
			NewRelic.addCustomParameter("pilot.name", p.getName());

			// Calculate how long we've been a member
			long pilotAge = (System.currentTimeMillis() - p.getCreatedOn().toEpochMilli()) / 86400000;
			ctx.setAttribute("pilotAge", Integer.valueOf((int) pilotAge), REQUEST);
			
			// Check for manual PIREP ability
			GetFlightReports frdao = new GetFlightReports(con);
			int heldPIREPs = frdao.getHeld(p.getID(), ctx.getDB());
			boolean manualPIREP = (p.getACARSRestriction() != Restriction.NOMANUAL);
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
			
			// Load elite status
			if (SystemData.getBoolean("econ.elite.enabled")) {
				GetElite eldao = new GetElite(con);
				int currentYear = EliteScorer.getStatusYear(now);
				TreeSet<EliteLevel> levels = eldao.getLevels(currentYear);
				ctx.setAttribute("eliteYear", Integer.valueOf(currentYear), REQUEST);
				List<EliteStatus> myStatus = eldao.getAllStatus(p.getID(), currentYear);
				if (myStatus.isEmpty()) {
					EliteStatus curStatus = new EliteStatus(p.getID(), levels.first()); 
					curStatus.setEffectiveOn(now);
					myStatus.add(curStatus);
				}
				
				// Get current status
				EliteStatus myCurrentStatus = myStatus.getLast();
				EliteLifetimeStatus els = eldao.getLifetimeStatus(p.getID(), ctx.getDB());
				if (myCurrentStatus.overridenBy(els)) {
					myCurrentStatus = els.toStatus();
					ctx.setAttribute("ltStatus", els.getLifetimeStatus(), REQUEST);
				}
				
				ctx.setAttribute("eliteStatus", myCurrentStatus, REQUEST);
				ctx.setAttribute("nextEliteLevel", levels.higher(myCurrentStatus.getLevel()), REQUEST);
				p.setEliteStatus(myCurrentStatus);
					
				// Get our totals
				GetEliteStatistics esdao = new GetEliteStatistics(con);
				YearlyTotal cyt = esdao.getEliteTotals(p.getID(), currentYear);
				ctx.setAttribute("currentEliteTotal", cyt, REQUEST);
				ctx.setAttribute("currentEliteRO", esdao.getRollover(p.getID(), currentYear), REQUEST);
				
				// Determine if we can do year-end activities
				boolean rolloverPeriod = EliteScorer.isRollover();
				ctx.setAttribute("eliteRollover", Boolean.valueOf(rolloverPeriod), REQUEST);
				TreeSet<EliteLevel> nyLevels = new TreeSet<EliteLevel>();
				if (rolloverPeriod) {
					nyLevels.addAll(eldao.getLevels(currentYear + 1));
					ctx.setAttribute("nyLevels", nyLevels, REQUEST);
				}
				
				// Display next year's level and downgrade potential after Q3
				if (rolloverPeriod) {
					boolean isRolloverComplete = ctx.isUserInRole("Operations") && esdao.isRolloverComplete(currentYear + 1);
					ctx.setAttribute("eliteRolloverComplete", Boolean.valueOf(isRolloverComplete), REQUEST);
					TreeSet<EliteLevel> lvls = nyLevels.isEmpty() ? levels : nyLevels;
					EliteLevel nextYearLevel = cyt.matches(lvls); EliteStatus nyStatus = new EliteStatus(p.getID(), nextYearLevel);
					if (nyStatus.overridenBy(els))
						nyStatus = els.toStatus();
					
					boolean isDowngrade = (nyStatus.getLevel().compareTo(myCurrentStatus.getLevel()) < 0);
					ctx.setAttribute("nyLevel", nyStatus.getLevel(), REQUEST);
					ctx.setAttribute("nyDowngrade", Boolean.valueOf(isDowngrade), REQUEST);
					if (isDowngrade)
						ctx.setAttribute("nextEliteLevel", myCurrentStatus.getLevel(), REQUEST);
				}
			}

			// Load all PIREPs and save the latest PIREP as a separate bean in the request
			frdao.setQueryMax(10);
			List<FlightReport> results = frdao.getByPilot(p.getID(), new LogbookSearchCriteria("SUBMITTED DESC", ctx.getDB()));
			FlightReport lastFlight = results.stream().filter(fr -> (fr.getStatus() != FlightStatus.DRAFT) && (fr.getStatus() != FlightStatus.REJECTED)).findFirst().orElse(null);
			ctx.setAttribute("lastFlight", lastFlight, REQUEST);
			
			// Check if we can request a backout charter
			if (lastFlight != null) {
				GetSchedule schdao = new GetSchedule(con);
				GetRawSchedule rsdao = new GetRawSchedule(con);
				schdao.setSources(rsdao.getSources(true, ctx.getDB()));
				int outFlightCount = schdao.getFlights(lastFlight.getAirportA()).size();
				boolean rCharter = ((outFlightCount == 0) && !lastFlight.hasAttribute(FlightReport.ATTR_ROUTEWARN)); 
				ctx.setAttribute("needReturnCharter", Boolean.valueOf(rCharter), REQUEST);
			}
			
			// Get online hours
			GetFlightReports prdao = new GetFlightReports(con);
			prdao.getOnlineTotals(p, ctx.getDB());
			
			// If we're a dispatcher, load dispatch totals
			if (ctx.isUserInRole("Dispatch")) {
				GetACARSDispatchStats dspstdao = new GetACARSDispatchStats(con);
				dspstdao.getDispatchTotals(p);
			}
			
			// Load Accomplishments
			GetAccomplishment acdao = new GetAccomplishment(con);
			ctx.setAttribute("accs", acdao.getByPilot(p, ctx.getDB()), REQUEST);

			// Get the schedule size
			GetScheduleInfo sdao = new GetScheduleInfo(con);
			ctx.setAttribute("scheduleSize", Integer.valueOf(sdao.getFlightCount()), REQUEST);

			// Get the PIREP disposal queue sizes
			if (ctx.isUserInRole("PIREP")) {
				boolean isAcademy = ctx.isUserInRole("Instructor") || ctx.isUserInRole("AcademyAdmin") || ctx.isUserInRole("AcademyAudit");
				String eqType = ctx.isUserInRole("HR") ? null : p.getEquipmentType();
				GetFlightReportQueue frqdao = new GetFlightReportQueue(con);
				ctx.setAttribute("pirepQueueStats", frqdao.getDisposalQueueStats(eqType), REQUEST);
				ctx.setAttribute("checkRideQueueSize", Integer.valueOf(prdao.getCheckRideQueueSize(eqType, isAcademy)), REQUEST);
				
				// Get Charter requests
				GetCharterRequests crqdao = new GetCharterRequests(con);
				ctx.setAttribute("charterRequestQueueSize", Integer.valueOf(crqdao.getPendingCount()), REQUEST);
			}
			
			// Initialize the testing history helper and check for test lockout
			TestingHistoryHelper testHistory = initTestHistory(p, con);
			ctx.setAttribute("eqSwitchMaxStage", Integer.valueOf(testHistory.getMaxCheckRideStage()), REQUEST);
			ctx.setAttribute("examLockout", Boolean.valueOf(testHistory.isLockedOut(SystemData.getInt("testing.lockout"))), REQUEST);
			
			// Check passenger count
			int totalPax = testHistory.getFlights().stream().filter(fr -> ((fr.getStatus() != FlightStatus.DRAFT) && (fr.getStatus() != FlightStatus.REJECTED))).mapToInt(FlightReport::getPassengers).sum();
			ctx.setAttribute("totalPax", Integer.valueOf(totalPax), REQUEST);

			// Get the Assistant Chief Pilots (if any) for the equipment program
			ctx.setAttribute("CP", pdao.get(testHistory.getEquipmentType().getCPID()), REQUEST);
			ctx.setAttribute("asstCP", pdao.getPilotsByEQ(testHistory.getEquipmentType(), null, true, Rank.ACP), REQUEST);

			// Save the pilot's equipment program and check if we can get promoted to Captain
			ctx.setAttribute("eqType", testHistory.getEquipmentType(), REQUEST);
			ctx.setAttribute("captPromote", Boolean.valueOf(testHistory.canPromote(testHistory.getEquipmentType())), REQUEST);

			// Count how many legs completed towards Promtion
			GetFlightReportRecognition pcdao = new GetFlightReportRecognition(con);
			int promoLegs = pcdao.getPromotionCount(p.getID(), p.getEquipmentType());
			ctx.setAttribute("isFO", Boolean.valueOf(Rank.FO == p.getRank()), REQUEST);
			ctx.setAttribute("promoteLegs", Integer.valueOf(promoLegs), REQUEST);

			// Get Exam profiles
			GetExamProfiles epdao = new GetExamProfiles(con);
			Map<String, ExamProfile> exams = CollectionUtils.createMap(epdao.getExamProfiles(false), ExamProfile::getName);

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
					try {
						boolean checkSwitch = true;
						if (!testHistory.hasCheckRide(eq)) {
							testHistory.canRequestCheckRide(eq);
							checkSwitch = false;
						}
						
						if (checkSwitch)
							testHistory.canSwitchTo(eq);
					} catch (IneligibilityException ie) {
						i.remove();	
						Collection<String> eNames = eq.getExamNames(Rank.FO);
						if (!testHistory.hasPassed(eNames)) {
							for (Iterator<String> ei = eNames.iterator(); ei.hasNext(); ) {
								String examName = ei.next();
								ExamProfile ep = exams.get(examName);
								if (ep != null) {
									try {
										testHistory.canWrite(ep);
										needFOExamEQ.add(eq);
									} catch (IneligibilityException iee) {
										// empty
									}
								}
							}
						}
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
				if (txreq.getLatestCheckRideID() != 0) {
					GetExam exdao = new GetExam(con);
					ctx.setAttribute("checkRide", exdao.getCheckRide(txreq.getLatestCheckRideID()), REQUEST);
				}
			} else if (hasTX)
				ctx.setAttribute("txPending", Boolean.TRUE, REQUEST);

			// See if we can write any examinations
			for (Iterator<ExamProfile> i = exams.values().iterator(); i.hasNext();) {
				ExamProfile ep = i.next();
				try {
					testHistory.canWrite(ep);
				} catch (IneligibilityException ie) {
					i.remove();
				}
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
				ctx.setAttribute("crQueueSize", Integer.valueOf(exdao.getSubmittedRides().size()), REQUEST);
			}
			
			// If we have proficiency-based check rides, get upcoming expirations
			if (p.getProficiencyCheckRides()) {
				int expDays = Math.min(30, Math.max(15, SystemData.getInt("testing.currency.validity", 365)));
				ctx.setAttribute("upcomingExpirations", testHistory.getCheckRides(expDays), REQUEST);
				ctx.setAttribute("expiryDays", Integer.valueOf(expDays), REQUEST);
				ctx.setAttribute("expirationDate", now.plus(expDays, ChronoUnit.DAYS), REQUEST);
				ctx.setAttribute("now", now, REQUEST);
			}

			// See if we are enrolled in a Flight Academy course
			if (SystemData.getBoolean("academy.enabled")) {
				GetAcademyCourses fadao = new GetAcademyCourses(con);
				List<Course> courses = new ArrayList<Course>(fadao.getByPilot(p.getID()));
				ctx.setAttribute("courses", courses, REQUEST);
				if (!courses.isEmpty()) {
					Course c = courses.getLast();
					if (c.getStatus() == Status.STARTED)
						ctx.setAttribute("course", c, REQUEST);
				}

				// Check if we have instruction flights
				GetAcademyCalendar facdao = new GetAcademyCalendar(con);
				facdao.setQueryMax(1);
				boolean hasFlights = !facdao.getFlightCalendar(p.getID(), null).isEmpty();
				ctx.setAttribute("academyInsFlights", Boolean.valueOf(hasFlights || ctx.isUserInRole("HR")), REQUEST);
			}

			// Save the examinations
			ctx.setAttribute("availableExams", exams.values(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Stuff the pilot profile in the request
		ctx.setAttribute("pilot", p, REQUEST);
		ctx.setAttribute(HTTPContext.USER_ATTR_NAME, p, SESSION);
		
		// Figure out the image to display
		Map<?, ?> acImgs = (Map<?, ?>) SystemData.getObject("pcImages");
		if (acImgs != null)
			ctx.setAttribute("acImage", acImgs.get(p.getEquipmentType().toLowerCase()), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/pilot/pilotCenter.jsp");
		result.setSuccess(true);
	}
}