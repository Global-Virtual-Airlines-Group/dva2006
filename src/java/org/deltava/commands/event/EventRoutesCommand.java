// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.event.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update flight routes for an Online Event.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class EventRoutesCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the flight routes.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Check if we're doing a delete
		boolean isDelete = Boolean.valueOf(ctx.getParameter("isDelete")).booleanValue();
		boolean isToggle = Boolean.valueOf(ctx.getParameter("isToggle")).booleanValue();
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the event
			GetEvent dao = new GetEvent(con);
			Event e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Online Event - " + ctx.getID());
			
			// Check our access
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanAddPlan())
				throw securityException("Cannot update Flight Routes");

			// Add/delete the route
			int routeID = StringUtils.parse(ctx.getParameter("routeID"), 0);
			SetEvent wdao = new SetEvent(con);
			if (isDelete) {
				Route r = e.getRoute(routeID);
				if (r != null)
					wdao.delete(r);
			} else if (isToggle) {
				Route r = e.getRoute(routeID);
				if (r != null)
					wdao.toggle(r);
			} else {
				// Build the route
				Route r = new Route(e.getID(), ctx.getParameter("route"));
				r.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
				r.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
				r.setName(ctx.getParameter("routeName"));
				r.setMaxSignups(StringUtils.parse(ctx.getParameter("maxSignups"), 0));
				r.setIsRNAV(Boolean.valueOf(ctx.getParameter("isRNAV")).booleanValue());
				r.setActive(true);
				
				// Get the next Route ID
				int maxRouteID = 1;
				for (Iterator<Route> i = e.getRoutes().iterator(); i.hasNext(); ) {
					Route rt = i.next();
					maxRouteID = Math.max(maxRouteID, rt.getRouteID() + 1);
				}
				
				// Save the route
				r.setRouteID(maxRouteID);
				wdao.save(r);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the Command
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("eventroutes", "edit", ctx.getID());
	}

	/**
	 * Callback method called when editing the flight routes.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the event
			GetEvent dao = new GetEvent(con);
			Event e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Online Event - " + ctx.getID());
			
			// Check our access
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanAddPlan())
				throw securityException("Cannot update Flight Routes");

			// Save the event and its routes
			ctx.setAttribute("event", e, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save airports in the request
		Map<String, Airport> aMap = SystemData.getAirports();
		Set<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		airports.addAll(aMap.values());
		ctx.setAttribute("airports", airports, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/event/eventRoutes.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the flight routes. <i>This merely calls 
	 * {@link EventRoutesCommand#execEdit(CommandContext)}</i>.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}