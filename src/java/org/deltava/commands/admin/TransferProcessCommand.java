// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2016, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.hr.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.TransferAccessControl;

/**
 * A Web Site Command to display a Transfer Request for processing.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class TransferProcessCommand extends AbstractCommand {
	
	private static final Collection<Rank> STAFF_RANKS = List.of(Rank.ACP, Rank.CP);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the transfer request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(ctx.getID());
			if (txreq == null)
				throw notFoundException("Invalid Transfer Request - " + ctx.getID());

			// Check our access
			TransferAccessControl access = new TransferAccessControl(ctx, txreq);
			access.validate();

			// Get the pilot
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserData ud = uddao.get(txreq.getID());
			Pilot usr = pdao.get(ud);
			ctx.setAttribute("pilot", usr, REQUEST);
			
			// See if there's any checkrides
			GetExam exdao = new GetExam(con);
			GetFlightReports frdao = new GetFlightReports(con);
			Map<Integer, CheckRide> rides = new HashMap<Integer, CheckRide>();
			Map<Integer, Pilot> graders = new HashMap<Integer, Pilot>();
			Map<Integer, FlightReport> pireps = new HashMap<Integer, FlightReport>();
			for (Integer crID : txreq.getCheckRideIDs()) {
				CheckRide cr = exdao.getCheckRide(crID.intValue());
				if (cr != null) {
					rides.put(crID, cr);
					Pilot scorer = pdao.get(uddao.get(cr.getScorerID()));
					if (scorer != null)
						graders.put(Integer.valueOf(cr.getScorerID()), scorer);
					if (cr.getFlightID() != 0)
						pireps.put(Integer.valueOf(cr.getFlightID()), frdao.getACARS(ud.getDB(), cr.getFlightID()));
				}
			}
			
			// Get the requested equipment type
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType newEQ = eqdao.get(txreq.getEquipmentType(), ctx.getDB());
			EquipmentType currEQ = eqdao.get(usr.getEquipmentType(), ud.getDB());
			ctx.setAttribute("currentEQ", currEQ, REQUEST);
			ctx.setAttribute("eqType", newEQ, REQUEST);
			if (txreq.getRatingOnly())
				ctx.setAttribute("activeEQ", Collections.singleton(currEQ), REQUEST);
			else
				ctx.setAttribute("activeEQ", eqdao.getActive(), REQUEST);
			
			// Check if the user has passed the Captain's examination
			TestingHistoryHelper testHistory = new TestingHistoryHelper(usr, currEQ, exdao.getExams(usr.getID()), Collections.emptyList());
			boolean hasCaptExam = testHistory.hasPassed(newEQ.getExamNames(Rank.C));

			// Check how many legs the user has completed
			GetFlightReportRecognition prdao = new GetFlightReportRecognition(con);
			int promoLegs = prdao.getPromotionCount(txreq.getID(), txreq.getEquipmentType());
			boolean hasLegs = (promoLegs >= newEQ.getPromotionLegs());
			
			// Check if user ever received Senior Captain rank
			GetStatusUpdate stdao = new GetStatusUpdate(con);
			boolean isSC = stdao.isSeniorCaptain(usr.getID());

			// Check if the user is eligible for promotion
			ctx.setAttribute("captExam", Boolean.valueOf(hasCaptExam), REQUEST);
			ctx.setAttribute("promoLegs", Integer.valueOf(promoLegs), REQUEST);
			ctx.setAttribute("captOK", Boolean.valueOf(hasCaptExam && hasLegs), REQUEST);
			
			// Get the available ranks
			Collection<Rank> eqRanks = new LinkedHashSet<Rank>(newEQ.getRanks());
			eqRanks.removeAll(STAFF_RANKS);
			if (!isSC)
				eqRanks.remove(Rank.SC);
			if (!hasCaptExam || !hasLegs)
				eqRanks.remove(Rank.C);
			ctx.setAttribute("newRanks", eqRanks, REQUEST);

			// Determine new equipment ratings if approved
			Collection<String> newRatings = new TreeSet<String>(usr.getRatings());
			newRatings.addAll(newEQ.getPrimaryRatings());
			newRatings.addAll(newEQ.getSecondaryRatings());
			ctx.setAttribute("newRatings", newRatings, REQUEST);
			
			// Load available check ride scripts
			if (access.getCanAssignRide()) {
				GetExamProfiles epdao = new GetExamProfiles(con);
				Collection<EquipmentRideScript> crScripts = epdao.getScripts().stream().filter(rs -> rs.getProgram().equals(newEQ.getName())).collect(Collectors.toList());
				ctx.setAttribute("eqScripts", crScripts, REQUEST);
			}
			
			// Get all aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);

			// Save the transfer request and access controller
			ctx.setAttribute("txReq", txreq, REQUEST);
			ctx.setAttribute("checkRides", rides, REQUEST);
			ctx.setAttribute("pireps", pireps, REQUEST);
			ctx.setAttribute("scorers", graders, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/txRequestProcess.jsp");
		result.setSuccess(true);
	}
}