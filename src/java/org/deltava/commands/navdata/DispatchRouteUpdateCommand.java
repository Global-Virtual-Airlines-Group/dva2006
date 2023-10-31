// Copyright 2009, 2010, 2011, 2012, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to ensure Dispatch Routes have the latest Terminal Route waypoints.
 * @author Luke
 * @version 11.1
 * @since 2.6
 */

public class DispatchRouteUpdateCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		boolean saveChanges = "save".equals(ctx.getCmdParameter(OPERATION, null));
		try {
			Connection con = ctx.getConnection();
			GetNavRoute navdao = new GetNavRoute(con);
			SetACARSRoute rwdao = new SetACARSRoute(con);
			
			// Get the routes
			int updateCount = 0;
			Collection<String> msgs = new ArrayList<String>();
			GetACARSRoute rdao = new GetACARSRoute(con);
			Collection<DispatchRoute> routes = rdao.getAll(true, true);
			for (DispatchRoute rt : routes) {
				boolean isUpdated = false;
				
				// Get the SID/STAR transition if different
				List<String> wps = StringUtils.split(rt.getRoute(), " ");
				
				// Load and validate the SID
				TerminalRoute sid = navdao.getRoute(rt.getAirportD(), TerminalRoute.Type.SID, rt.getSID());
				if ((sid == null) && (rt.getSID() != null)) {
					int pos = rt.getSID().indexOf('.');
					String name = rt.getSID().substring(0, pos);
					sid = navdao.getRoute(rt.getAirportD(), TerminalRoute.Type.SID, rt.getSID(), true);
					
					// If we still can't find a SID, find one that uses this transition and runway
					if (sid == null)
						sid = navdao.getRoute(rt.getAirportD(), TerminalRoute.Type.SID, "%" + rt.getSID().substring(pos), true);
					
					// If we found a better SID, update what is in the route with this one
					if (sid != null) {
						String msg = "Updated Route #" + rt.getID() + " SID from " + rt.getSID() + " to " + sid.getCode();
						msgs.add(msg);
						rt.setComments(rt.getComments() + "\r\n" + msg);
						rt.setSID(sid.getCode());
						rt.removeAirway(name);
						isUpdated = true;
						
						// Add the waypoints
						String startWP = wps.isEmpty() ? null : wps.get(0);
						LinkedList<NavigationDataBean> waypoints = new LinkedList<NavigationDataBean>(sid.getWaypoints(startWP));
						for (Iterator<NavigationDataBean> ni = waypoints.descendingIterator(); ni.hasNext(); )
							rt.insertWaypoint(ni.next(), sid.getName());
					} else {
						String msg = "Invalid SID - " + rt.getSID() + ", disabling Route #" + rt.getID();
						msgs.add(msg);
						rt.setComments(rt.getComments() + "\r\n" + msg);
						rt.setActive(false);
						isUpdated = true;
					}
				}
				
				// Load and validate the STAR
				TerminalRoute star = navdao.getRoute(rt.getAirportA(), TerminalRoute.Type.STAR, rt.getSTAR());
				if ((star == null) && (rt.getSTAR() != null)) {
					int pos = rt.getSTAR().indexOf('.');
					String name = rt.getSTAR().substring(0, pos);
					star = navdao.getRoute(rt.getAirportA(), TerminalRoute.Type.STAR, rt.getSTAR(), true);
					
					// If we still can't find a STAR, find one that uses this transition and runway
					if (star == null)
						star = navdao.getRoute(rt.getAirportA(), TerminalRoute.Type.STAR, "%" + rt.getSTAR().substring(pos), true);
					
					// If we found a better STAR, update what is in the route with this one
					if (star != null) {
						String msg = "Updated Route #" + rt.getID() + " STAR from " + rt.getSTAR() + " to " + star.getCode();
						msgs.add(msg);
						rt.setComments(rt.getComments() + "\r\n" + msg);
						rt.setSTAR(star.getCode());
						rt.removeAirway(name);
						isUpdated = true;
						
						// Add the waypoints
						String endWP = wps.isEmpty() ? null : wps.getLast();
						for (Iterator<NavigationDataBean> ni = star.getWaypoints(endWP).iterator(); ni.hasNext(); )
							rt.addWaypoint(ni.next(), star.getName());
					} else {
						String msg = "Invalid STAR - " + rt.getSTAR() + ", disabling Route #" + rt.getID();
						msgs.add(msg);
						rt.setComments(rt.getComments() + "\r\n" + msg);
						rt.setActive(false);
						isUpdated = true;
					}
				}
				
				// Save the updated route
				if (isUpdated) updateCount++;
				if (isUpdated && saveChanges) {
					ctx.startTX();
					rwdao.write(rt);
					rwdao.activate(rt.getID(), rt.getActive());
					ctx.commitTX();
				}
			}
			
			// Save status messages
			ctx.setAttribute("msgs", msgs, REQUEST);
			ctx.setAttribute("updateCount", Integer.valueOf(updateCount), REQUEST);
			ctx.setAttribute("isPreview", Boolean.valueOf(!saveChanges), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/dspRouteUpdate.jsp");
		result.setSuccess(true);
	}
}