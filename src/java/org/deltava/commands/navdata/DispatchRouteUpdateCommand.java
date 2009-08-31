// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.6
 * @since 2.6
 */

public class DispatchRouteUpdateCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			GetNavRoute navdao = new GetNavRoute(con);
			SetACARSRoute rwdao = new SetACARSRoute(con);
			
			// TODO: Remove this transaction after testing
			ctx.startTX();
			
			// Get the routes
			int updateCount = 0;
			Collection<String> msgs = new ArrayList<String>();
			GetACARSRoute rdao = new GetACARSRoute(con);
			Collection<DispatchRoute> routes = rdao.getAll(true, true);
			for (Iterator<DispatchRoute> i = routes.iterator(); i.hasNext(); ) {
				DispatchRoute rt = i.next();
				boolean isUpdated = false;
				
				// Get the SID/STAR transition if different
				List<String> wps = StringUtils.split(rt.getRoute(), " ");
				
				// Load and validate the SID
				TerminalRoute sid = navdao.getRoute(rt.getAirportD(), TerminalRoute.SID, rt.getSID());
				if ((sid == null) && (rt.getSID() != null)) {
					int pos = rt.getSID().indexOf('.');
					String name = rt.getSID().substring(0, pos);
					String newName = name.substring(0, name.length() - 1) + "%" + name.substring(pos);
					sid = navdao.getRoute(rt.getAirportD(), TerminalRoute.SID, newName);
					
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
						for (Iterator<NavigationDataBean> ni = waypoints.descendingIterator(); ni.hasNext(); ) {
							NavigationDataBean nd = ni.next();		
							rt.insertWaypoint(nd, sid.getName());
						}
					} else {
						String msg = "Invalid SID - " + rt.getSID() + ", disabling Route #" + rt.getID();
						msgs.add(msg);
						rt.setComments(rt.getComments() + "\r\n" + msg);
						rt.setActive(false);
						isUpdated = true;
					}
				}
				
				// Load and validate the STAR
				TerminalRoute star = navdao.getRoute(rt.getAirportA(), TerminalRoute.STAR, rt.getSTAR());
				if ((star == null) && (rt.getSTAR() != null)) {
					int pos = rt.getSTAR().indexOf('.');
					String name = rt.getSTAR().substring(0, pos);
					String newName = name.substring(0, name.length() - 2) + "%" + name.substring(pos);
					star = navdao.getRoute(rt.getAirportA(), TerminalRoute.STAR, newName);
					
					// If we found a better STAR, update what is in the route with this one
					if (star != null) {
						String msg = "Updated Route #" + rt.getID() + " STAR from " + rt.getSTAR() + " to " + star.getCode();
						msgs.add(msg);
						rt.setComments(rt.getComments() + "\r\n" + msg);
						rt.setSTAR(star.getCode());
						rt.removeAirway(name);
						isUpdated = true;
						
						// Add the waypoints
						String endWP = wps.isEmpty() ? null : wps.get(wps.size() - 1);
						for (Iterator<NavigationDataBean> ni = star.getWaypoints(endWP).iterator(); ni.hasNext(); ) {
							NavigationDataBean nd = ni.next();
							rt.addWaypoint(nd, star.getName());
						}
					} else {
						String msg = "Invalid STAR - " + rt.getSTAR() + ", disabling Route #" + rt.getID();
						msgs.add(msg);
						rt.setComments(rt.getComments() + "\r\n" + msg);
						rt.setActive(false);
						isUpdated = true;
					}
				}
				
				// Save the updated route
				if (isUpdated) {
					rwdao.write(rt);
					updateCount++;
				}
			}
			
			// Save status messages
			ctx.setAttribute("msgs", msgs, REQUEST);
			ctx.setAttribute("updateCount", Integer.valueOf(updateCount), REQUEST);
		} catch (DAOException de) {
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