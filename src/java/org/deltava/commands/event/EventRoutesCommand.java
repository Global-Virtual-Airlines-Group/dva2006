// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.3
 * @since 1.0
 */

public class EventRoutesCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the flight routes.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
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
			
			// Start transaction
			ctx.startTX();

			// Add/delete the route
			SetEvent wdao = new SetEvent(con);

			// Get existing routes
			int maxRouteID = 1;
			for (Iterator<Route> i = e.getRoutes().iterator(); i.hasNext(); ) {
				Route rt = i.next();
				maxRouteID = Math.max(maxRouteID, rt.getRouteID() + 1);
				
				// Check for delete/disable
				boolean isDelete = Boolean.valueOf(ctx.getParameter("delete" + rt.getRouteID())).booleanValue();
				boolean isDisable = Boolean.valueOf(ctx.getParameter("disable" + rt.getRouteID())).booleanValue();
					
				// Update the route
				if (isDelete)
					wdao.delete(rt);
				else {
					rt.setName(ctx.getParameter("routeName" + rt.getRouteID()));
					rt.setRoute(ctx.getParameter("route" + rt.getRouteID()));
					rt.setIsRNAV(Boolean.valueOf(ctx.getParameter("isRNAV" + rt.getRouteID())).booleanValue());
					rt.setMaxSignups(StringUtils.parse(ctx.getParameter("maxSignups" + rt.getRouteID()), 0));
					wdao.save(rt);
					if (isDisable)
						wdao.toggle(rt);
				}
			}
				
			// Build a new route
			if (!StringUtils.isEmpty(ctx.getParameter("routeName"))) {
				Route r = new Route(e.getID(), ctx.getParameter("route"));
				r.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
				r.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
				r.setName(ctx.getParameter("routeName"));
				r.setMaxSignups(StringUtils.parse(ctx.getParameter("maxSignups"), 0));
				r.setIsRNAV(Boolean.valueOf(ctx.getParameter("isRNAV")).booleanValue());
				r.setActive(true);
				
				// Save the route
				r.setRouteID(maxRouteID);
				wdao.save(r);
			}
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
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
			// Get the DAO and the event
			GetEvent dao = new GetEvent(ctx.getConnection());
			Event e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Online Event - " + ctx.getID());
			
			// Check our access
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanAddPlan())
				throw securityException("Cannot update Flight Routes");
			
			// Save route IDs
			Collection<Integer> routeIDs = new TreeSet<Integer>();
			for (Route r : e.getRoutes())
				routeIDs.add(Integer.valueOf(r.getRouteID()));

			// Save the event and its route IDs
			ctx.setAttribute("event", e, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("routeIDs", routeIDs, REQUEST);
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