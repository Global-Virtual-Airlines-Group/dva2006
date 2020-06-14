// Copyright 2005, 2006, 2008, 2009, 2010, 2012, 2014, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.navdata.*;
import org.deltava.beans.servinfo.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.EnumUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the "who is online" page.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class FlightBoardCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the network name and info
		OnlineNetwork network = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("id"), OnlineNetwork.valueOf(SystemData.get("online.default_network")));
		NetworkInfo info = ServInfoHelper.getInfo(network);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and execute, and highlight our pilots
			if (!info.hasPilotIDs()) {
				GetPilotOnline dao = new GetPilotOnline(con);
				Map<String, Integer> idMap = dao.getIDs(network);
				info.setPilotIDs(idMap);
			}

			// Get airports to load from DAFIF data and highlight our airline's code
			List<?> codes = (List<?>) SystemData.getObject("online.highlightCodes");
			Collection<String> airportIDs = new HashSet<String>();
			for (Pilot usr : info.getPilots()) {
				for (Iterator<?> ci = codes.iterator(); (ci.hasNext() && !usr.isHighlighted());) {
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
			navaids.filter(Collections.singleton(Navaid.AIRPORT));

			// Update the pilots with the proper airport data
			for (Pilot usr : info.getPilots()) {
				// Update the departure airport
				if (navaids.contains(usr.getAirportD().getICAO()) && !usr.getAirportD().hasPosition()) {
					AirportLocation al = (AirportLocation) navaids.get(usr.getAirportD().getICAO());
					usr.getAirportD().setName(al.getName());
					usr.getAirportD().setLocation(al.getLatitude(), al.getLongitude());
				}

				// Update the arrival airport
				if (navaids.contains(usr.getAirportA().getICAO()) && !usr.getAirportA().hasPosition()) {
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
		result.setURL("/jsp/event/flightBoard.jsp");
		result.setSuccess(true);
	}
}