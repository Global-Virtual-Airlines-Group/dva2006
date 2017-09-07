// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command for a Pilot to self-assign a Currency Check Ride.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class CurrencyRideAssignCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Check if we have a pending transfer request
			GetTransferRequest txdao = new GetTransferRequest(con);
			if (txdao.hasTransfer(p.getID()))
				throw securityException("Pending Equipment Transfer request for " + p.getName());
			else if (!ctx.getUser().getProficiencyCheckRides())
				throw securityException("Proficiency Check Rides not enabled for " + p.getName());
			
			// Load test history and check rides that expire soon
			int expDays = Math.min(30, Math.max(15, SystemData.getInt("testing.currency.validity", 365)));
			TestingHistoryHelper testHelper = initTestHistory(p, con);
			Collection<CheckRide> expRides = testHelper.getCheckRides(expDays);
			boolean hasCR = testHelper.getExams().stream().anyMatch(ex -> ((ex instanceof CheckRide) && (ex.getStatus() != TestStatus.SCORED)));
			
			// Load valid equipment types - needs to be something we are rated for
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<EquipmentType> myEQ = new ArrayList<EquipmentType>();
			for (CheckRide ecr : expRides)
				myEQ.add(eqdao.get(ecr.getEquipmentType()));
			
			// Forward to the JSP if no program specified
			if (ctx.getParameter("eqType") == null) {
				Map<String, Collection<String>> primaryEQ = new TreeMap<String, Collection<String>>();
				myEQ.forEach(eq -> primaryEQ.put(eq.getName(), eq.getPrimaryRatings()));

				ctx.setAttribute("expiryDate", Instant.now().plus(expDays, ChronoUnit.DAYS), REQUEST);
				ctx.setAttribute("eqTypes", myEQ, REQUEST);
				ctx.setAttribute("eqAircraft", primaryEQ, REQUEST);
				ctx.setAttribute("hasCheckRide", Boolean.valueOf(hasCR), REQUEST);
				
				CommandResult result = ctx.getResult();
				result.setURL("/jsp/testing/currencyRideAssign.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Validate the equipment type
			EquipmentType eq = eqdao.get(ctx.getParameter("eqType"));
			if (eq == null)
				throw notFoundException("Unknown Equipment Program - " + ctx.getParameter("eqType"));
			else if (!myEQ.contains(eq))
				throw securityException("No expiring currency in " + eq.getName() + " program"); 
			else if (hasCR)
				throw securityException("Pending Check Ride");
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("RIDEASSIGN"));
			mctxt.addData("pilot", p);
			mctxt.addData("eqType", eq);

			// Get the check ride script
			String comments = "";
			EquipmentRideScriptKey key = new EquipmentRideScriptKey(eq.getName(), ctx.getParameter("acType"), true);
			GetExamProfiles epdao = new GetExamProfiles(con);
			CheckRideScript sc = epdao.getScript(key);
			if (sc != null)
				comments = sc.getDescription();
			
			// Create the check ride
			CheckRide cr = new CheckRide(eq.getName() + " recurrent Check Ride");
			cr.setOwner(SystemData.getApp(null));
			cr.setDate(Instant.now());
			cr.setEquipmentType(eq.getName());
			cr.setAircraftType(ctx.getParameter("acType"));
			cr.setAuthorID(p.getID());
			cr.setScorerID(ctx.getUser().getID());
			cr.setStatus(TestStatus.NEW);
			cr.setStage(eq.getStage());
			cr.setType(RideType.CURRENCY);
			cr.setComments(comments);
			
			// Write the checkride to the database
			SetExam exwdao = new SetExam(con);
			exwdao.write(cr);

			// Save the checkride in the request
			mctxt.addData("checkRide", cr);
			ctx.setAttribute("checkRide", cr, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(p);
		
		// Update status for the JSP
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
		ctx.setAttribute("isAssign", Boolean.TRUE, REQUEST);
		ctx.setAttribute("isCurrency", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/cRideUpdate.jsp");
		result.setSuccess(true);
	}
}