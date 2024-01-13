// Copyright 2010, 2011, 2014, 2015, 2016, 2017, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.apache.logging.log4j.Level;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download Online Tracks via the ServInfo feed from all Online networks.
 * @author Luke
 * @version 11.1
 * @since 3.1
 */

public class OnlineTrackTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public OnlineTrackTask() {
		super("Online Track Download", OnlineTrackTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Loop through the networks
		Collection<?> networkNames = (Collection<?>) SystemData.getObject("online.networks");
		boolean logAll = SystemData.getBoolean("online.log_all");
		for (Iterator<?> i = networkNames.iterator(); i.hasNext(); ) {
			OnlineNetwork network = OnlineNetwork.valueOf(String.valueOf(i.next()).toUpperCase());
			log.info("Loading {} information for {}", network, SystemData.get("airline.code"));
			
			// Get the network info
			NetworkInfo info = ServInfoHelper.getInfo(network);
			log.log((info.getValidDate() == null) ? Level.WARN : Level.INFO, String.format("Loaded data for %s valid as of %s", network, info.getValidDate()));
			if (info.getValidDate() == null) continue;
			
			// Load the network IDs
			Map<String, Integer> networkIDs = new HashMap<String, Integer>();
			try {
				Connection con = ctx.getConnection();
				GetPilotOnline podao = new GetPilotOnline(con);
				networkIDs.putAll(podao.getIDs(network));
				info.setPilotIDs(networkIDs);
				log.debug("Loaded {} {} IDs", Integer.valueOf(networkIDs.size()), network);
				
				// Aggregate the data for VATSIM
				if (logAll && ((ctx.getLastRun() == null) || (info.getValidDate().isAfter(ctx.getLastRun())))) {
					Collection<ConnectedUser> users = new ArrayList<ConnectedUser>(info.getPilots());
					users.addAll(info.getControllers());
					int timeDelta = (int) ((ctx.getLastRun() == null) ? 2 : Math.max(10, (System.currentTimeMillis() - ctx.getLastRun().toEpochMilli()) / 60000L));

					SetOnlineTime otwdao = new SetOnlineTime(con);
					otwdao.write(network, users, timeDelta);
					log.debug("Wrote {} stats records for {}", Integer.valueOf(users.size()), network);
				}
				
				// Loop through the pilots
				int flightCount = 0;
				GetOnlineTrack otdao = new GetOnlineTrack(con);
				for (Pilot p : info.getPilots()) {
					if (!networkIDs.containsKey(String.valueOf(p.getID())))
						continue;
					else if ((p.getPilotID() == 0) || !p.isPopulated() || !p.hasLocation())
						continue;
					else if ((p.getAirportD().getICAO().length() != 4) || (p.getAirportA().getICAO().length() != 4))
						continue;
					
					// Check if we've already opened a flight track for this Pilot
					ctx.startTX();
					SetOnlineTrack otwdao = new SetOnlineTrack(con);
					int trackID = otdao.getTrackID(p.getPilotID(), network, info.getValidDate(), p.getAirportD(), p.getAirportA());
					if (trackID == 0) {
						String rt = p.getRoute();
						if ((rt.length() > 0) && (rt.charAt(0) == '+'))
							rt = rt.substring(1);
						
						trackID = otwdao.writeTrack(p.getPilotID(), network, p.getAirportD(), p.getAirportA(), rt.trim());
					}
					
					// Create the position
					PositionData pd = new PositionData(info.getValidDate(), new GeoPosition(p.getLatitude(), p.getLongitude(), p.getAltitude()));
					pd.setFlightID(trackID);
					pd.setAirSpeed(p.getGroundSpeed());
					pd.setHeading(p.getHeading());
					flightCount++;
					
					// Write the position and commit
					otwdao.writePosition(pd);
					ctx.commitTX();
				}
				
				// Log flight records written
				log.info("Saved {} {} position records", Integer.valueOf(flightCount), network);
				
				// Purge old flight entries
				SetOnlineTrack otwdao = new SetOnlineTrack(con);
				int purgeCount = otwdao.purgeAll(72);
				log.info("Purged {} old flight tracks after 72 hours", Integer.valueOf(purgeCount));
			} catch (DAOException de) {
				ctx.rollbackTX();
				log.atError().withThrowable(de).log("Error loading {} data", network);
			} finally {
				ctx.release();
			}
		}
	}
}