// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import javax.servlet.http.HttpSession;

import org.deltava.beans.*;
import org.deltava.beans.testing.ExamProfile;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Pilot Center.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotCenterCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
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
			result.setURL("emailupd.do");
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

			// Save the pilot location
			ctx.setAttribute("geoLocation", pdao.getLocation(p.getID()), REQUEST);

			// Check our access level
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);

			// Load all PIREPs and save the latest PIREP as a separate bean in the request
			GetFlightReports frdao = new GetFlightReports(con);
			List results = frdao.getByPilot(p.getID(), "DATE DESC");
			if (results.size() > 0)
				ctx.setAttribute("lastFlight", results.get(0), REQUEST);

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

			// Check if we are trying to switch equipment types
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(p.getID());
			if (txreq == null) {
				// Get all active equipment programs, and see which we can switch to
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Collection<EquipmentType> activeEQ = eqdao.getActive();
				for (Iterator<EquipmentType> i = activeEQ.iterator(); i.hasNext();) {
					EquipmentType eq = i.next();
					if (!_testHistory.canSwitchTo(eq) && !_testHistory.canRequestCheckRide(eq))
						i.remove();
				}

				// Save the equipment types we can get promoted to
				ctx.setAttribute("eqSwitch", activeEQ, REQUEST);
			} else {
				ctx.setAttribute("txreq", txreq, REQUEST);
			}

			// See if we can write any examinations
			GetExamProfiles epdao = new GetExamProfiles(con);
			Collection<ExamProfile> exams = epdao.getExamProfiles();
			for (Iterator<ExamProfile> i = exams.iterator(); i.hasNext();) {
				ExamProfile ep = i.next();
				if (!_testHistory.canWrite(ep))
					i.remove();
			}

			// Save the examinations
			ctx.setAttribute("availableExams", exams, REQUEST);
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