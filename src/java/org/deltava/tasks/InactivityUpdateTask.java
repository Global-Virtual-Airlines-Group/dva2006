// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;
import org.deltava.security.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to disable Users who have not logged in within a period of time.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InactivityUpdateTask extends Task {

	private final DateFormat _df = new SimpleDateFormat("MMMM dd yyyy");

	/**
	 * Initializes the Schedued Task.
	 */
	public InactivityUpdateTask() {
		super("Inactivity Status Update", InactivityUpdateTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute(TaskContext ctx) {

		// Get the inactivity cutoff time
		int inactiveDays = SystemData.getInt("users.inactive_days");
		int notifyDays = SystemData.getInt("users.notify_days");
		
		// Check if we're in test mode
		boolean isTest = SystemData.getBoolean("smtp.testMode");
		
		// Get the System authenticator
		Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);

		try {
			Connection con = ctx.getConnection();
			
			// Initialize the DAOs
			GetInactivity idao = new GetInactivity(con);
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			SetPilot pwdao = new SetPilot(con);
			SetInactivity iwdao = new SetInactivity(con);

			// Get the Message templates
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			MessageTemplate imt = mtdao.get("USERINACTIVE");
			MessageTemplate nmt = mtdao.get("USERNOTIFY");

			// Figure out who we're operating as
			GetInactivity dao = new GetInactivity(con);
			Pilot taskBy = dao.getByName(SystemData.get("users.tasks_by"), SystemData.get("airline.db"));
			
			// Get the pilots to deactivate
			Collection<InactivityPurge> purgeBeans = dao.getPurgeable(true);
			Map<Integer, Pilot> pilots = dao.getByID(purgeBeans, "PILOTS");
			for (Iterator<InactivityPurge> i = purgeBeans.iterator(); i.hasNext();) {
				InactivityPurge ip = i.next();
				Pilot p = pilots.get(new Integer(ip.getID()));
				if (p != null) {
					log.warn("Marking " + p.getName() + " Inactive");

					// Create the StatusUpdate bean
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
					upd.setAuthorID(taskBy.getID());
					upd.setCreatedOn(new Date());
					upd.setDescription("Marked Inactive due to no logins within " + inactiveDays + " days");
					sudao.write(upd);

					// Create the Message Context
					MessageContext mctxt = new MessageContext();
					mctxt.setTemplate(imt);
					mctxt.addData("user", taskBy);
					mctxt.addData("pilot", p);
					mctxt.addData("lastLogin", (p.getLastLogin() == null) ? "NEVER" : _df.format(p.getLastLogin()));
					
					// Start a transaction
					ctx.startTX();

					// Deactivate the Pilot
					p.setStatus(Pilot.INACTIVE);
					pwdao.write(p);
					
					// Remove the user from any destination directories
					if (auth instanceof MultiAuthenticator) {
						MultiAuthenticator mAuth = (MultiAuthenticator) auth;
						if (auth instanceof SQLAuthenticator) {
							SQLAuthenticator sqlAuth = (SQLAuthenticator) auth;
							sqlAuth.setConnection(con);
							mAuth.removeDestination(p);
							sqlAuth.clearConnection();
						} else
							mAuth.removeDestination(p);
					}
					
					// Remove the inactivity entry
					iwdao.delete(ip.getID());
					
					// Commit
					ctx.commitTX();

					// Send notification message
					Mailer mailer = new Mailer(isTest ? null : taskBy);
					mailer.setContext(mctxt);
					mailer.send(p);
				} else {
					log.warn("Spurious Purge entry for Pilot ID " + ip.getID());
					iwdao.delete(ip.getID());
				}
			}
			
			// Get the Pilots to notify
			Collection<Pilot> nPilots = dao.getInactivePilots(notifyDays);
			for (Iterator<Pilot> i = nPilots.iterator(); i.hasNext();) {
				Pilot p = i.next();
				
				// Check if we've been notified already
				InactivityPurge ip = idao.getInactivity(p.getID());
				if ((ip == null) || (!ip.isNotified())) {
					log.warn("Notifying " + p.getName());
					
					// Create the StatusUpdate bean
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.INACTIVITY);
					upd.setAuthorID(taskBy.getID());
					upd.setCreatedOn(new Date());
					upd.setDescription("Sent Reminder due to no logins within " + notifyDays + " days");
					sudao.write(upd);

					// Create the Message Context
					MessageContext mctxt = new MessageContext();
					mctxt.setTemplate(nmt);
					mctxt.addData("user", taskBy);
					mctxt.addData("pilot", p);
					mctxt.addData("lastLogin", (p.getLastLogin() == null) ? "NEVER" : _df.format(p.getLastLogin()));

					// Make sure we have a notification entry
					iwdao.setInactivity(p.getID(), inactiveDays - notifyDays, true);
					
					// Send the message
					Mailer mailer = new Mailer(isTest ? null : taskBy);
					mailer.setContext(mctxt);
					mailer.send(p);
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Log completion
		log.info("Completed");
	}
}