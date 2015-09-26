// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.acars.RouteEntry;

import org.deltava.dao.*;
import org.deltava.dao.file.SetSerializedPosition;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to copy ACARS position data to the file system.  
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public class PositionArchiveTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public PositionArchiveTask() {
		super("ACARS Position Archive", PositionArchiveTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		log.info("Executing");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the IDs
			GetACARSPurge pdao = new GetACARSPurge(con);
			pdao.setQueryMax(500);
			Collection<Integer> IDs = pdao.getDatabaseFlights();
			
			// Write each flight entry to the database
			GetACARSPositions prdao = new GetACARSPositions(con);
			SetACARSArchive awdao = new SetACARSArchive(con);
			for (Integer ID : IDs) {
				String hash = Integer.toHexString(ID.intValue() % 2048);
				File path = new File(SystemData.get("path.pos_archive"), hash); path.mkdirs();
				Collection<? extends RouteEntry> entries = prdao.getRouteEntries(ID.intValue(), true);
				try (OutputStream os = new FileOutputStream(new File(path, Integer.toHexString(ID.intValue()) + ".dat"))) {
					try (OutputStream bos = new BufferedOutputStream(os, 20480)) {
						SetSerializedPosition pwdao = new SetSerializedPosition(bos);
						pwdao.archivePositions(ID.intValue(), entries);	
					}
					
					awdao.clear(ID.intValue());
				} catch (IOException ie) {
					log.warn(ie.getMessage());
				}
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}