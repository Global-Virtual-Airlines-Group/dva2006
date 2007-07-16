// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.ConnectionEntry;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge old ACARS log data.
 * @author Luke
 * @version 1.0
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
		
		// Determine the purge intervals
		int flightPurge = SystemData.getInt("log.purge.flights", 48);
		int conPurge = SystemData.getInt("log.purge.cons", 60);
		int statsPurge = SystemData.getInt("log.purge.acars_stats", 96);
		try {
			Connection con = ctx.getConnection();
			
			// Ensure all archived data is in the right place
			SetACARSLog wdao = new SetACARSLog(con);
			wdao.synchronizeArchive();
			
			// Remove old flights and position reports without a flight report
			log.warn("Purged " + wdao.purgeFlights(flightPurge) + " flight entries");
			
			// Purge old stats
			log.warn("Purged " + wdao.purgeLogs(statsPurge) + " command statistics entries");
			
			// Get connections
			GetACARSLog dao = new GetACARSLog(con);
			Collection<ConnectionEntry> cons = dao.getUnusedConnections(conPurge);
			
			// Purge the connections
			int purgeCount = 0;
			for (Iterator<ConnectionEntry> i = cons.iterator(); i.hasNext(); ) {
				ConnectionEntry ce = i.next();
				try {
					wdao.deleteConnection(ce.getID());
					purgeCount++;
					log.info("Purged Connection " + StringUtils.formatHex(ce.getID()));
				} catch (DAOException de) {
					log.error("Error purging Connection " + StringUtils.formatHex(ce.getID()) + " - " + de.getMessage());
				}
			}
			
			// Log purge count
			log.info("Purged " + purgeCount + " connection entries");
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Completed");
	}
}