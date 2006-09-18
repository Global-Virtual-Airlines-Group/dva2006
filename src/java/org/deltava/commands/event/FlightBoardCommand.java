// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.navdata.*;
import org.deltava.beans.servinfo.*;
import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetServInfo;

import org.deltava.util.ThreadUtils;
import org.deltava.util.servinfo.ServInfoLoader;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the "who is online" page.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightBoardCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(FlightBoardCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the network name and wether we display a map
		String network = (String) ctx.getCmdParameter(ID, SystemData.get("online.default_network"));
		boolean showMap = "map".equals(ctx.getCmdParameter(OPERATION, "false"));
		
		// Get the network info from the cache
		NetworkInfo info = GetServInfo.getCachedInfo(network);
		ServInfoLoader loader = new ServInfoLoader(SystemData.get("online." + network.toLowerCase() + ".status_url"), network);

		// Check if we're already loading
		if (info == null) {
			log.info("Loading " + network + " data in main thread");
			Thread t = null;
			synchronized (ServInfoLoader.class) {
				t = new Thread(loader, network + " ServInfo Loader");
				t.setDaemon(true);
				t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
				ServInfoLoader.addLoader(network, t);
			}
			
			// Wait for the thread to exit
			int totalTime = 0;
			while (ThreadUtils.isAlive(t) && (totalTime < 10000)) {
				totalTime += 250;
				ThreadUtils.sleep(250);
			}
			
			// If the thread hasn't died, then kill it
			if (totalTime >= 10000) {
				ThreadUtils.kill(t, 1000);
				info = new NetworkInfo(network);
			} else
				info = loader.getInfo();
		} else if (info.getExpired()) {
			synchronized (ServInfoLoader.class) {
				if (!ServInfoLoader.isLoading(network)) {
					log.info("Spawning new ServInfo load thread");
					Thread t = new Thread(loader, network + " ServInfo Loader");
					t.setDaemon(true);
					t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
					ServInfoLoader.addLoader(network, t);
				} else
					log.warn("Already loading " + network + " information");
			}
		}

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and execute, and highlight our pilots
			GetPilotOnline dao = new GetPilotOnline(con);
			Map<String, Integer> idMap = dao.getIDs(network);
			info.setPilotIDs(idMap);

			// Get Online Members and load DAFIF data only if we are uncached
			if (!info.getCached()) {
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
			}

			// Save the network information in the request
			ctx.setAttribute("netInfo", info, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
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