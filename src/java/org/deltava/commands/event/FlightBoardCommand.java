// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.net.*;
import java.sql.Connection;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.deltava.beans.navdata.*;
//import org.deltava.beans.schedule.Airport;

import org.deltava.beans.servinfo.*;
import org.deltava.commands.*;

import org.deltava.dao.GetNavData;
import org.deltava.dao.GetPilotOnline;
import org.deltava.dao.http.GetServInfo;

import org.deltava.util.system.SystemData;

/**
 * A Command to display the "who is online" page.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightBoardCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(FlightBoardCommand.class);

	/**
	 * Helper method to open a connection to a particular URL.
	 */
	private HttpURLConnection getURL(String dataURL) throws IOException {
		URL url = new URL(dataURL);
		log.debug("Loading data from " + url.toString());
		return (HttpURLConnection) url.openConnection();
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the network name and wether we display a map
		String networkName = (String) ctx.getCmdParameter(ID, SystemData.get("online.default_network"));
		boolean showMap = "map".equals(ctx.getCmdParameter(OPERATION, "false"));

		// Get VATSIM/IVAO/ACARS members
		try {
			NetworkInfo info = null;
			Connection con = ctx.getConnection();
			
			// Load via HTTP if not ACARS
			if (networkName.equals("ACARS")) {
				ServInfoProvider acarsInfo = (ServInfoProvider) SystemData.getObject(SystemData.ACARS_POOL);
				info = acarsInfo.getNetworkInfo();
			} else {
				// Connect to info URL
				HttpURLConnection urlcon = getURL(SystemData.get("online." + networkName.toLowerCase() + ".status_url"));

				// Get network status
				GetServInfo sdao = new GetServInfo(urlcon);
				NetworkStatus status = sdao.getStatus(networkName);
				urlcon.disconnect();

				// Get network status
				urlcon = getURL(status.getDataURL());
				GetServInfo idao = new GetServInfo(urlcon);
				idao.setBufferSize(32768);
				info = idao.getInfo(networkName);
				urlcon.disconnect();

				// Get the DAO and execute, and highlight our pilots
				GetPilotOnline dao = new GetPilotOnline(con);
				Map<String, Integer> idMap = dao.getIDs(networkName);
				info.setPilotIDs(idMap);
			}
			
			// Get Online Members and load DAFIF data only if we are uncached
			if (!info.getCached()) {
				// Get airports to load from DAFIF data and highlight our airline's code
				List codes = (List) SystemData.getObject("online.highlightCodes");
				Set<String> airportIDs = new HashSet<String>();
				for (Iterator<Pilot> i = info.getPilots().iterator(); i.hasNext();) {
					Pilot usr = i.next();
					for (Iterator ci = codes.iterator(); (ci.hasNext() && !usr.isHighlighted()); ) {
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
			}

			// Save the network information in the request
			ctx.setAttribute("netInfo", info, REQUEST);
		} catch (Exception e) {
			throw new CommandException(e);
		} finally {
			ctx.release();
		}

		// Load the network names and save in the request
		List networkNames = (List) SystemData.getObject("online.networks");
		ctx.setAttribute("networks", networkNames, REQUEST);
		ctx.setAttribute("network", networkName, REQUEST);

		// Forward to the display JSP
		CommandResult result = ctx.getResult();
		result.setURL(showMap ? "/jsp/event/flightBoardMap.jsp" : "/jsp/event/flightBoard.jsp");
		result.setSuccess(true);
	}
}