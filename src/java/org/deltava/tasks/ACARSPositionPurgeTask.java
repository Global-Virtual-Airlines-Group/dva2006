// Copyright 2010, 2011, 2012, 2018, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;
import org.deltava.dao.ipc.GetACARSPool;

import org.deltava.taskman.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge orphaned ACARS position entries.
 * @author Luke
 * @version 11.1
 * @since 3.2
 */

public class ACARSPositionPurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public ACARSPositionPurgeTask() {
		super("ACARS Position Purge", ACARSPositionPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		log.info("Executing");

		// Get active flights
		GetACARSPool acdao = new GetACARSPool();
		Collection<Integer> activeIDs = acdao.getFlightIDs();

		int flightPurge = SystemData.getInt("log.purge.flights", 48);
		try {
			Connection con = ctx.getConnection();

			// Get the flight IDs
			GetACARSPurge prdao = new GetACARSPurge(con);
			Collection<Integer> IDs = prdao.getPositionFlightIDs(Math.round(flightPurge * 1.5f));
			IDs.removeAll(activeIDs);

			// Get the DAOs
			GetUserData uddao = new GetUserData(con);
			GetFlightReports frdao = new GetFlightReports(con);
			GetExam exdao = new GetExam(con);
			SetExam exwdao = new SetExam(con);

			// Loop through the IDs
			GetACARSPositions dao = new GetACARSPositions(con);
			SetACARSPurge wdao = new SetACARSPurge(con);
			SetACARSArchive arcdao = new SetACARSArchive(con);
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext();) {
				int id = i.next().intValue();

				// Load the flight ID
				FlightInfo fInfo = dao.getInfo(id);
				if (fInfo == null) {
					int purgeCount = wdao.deletePositions(id);
					log.warn("Flight ID {} not found, purged {} positions", Integer.valueOf(id), Integer.valueOf(purgeCount));

				// If we're supposed to be archived, archive positions
				} else if (fInfo.getArchived()) {
					SequencedCollection<ACARSRouteEntry> entries = dao.getRouteEntries(id, false);
					arcdao.archive(id, entries);
					log.warn("Flight ID {}, moved {} positions", Integer.valueOf(id), Integer.valueOf(entries.size()));

				// If we don't have a PIREP, nuke
				} else if (!fInfo.getHasPIREP()) {
					ctx.startTX();
					wdao.deleteInfo(id);
					int purgeCount = wdao.deletePositions(id);
					ctx.commitTX();
					log.warn("Flight ID {} has no PIREP, purged {} positions", Integer.valueOf(id), Integer.valueOf(purgeCount));

				// Validate that the PIREP does exist
				} else {
					UserData ud = uddao.get(fInfo.getAuthorID());
					FDRFlightReport afr = frdao.getACARS(ud.getDB(), id);
					CheckRide cr = exdao.getACARSCheckRide(id);
					if (afr == null) {
						ctx.startTX();
						if ((cr != null) && (cr.getFlightID() > 0)) {
							cr.setFlightID(0);
							if (cr.getStatus() == TestStatus.SUBMITTED)
								cr.setStatus(TestStatus.NEW);

							exwdao.write(cr);
							log.warn("Reset Check Ride {} for Flight ID {}", Integer.valueOf(cr.getID()), Integer.valueOf(id));
						}

						wdao.deleteInfo(id);
						int purgeCount = wdao.deletePositions(id);
						ctx.commitTX();
						log.warn("Flight ID {} has no PIREP, purged {} positions", Integer.valueOf(id), Integer.valueOf(purgeCount));
					} else if ((afr.getStatus() == FlightStatus.OK) || (afr.getStatus() == FlightStatus.REJECTED)) {
						SequencedCollection<ACARSRouteEntry> entries = dao.getRouteEntries(id, false);
						arcdao.archive(id, entries);
						log.warn("Flight ID {} is not archived, moved {} positions", Integer.valueOf(id), Integer.valueOf(entries.size()));
					}
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Completed");
	}
}