// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.*;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to send reminders and reject held Flight Reports. 
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

public class HeldFlightTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public HeldFlightTask() {
		super("Held Flight Report Processor", HeldFlightTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Calculate threhsholds
		Instant warnDate = Instant.now().minusSeconds(Duration.ofDays(SystemData.getInt("pirep.max_hold_days", 5)).toSeconds());
		Instant rejectDate = warnDate.plusSeconds(Duration.ofDays(2).toSeconds());
		
		Mailer m = new Mailer(ctx.getUser());
		
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			GetFlightReports frdao = new GetFlightReports(con);
			SetFlightReport frwdao = new SetFlightReport(con);
			
			// Load the flights
			SequencedCollection<FlightReport> pireps = frdao.getByStatus(List.of(FlightStatus.HOLD), "DATE");
			for (FlightReport fr : pireps) {
				Pilot p = pdao.get(fr.getAuthorID());
				Pilot dp = pdao.get(fr.getDatabaseID(DatabaseID.DISPOSAL));
				MessageContext mctx = new MessageContext();
				mctx.addData("user", ctx.getUser());
				mctx.addData("pilot", p);
				m.setCC(dp);

				Duration d = Duration.between(fr.getDisposedOn(), Instant.now());
				if (fr.getDisposedOn().isAfter(rejectDate)) {
					log.warn("Automatically rejecting Flight Report {} from {} after {} days", Integer.valueOf(fr.getID()), p.getName(), Long.valueOf(d.toDays()));
					mctx.setTemplate(mtdao.get("PIREPREJECT"));
					fr.addStatusUpdate(0, HistoryType.LIFECYCLE, "Automatic Rejection after review delay");
					if (fr.getDatabaseID(DatabaseID.TOUR) != 0) {
						fr.setDatabaseID(DatabaseID.TOUR, 0);
						fr.addStatusUpdate(0, HistoryType.SYSTEM, "Removed Flight Tour leg");
					}
					
					frwdao.writeHistory(fr.getStatusUpdates(), ctx.getDB());
					frwdao.dispose(ctx.getDB(), ctx.getUser(), fr, FlightStatus.REJECTED);
				} else if (fr.getDisposedOn().isAfter(warnDate)) {
					log.warn("Sending reminder for Flight Report {} from {} after {} days", Integer.valueOf(fr.getID()), p.getName(), Long.valueOf(d.toDays()));
					mctx.setTemplate(mtdao.get("PIREPWARN"));
				}

				// Add notification attributes
				mctx.addData("flightLength", Double.valueOf(fr.getLength() / 10.0));
				mctx.addData("flightDate", StringUtils.format(fr.getDate(), p.getDateFormat()));
				mctx.addData("pirep", fr);
				m.setContext(mctx);
				m.send(p);
			}
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			logError("Error warning Held Flight Reports", de);
		} finally {
			ctx.release();
		}
		
		log.info("Processing Complete");
	}
}