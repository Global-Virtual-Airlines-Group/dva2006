// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.system.*;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;
import org.deltava.security.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to disable Users who have not logged in within a period of time.
 * @author Luke
 * @version 2.8
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
			GetInactivity dao = new GetInactivity(con);
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			SetAcademy cwdao = new SetAcademy(con);
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			SetPilot pwdao = new SetPilot(con);
			SetInactivity iwdao = new SetInactivity(con);
			
			// Load pending flight academy users
			Collection<Course> pC = cdao.getByStatus("C.PILOT_ID", Course.PENDING);
			pC.addAll(cdao.getByStatus("C.PILOT_ID", Course.STARTED));
			Map<Integer, Course> courses = CollectionUtils.createMap(pC, "pilotID");

			// Get the Message templates
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			MessageTemplate imt = mtdao.get("USERINACTIVE");
			MessageTemplate nmt = mtdao.get("USERNOTIFY");

			// Figure out who we're operating as
			GetPilotDirectory pddao = new GetPilotDirectory(con);
			Pilot taskBy = pddao.getByCode(SystemData.get("users.tasks_by"));
			
			// Get the pilots to mark without warning
			Map<Integer, InactivityPurge> purgeBeans = CollectionUtils.createMap(dao.getPurgeable(),  "ID");
			Collection<Integer> noWarnIDs = dao.getRepeatInactive(notifyDays, inactiveDays, 2);
			for (Iterator<Integer> i = noWarnIDs.iterator(); i.hasNext(); ) {
				Integer id = i.next();
				if (!purgeBeans.containsKey(id)) {
					InactivityPurge ip = dao.getInactivity(id.intValue());
					if (ip == null) {
						ip = new InactivityPurge(id.intValue());
						ip.setInterval(notifyDays);
						purgeBeans.put(id, ip);
					} else
						i.remove();
				}
			}
			
			// Get the pilots to deactivate
			Map<Integer, Pilot> pilots = dao.getByID(purgeBeans.keySet(), "PILOTS");
			for (Iterator<Map.Entry<Integer, InactivityPurge>> i = purgeBeans.entrySet().iterator(); i.hasNext();) {
				Map.Entry<Integer, InactivityPurge> me = i.next();
				InactivityPurge ip = me.getValue();
				Integer id = me.getKey();
				Pilot p = pilots.get(id);
				if (p != null) {
					boolean noWarn = !ip.isNotified();
					if (noWarn)
						log.warn("Marking " + p.getName() + " Inactive after no participation in " + inactiveDays + " days");
					else if (p.getLoginCount() == 0)
						log.warn("Marking " + p.getName() + " Inactive after no first login in " + notifyDays + " days");
					else
						log.warn("Marking " + p.getName() + " Inactive after " + ip.getInterval() + " days");

					// Create the StatusUpdate bean
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
					upd.setAuthorID(taskBy.getID());
					upd.setCreatedOn(new Date());
					if (noWarn)
						upd.setDescription("Marked Inactive due to no participation within " + inactiveDays + " days");
					else if (p.getLoginCount() == 0)
						upd.setDescription("Marked Inactive after no first login in " + notifyDays + " days");
					else
						upd.setDescription("Marked Inactive due to no logins within " + ip.getInterval() + " days");
					
					// Create the Message Context
					MessageContext mctxt = new MessageContext();
					mctxt.setTemplate(imt);
					mctxt.addData("user", taskBy);
					mctxt.addData("pilot", p);
					mctxt.addData("lastLogin", (p.getLastLogin() == null) ? "NEVER" : _df.format(p.getLastLogin()));
					
					// Start a transaction
					ctx.startTX();
					
					// Write the update
					sudao.write(upd);
					
					// Check if we have a flight academy entry
					Course c = courses.get(id);
					if (c != null) {
						CourseComment cc = new CourseComment(c.getID(), upd.getAuthorID());
						cc.setCreatedOn(upd.getCreatedOn());
						cc.setText(upd.getDescription());
						log.warn("Removing " + p.getName() + " from " + c.getName() + " Flight Academy Course");
						
						// Mark as abandoned and save comment
						c.setStatus(Course.ABANDONED);
						cwdao.comment(cc);
						cwdao.write(c);
					}

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
			Collection<Integer> nPilotIDs = dao.getInactivePilots(notifyDays);
			nPilotIDs.removeAll(noWarnIDs);
			Collection<Pilot> nPilots = dao.getByID(nPilotIDs, "PILOTS").values();
			for (Iterator<Pilot> i = nPilots.iterator(); i.hasNext();) {
				Pilot p = i.next();
				
				// Check if we've been notified already
				InactivityPurge ip = dao.getInactivity(p.getID());
				if ((ip == null) || (!ip.isNotified())) {
					log.warn("Notifying " + p.getName());
					
					// Create the Message Context
					MessageContext mctxt = new MessageContext();
					mctxt.setTemplate(nmt);
					mctxt.addData("user", taskBy);
					mctxt.addData("pilot", p);
					mctxt.addData("lastLogin", (p.getLastLogin() == null) ? "NEVER" : _df.format(p.getLastLogin()));

					// Start the transaction
					ctx.startTX();
					
					// Create the StatusUpdate bean
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.INACTIVITY);
					upd.setAuthorID(taskBy.getID());
					upd.setCreatedOn(new Date());
					upd.setDescription("Sent Reminder due to no logins within " + notifyDays + " days");
					sudao.write(upd);

					// Make sure we have a notification entry
					iwdao.setInactivity(p.getID(), inactiveDays, true);
					
					// Commit the transaction
					ctx.commitTX();
					
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