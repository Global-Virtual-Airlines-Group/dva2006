// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;
import java.sql.Connection;


import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to copy ACARS position data to the file system.  
 * @author Luke
 * @version 6.2
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
			pdao.setQueryMax(6500);
			Collection<Integer> IDs = pdao.getDatabaseFlights();
			
			// Write each flight entry to the database
			long startTime = System.currentTimeMillis();
			GetACARSPositions prdao = new GetACARSPositions(con);
			SetACARSArchive awdao = new SetACARSArchive(con);
			for (Integer ID : IDs) {
				String hash = Integer.toHexString(ID.intValue() % 2048);
				File path = new File(SystemData.get("path.archive"), hash); path.mkdirs();
				byte[] data = prdao.getRawArchive(ID.intValue());
				File dt = new File(path, Integer.toHexString(ID.intValue()) + ".dat");
				if (dt.exists() && (dt.length() > 0)) {
					log.warn(dt.getAbsolutePath() + " already exists!");
					continue;
				}

				// Write to the file system
				try (OutputStream os = new FileOutputStream(dt)) {
					try (OutputStream bos = new GZIPOutputStream(os, 8192)) {
						bos.write(data);
					}
					
					awdao.clear(ID.intValue());
				} catch (IOException ie) {
					log.warn(ie.getMessage());
				}
				
				// Reset the connections
				if ((System.currentTimeMillis() - startTime) > 45_000) {
					ctx.release();
					con = ctx.getConnection();
					prdao = new GetACARSPositions(con);
					awdao = new SetACARSArchive(con);
					startTime = System.currentTimeMillis();
				}
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}