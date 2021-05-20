// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.assign.*;
import org.deltava.beans.assign.CharterRequest.RequestStatus;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CharterRequestAccessControl;

import org.deltava.util.EnumUtils;

/**
 * A Web Site Command to dispose of Charter flight Requests. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class CharterRequestDisposeCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		RequestStatus st = EnumUtils.parse(RequestStatus.class, ctx.getParameter("op"), RequestStatus.PENDING);
		if (st == RequestStatus.PENDING)
			throw new CommandException("Invalid Charter Request status - " + ctx.getParameter("op"), false); 
		
		MessageContext mctx = new MessageContext(); Pilot p = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the request
			GetCharterRequests rqdao = new GetCharterRequests(con);
			CharterRequest req = rqdao.get(ctx.getID());
			if (req == null)
				throw notFoundException("Invalid Charter Request ID - " + ctx.getID());
			
			// Check access
			CharterRequestAccessControl ac = new CharterRequestAccessControl(ctx, req);
			ac.validate();
			if (!ac.getCanDispose())
				throw securityException("Cannot dispose Charter Request " + ctx.getID());
			
			// Load the Pilot
			GetPilot pdao = new GetPilot(con);
			p = pdao.get(req.getAuthorID());
			ctx.setAttribute("pilot", p, REQUEST);
			
			// Load the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get((st == RequestStatus.REJECTED) ? "CHREQREJECT" : "CHREQAPPROVE"));
			
			// Update fields
			req.setStatus(st);
			req.setDisposalID(ctx.getUser().getID());
			req.setDisposedOn(Instant.now());
			
			// Start transaction
			ctx.startTX();
			
			// Write assignment/flight report if approved
			SetAssignment awdao = new SetAssignment(con);
			if (st == RequestStatus.APPROVED) {
				AssignmentInfo info = new AssignmentInfo(req.getEquipmentType());
				info.setAssignDate(Instant.now());
				info.setStatus(AssignmentStatus.RESERVED);
				info.setPurgeable(true);
				info.setRandom(true);
			
				// Build the leg
				AssignmentLeg leg = new AssignmentLeg(req.getAirline(), 9000 + (p.getPilotNumber() % 1000), 1);
				leg.setEquipmentType(req.getEquipmentType());
				leg.setAirportD(req.getAirportD());
				leg.setAirportA(req.getAirportA());
				info.addAssignment(leg);
				info.setPilotID(p.getID());
			
				// Build the PIREP
				FlightReport fr = new FlightReport(leg);
				fr.setDatabaseID(DatabaseID.PILOT, p.getID());
				fr.setDatabaseID(DatabaseID.DISPOSAL, ctx.getUser().getID());
				fr.setRank(p.getRank());
				fr.setDate(info.getAssignDate());
				fr.setAttribute(FlightReport.ATTR_CHARTER, true);
				fr.setEquipmentType(req.getEquipmentType());
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, String.format("Charter Flight Request %d", Integer.valueOf(req.getID())));
			
				// Create the Flight Assignment
				awdao.write(info, ctx.getDB());
				awdao.assign(info, info.getPilotID(), ctx.getDB());
			
				// Write the Flight leg
				fr.setDatabaseID(DatabaseID.ASSIGN, info.getID());
				info.addFlight(fr);
				SetFlightReport fwdao = new SetFlightReport(con);
				fwdao.write(fr);
			}
				
			// Update the charter request
			awdao.write(req);
			
			// Save status attributes and commit
			ctx.commitTX();
			mctx.addData("chreq", req);
			mctx.addData("user", ctx.getUser());
			ctx.setAttribute("req", req, REQUEST);
			ctx.setAttribute("isApproved", Boolean.valueOf(st == RequestStatus.APPROVED), REQUEST);
			ctx.setAttribute("reqStatus", st, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send the notification
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctx);
		mailer.send(p);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/assign/charterRequestUpdate.jsp");
		result.setSuccess(true);
	}
}