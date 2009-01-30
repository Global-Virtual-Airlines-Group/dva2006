// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.navdata.*;
import org.deltava.beans.servinfo.*;
import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetServInfo;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the "who is online" page.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class FlightBoardCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the network name and wether we display a map
		String networkName = (String) ctx.getCmdParameter(ID, SystemData.get("online.default_network"));
		boolean showMap = "map".equals(ctx.getCmdParameter(OPERATION, "false"));
		OnlineNetwork network = OnlineNetwork.valueOf(SystemData.get("online.default_network"));
		try {
			network = OnlineNetwork.valueOf(networkName.toUpperCase());
		} catch (Exception e) {
			// empty
		}

		try {
			// Get the network info
			File f = new File(SystemData.get("online." + network.toString().toLowerCase() + ".local.info"));
			GetServInfo sidao = new GetServInfo(new FileInputStream(f));
			NetworkInfo info = sidao.getInfo(network);

			// Get the connection
			Connection con = ctx.getConnection();
			
			// Get the DAO and execute, and highlight our pilots
			GetPilotOnline dao = new GetPilotOnline(con);
			Map<String, Integer> idMap = dao.getIDs(network);
			info.setPilotIDs(idMap);

			// Get airports to load from DAFIF data and highlight our airline's code
			List codes = (List) SystemData.getObject("online.highlightCodes");
			Set<String> airportIDs = new HashSet<String>();
			for (Iterator<Pilot> i = info.getPilots().iterator(); i.hasNext();) {
				Pilot usr = i.next();
				for (Iterator ci = codes.iterator(); (ci.hasNext() && !usr.isHighlighted());) {
					String code = (String) ci.next();
					if (usr.getCallsign().startsWith(code))
						usr.setHighlighted(true);
				}
				
				if (!usr.getAirportD().hasPosition())
					airportIDs.add(usr.getAirportD().getICAO());
				if (!usr.getAirportA().hasPosition())
					airportIDs.add(usr.getAirportA().getICAO());
			}
			
			// Get the airports only
			GetNavData navdao = new GetNavData(con);
			NavigationDataMap navaids = navdao.getByID(airportIDs);
			navaids.filter(NavigationDataBean.AIRPORT);

			// Update the pilots with the proper airport data
			for (Iterator<Pilot> i = info.getPilots().iterator(); i.hasNext();) {
				Pilot usr = i.next();

				// Update the departure airport
				if (navaids.contains(usr.getAirportD().getICAO())) {
					AirportLocation al = (AirportLocation) navaids.get(usr.getAirportD().getICAO());
					usr.getAirportD().setName(al.getName());
					usr.getAirportD().setLocation(al.getLatitude(), al.getLongitude());
				}

				// Update the arrival airport
				if (navaids.contains(usr.getAirportA().getICAO())) {
					AirportLocation al = (AirportLocation) navaids.get(usr.getAirportA().getICAO());
					usr.getAirportA().setName(al.getName());
					usr.getAirportA().setLocation(al.getLatitude(), al.getLongitude());
				}
			}

			// Save the network information in the request
			ctx.setAttribute("netInfo", info, REQUEST);
		} catch (Exception e) {
			throw new CommandException(e);
		} finally {
			ctx.release();
		}

		// Load the network names and save in the request
		ctx.setAttribute("networks", SystemData.getObject("online.networks"), REQUEST);
		ctx.setAttribute("network", network, REQUEST);

		// Forward to the display JSP
		CommandResult result = ctx.getResult();
		result.setURL(showMap ? "/jsp/event/flightBoardMap.jsp" : "/jsp/event/flightBoard.jsp");
		result.setSuccess(true);
	}
}