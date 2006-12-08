// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.ConnectionEntry;

import org.deltava.dao.*;

import org.deltava.taskman.DatabaseTask;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge old ACARS log data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSDataPurgeTask extends DatabaseTask {

	/**
	 * Initializes the Task.
	 */
	public ACARSDataPurgeTask() {
		super("ACARS Log Purge", ACARSDataPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute() {
		// Determine the purge intervals
		int msgPurge = SystemData.getInt("log.purge.messages", 72);
		int flightPurge = SystemData.getInt("log.purge.flights", 48);
		int conPurge = SystemData.getInt("log.purge.cons", 60);
		log.info("Executing");
		
		try {
			Connection con = getConnection();
			
			// Remove messages and flights
			SetACARSLog wdao = new SetACARSLog(con);
			log.info("Purged " + wdao.purgeMessages(msgPurge) + " text messages");
			log.info("Purged " + wdao.purgeFlights(flightPurge) + " flight entries");
			
			// Get connections
			GetACARSLog dao = new GetACARSLog(con);
			Collection cons = dao.getUnusedConnections(conPurge);
			
			// Purge the connections
			int purgeCount = 0;
			for (Iterator i = cons.iterator(); i.hasNext(); ) {
				ConnectionEntry ce = (ConnectionEntry) i.next();
				try {
					wdao.deleteConnection(ce.getID());
					purgeCount++;
					log.info("Purged Connection " + StringUtils.formatHex(ce.getID()));
				} catch (DAOException de) {
					log.warn("Error purging Connection " + StringUtils.formatHex(ce.getID()) + " - " + de.getMessage());
				}
			}
			
			// Log purge count
			log.info("Purged " + purgeCount + " connection entries");
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			release();
		}

		log.info("Completed");
	}
}