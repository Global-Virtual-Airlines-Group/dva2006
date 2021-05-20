// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.assign.CharterRequest;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.comparators.AirlineComparator;

import org.deltava.security.command.CharterRequestAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view and edit Charter flight Requests.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class CharterRequestCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			boolean isNew = (ctx.getID() == 0); CharterRequest req = null;
			if (!isNew) {
				GetCharterRequests rqdao = new GetCharterRequests(con);
				req = rqdao.get(ctx.getID());
				if (req == null)
					throw notFoundException("Invalid Charter Request ID - " + ctx.getID());
			} 
			
			// Check our access
			CharterRequestAccessControl ac = new CharterRequestAccessControl(ctx, req);
			ac.validate();
			if (!ac.getCanEdit())
				throw securityException(String.format("Cannot %s Charter Request %d", isNew ? "create" : "edit", Integer.valueOf(ctx.getID())));
			
			if (req == null) {
				req = new CharterRequest();
				req.setCreatedOn(Instant.now());
				req.setAuthorID(ctx.getUser().getID());
			}
			
			// Populate from the request
			req.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
			req.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
			req.setAirline(SystemData.getAirline(ctx.getParameter("airline")));
			req.setEquipmentType(ctx.getParameter("eq"));
			req.setComments(ctx.getParameter("comments"));
			
			// Save the request
			SetAssignment rwdao = new SetAssignment(con);
			rwdao.write(req);
			ctx.setAttribute("req", req, REQUEST);
			ctx.setAttribute("isEdit", Boolean.valueOf(!isNew), REQUEST);
			ctx.setAttribute("isCreate", Boolean.valueOf(isNew), REQUEST);
			
			// Send notification
			if (isNew) {
				MessageContext mctx = new MessageContext();				
				mctx.addData("chreq", req);
				mctx.addData("user", ctx.getUser());
				
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctx.setTemplate(mtdao.get("CHREQNEW"));
				
				// Send the message
				GetPilotDirectory pdao = new GetPilotDirectory(con);
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctx);
				mailer.send(pdao.getByRole("Operations", ctx.getDB(), true));
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/assign/charterRequestUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		boolean isNew = (ctx.getID() == 0); CharterRequest req = null;
		if (!isNew) {
			try {
				Connection con = ctx.getConnection();
				GetCharterRequests rqdao = new GetCharterRequests(con);
				req = rqdao.get(ctx.getID());
				if (req == null)
					throw notFoundException("Invalid Charter Request ID - " + ctx.getID());
				
				// Load pilots
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("author", pdao.get(req.getAuthorID()), REQUEST);
				ctx.setAttribute("disposedBy", pdao.get(req.getDisposalID()), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		} else
			ctx.setAttribute("author", ctx.getUser(), REQUEST);
		
		// Save request attributes
		List<Airline> airlines = SystemData.getAirlines().values().stream().filter(Airline::getActive).collect(Collectors.toList());
		Collections.sort(airlines, new AirlineComparator(AirlineComparator.NAME));
		airlines.addAll(SystemData.getAirlines().values());
		ctx.setAttribute("chreq", req, REQUEST);
		ctx.setAttribute("airlines", airlines, REQUEST);
		
		// Check our access
		CharterRequestAccessControl ac = new CharterRequestAccessControl(ctx, req);
		ac.validate();
		if (!ac.getCanEdit())
			throw securityException(String.format("Cannot %s Charter Request %d", isNew ? "create" : "edit", Integer.valueOf(ctx.getID())));
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/assign/charterRequestEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
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
			if (!ac.getCanView())
				throw securityException("Cannot view Charter Request " + ctx.getID());
			
			// Load the pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("author", pdao.get(req.getAuthorID()), REQUEST);
			ctx.setAttribute("disposedBy", pdao.get(req.getDisposalID()), REQUEST);
			
			// Load the Aircraft
			GetAircraft acdao = new GetAircraft(con);
			Aircraft eq = acdao.get(req.getEquipmentType());
			ctx.setAttribute("eq", eq, REQUEST);
			ctx.setAttribute("opts", eq.getOptions(ctx.getDB().toUpperCase()), REQUEST);
			
			// Check the schedule
			GetSchedule sdao = new GetSchedule(con);
			ctx.setAttribute("airlines", sdao.getAirlines(req), REQUEST);
			
			// Save in the request
			ctx.setAttribute("chreq", req, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/assign/charterRequest.jsp");
		result.setSuccess(true);
	}
}