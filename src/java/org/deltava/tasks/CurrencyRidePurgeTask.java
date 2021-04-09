// Copyright 2017, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.MessageTemplate;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge old Currency Check Rides.
 * @author Luke
 * @version 10.0
 * @since 8.0
 */

public class CurrencyRidePurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public CurrencyRidePurgeTask() {
		super("Currency Ride Purge", CurrencyRidePurgeTask.class);
	}

	/**
	 * Executes the Task.
	 * @param ctx the TaskContext
	 */
	@Override
	protected void execute(TaskContext ctx) {
		log.info("Starting");
		int purgeInterval = SystemData.getInt("users.transfer_max", 30);

		try {
			Connection con = ctx.getConnection();
			GetExam exdao = new GetExam(con);
			GetPilot pdao = new GetPilot(con);
			SetExam exwdao = new SetExam(con);
			SetStatusUpdate suwdao = new SetStatusUpdate(con);
			
			// Get the Message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			MessageTemplate mt = mtdao.get("CURRCACNEL");
			
			// Load the check rides
			List<CheckRide> rides = exdao.getPendingRides(purgeInterval, RideType.CURRENCY);
			for (CheckRide cr : rides) {
				Pilot p = pdao.get(cr.getAuthorID());
				
				// Make a status update
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.CURRENCY);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Currency Ride canceled after " + purgeInterval + " days");
				
				// Delete the check ride
				ctx.startTX();
				exwdao.delete(cr);
				suwdao.write(upd, ctx.getDB());
				
				// Log the deletion
				log.warn("Deleting currency Check Ride for " + p.getName() + " after " + purgeInterval + " days");

				// Commit
				ctx.commitTX();
				
				// Send the notification
				MessageContext mctxt = new MessageContext();
				mctxt.setTemplate(mt);
				mctxt.addData("user", ctx.getUser());
				mctxt.addData("pilot", p);
				mctxt.addData("checkRide", cr);
				mctxt.addData("rejectComments", "Currency Check Ride automatically deleted after " + purgeInterval + " days");

				// Send a notification message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctxt);
				mailer.send(p);
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