// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.text.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.StatusUpdate;
import org.deltava.beans.system.InactivityPurge;
import org.deltava.beans.system.MessageTemplate;

import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.taskman.DatabaseTask;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to disable Users who have not logged in within a period of time.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InactivityUpdateTask extends DatabaseTask {

	private static final DateFormat _df = new SimpleDateFormat("MMMM dd yyyy");

	/**
	 * Initializes the Schedued Task.
	 */
	public InactivityUpdateTask() {
		super("Inactivity Status Update", InactivityUpdateTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute() {

		// Get the inactivity cutoff time
		int inactiveDays = SystemData.getInt("users.inactive_days");
		int notifyDays = SystemData.getInt("users.notify_days");
		
		// Check if we're in test mode
		boolean isTest = SystemData.getBoolean("smtp.testMode");

		try {
			// Initialize the DAOs
			GetInactivity idao = new GetInactivity(_con);
			SetStatusUpdate sudao = new SetStatusUpdate(_con);
			SetPilot pwdao = new SetPilot(_con);
			SetInactivity iwdao = new SetInactivity(_con);
			SetTS2Data ts2wdao = new SetTS2Data(_con);

			// Get the Message templates
			GetMessageTemplate mtdao = new GetMessageTemplate(_con);
			MessageTemplate imt = mtdao.get("USERINACTIVE");
			MessageTemplate nmt = mtdao.get("USERNOTIFY");

			// Figure out who we're operating as
			GetInactivity dao = new GetInactivity(_con);
			Pilot taskBy = dao.getByName(SystemData.get("users.tasks_by"), SystemData.get("airline.db"));
			
			// Initialize the mailer
			Mailer mailer = new Mailer(isTest ? null : taskBy);

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

					// Deactivate the Pilot
					p.setStatus(Pilot.INACTIVE);
					pwdao.write(p);
					
					// Clear TS2 credentials
					if (SystemData.getBoolean("airline.voice.ts2.enabled"))
						ts2wdao.delete(p.getID());

					// Send notification message
					mailer.setContext(mctxt);
					//mailer.send(p);
				} else {
					log.warn("Spurious Purge entry for Pilot ID " + ip.getID());
				}

				// Clear the inactivity record
				iwdao.delete(ip.getID());
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
					mailer.setContext(mctxt);
					//mailer.send(p);
				}
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			throw new RuntimeException(de);
		}

		// Log completion
		log.info("Completed");
	}
}