// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.*;

import org.deltava.dao.*;
import org.deltava.dao.ipc.GetACARSPool;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge old ACARS log data.
 * @author Luke
 * @version 4.1
 * @since 1.0
 */

public class ACARSDataPurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public ACARSDataPurgeTask() {
		super("ACARS Log Purge", ACARSDataPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute(TaskContext ctx) {
		log.info("Executing");

		// Get active flights
		GetACARSPool acdao = new GetACARSPool();
		Collection<Integer> activeIDs = acdao.getFlightIDs();

		// Determine the purge intervals
		int flightPurge = SystemData.getInt("log.purge.flights", 48);
		int conPurge = SystemData.getInt("log.purge.cons", 48);
		try {
			Connection con = ctx.getConnection();

			// Ensure all archived data is in the right place
			GetACARSPurge prdao = new GetACARSPurge(con);
			GetACARSPositions posdao = new GetACARSPositions(con);
			SetACARSArchive awdao = new SetACARSArchive(con);
			SetACARSPurge pwdao = new SetACARSPurge(con);
			Collection<Integer> unsynchedIDs = prdao.getUnsynchedACARSFlights();
			for (Integer ID : unsynchedIDs) {
				Collection<? extends RouteEntry> entries = posdao.getRouteEntries(ID.intValue(), false);
				log.warn("Moved unsynchronized ACARS flight " + ID.toString() + " to archive");
				awdao.archive(ID.intValue(), entries);
			}
			
			unsynchedIDs = prdao.getUnsynchedXACARSFlights();
			for (Integer ID : unsynchedIDs) {
				Collection<? extends RouteEntry> entries = posdao.getXACARSEntries(ID.intValue());
				log.warn("Moved unsynchronized XACARS flight " + ID.toString() + " to archive");
				awdao.archive(ID.intValue(), entries);
			}

			// Remove old flights and position reports without a flight report
			Collection<Integer> purgedIDs = pwdao.purgeFlights(flightPurge, activeIDs);
			log.warn("Purged " + purgedIDs.size() + " flight entries - " + purgedIDs);

			// Purge old takeoffs
			log.warn("Purged " + pwdao.purgeTakeoffs(flightPurge) + " takeoff/landing entries");

			// Get connections
			GetACARSPurge dao = new GetACARSPurge(con);
			Collection<ConnectionEntry> cons = dao.getUnusedConnections(conPurge);

			// Purge the connections
			int purgeCount = 0;
			for (ConnectionEntry ce : cons) {
				if (!ce.getDispatch()) {
					try {
						pwdao.deleteConnection(ce.getID());
						purgeCount++;
						log.info("Purged Connection " + StringUtils.formatHex(ce.getID()));
					} catch (DAOException de) {
						log.error("Error purging Connection " + StringUtils.formatHex(ce.getID()) + " - " + de.getMessage());
					}
				}
			}

			log.info("Purged " + purgeCount + " connection entries");
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Completed");
	}
}