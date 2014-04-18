// Copyright 2010, 2011, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download Online Tracks via the ServInfo feed from all Online networks.
 * @author Luke
 * @version 5.4
 * @since 3.1
 */

public class OnlineTrackTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public OnlineTrackTask() {
		super("Online Track Download", OnlineTrackTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		// Loop through the networks
		Collection<?> networkNames = (Collection<?>) SystemData.getObject("online.networks");
		for (Iterator<?> i = networkNames.iterator(); i.hasNext(); ) {
			OnlineNetwork network = OnlineNetwork.valueOf(String.valueOf(i.next()));
			log.info("Loading " + network + " information for " + SystemData.get("airline.code"));
			
			// Get the network info
			NetworkInfo info = ServInfoHelper.getInfo(network);
			
			// Load the network IDs
			Map<String, Integer> networkIDs = new HashMap<String, Integer>();
			try {
				Connection con = ctx.getConnection();
				GetPilotOnline podao = new GetPilotOnline(con);
				networkIDs.putAll(podao.getIDs(network));
				info.setPilotIDs(networkIDs);
				if (log.isDebugEnabled())
					log.debug("Loaded " + networkIDs.size() + " " + network + " IDs");
				
				// Loop through the pilots
				int flightCount = 0;
				GetOnlineTrack otdao = new GetOnlineTrack(con);
				SetOnlineTrack otwdao = new SetOnlineTrack(con);
				for (Iterator<Pilot> pi = info.getPilots().iterator(); pi.hasNext(); ) {
					Pilot p = pi.next();
					if (!networkIDs.containsKey(String.valueOf(p.getID())))
						continue;
					else if ((p.getAirportD().getICAO().length() != 4) || (p.getAirportA().getICAO().length() != 4))
						continue;
					else if (p.getPilotID() == 0)
						continue;
					
					// Check if we've already opened a flight track for this Pilot
					ctx.startTX();
					int trackID = otdao.getTrackID(p.getPilotID(), network, info.getValidDate(), p.getAirportD(), p.getAirportA());
					if (trackID == 0) {
						String rt = p.getRoute();
						if ((rt.length() > 0) && (rt.charAt(0) == '+'))
							rt = rt.substring(1);
						
						trackID = otwdao.writeTrack(p.getPilotID(), network, p.getAirportD(), p.getAirportA(), rt.trim());
					}
					
					// Create the position
					PositionData pd = new PositionData(info.getValidDate());
					pd.setFlightID(trackID);
					pd.setPosition(p.getLatitude(), p.getLongitude(), p.getAltitude());
					pd.setAirSpeed(p.getGroundSpeed());
					pd.setHeading(p.getHeading());
					flightCount++;
					
					// Write the position and commit
					otwdao.writePosition(pd);
					ctx.commitTX();
				}
				
				// Log flight records written
				log.info("Saved " + flightCount + " " + network + " position records");
				
				// Purge old flight entries
				int purgeCount = otwdao.purgeAll(48);
				log.info("Purged " + purgeCount + " old flight tracks");
			} catch (DAOException de) {
				ctx.rollbackTX();
				log.error("Error loading " + network + " data", de);
			} finally {
				ctx.release();
			}
		}
	}
}