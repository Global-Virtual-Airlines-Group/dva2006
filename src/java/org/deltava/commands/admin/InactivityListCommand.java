// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.InactivityPurge;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to manually display Pilots scheduled to be marked for Inactivity.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class InactivityListCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the inactivity cutoff times
		int inactiveDays = SystemData.getInt("users.inactive_days");
		int notifyDays = SystemData.getInt("users.notify_days");
		
		try {
			Connection con = ctx.getConnection();
			
			// Initialize the DAOs
			GetInactivity dao = new GetInactivity(con);
			
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
			Map<Pilot, String> results = new TreeMap<Pilot, String>(new PilotComparator(PersonComparator.LASTLOGIN));
			Map<Integer, Pilot> pilots = dao.getByID(purgeBeans.keySet(), "PILOTS");
			for (Iterator<Map.Entry<Integer, InactivityPurge>> i = purgeBeans.entrySet().iterator(); i.hasNext();) {
				Map.Entry<Integer, InactivityPurge> me = i.next();
				InactivityPurge ip = me.getValue();
				Integer id = me.getKey();
				Pilot p = pilots.get(id);
				if (p != null) {
					boolean noWarn = !ip.isNotified();
					if (noWarn)
						results.put(p, "Inactive after no participation in " + inactiveDays + " days");
					else if (p.getLoginCount() == 0)
						results.put(p, "No first login in " + notifyDays + " days");
					else 
						results.put(p, "Inactive after no logins in " + ip.getInterval() + " days");
				}
			}
			
			// Get the Pilots to notify
			Collection<Integer> nPilotIDs = dao.getInactivePilots(notifyDays);
			nPilotIDs.removeAll(noWarnIDs);
			Collection<Pilot> nPilots = dao.getByID(nPilotIDs, "PILOTS").values();
			for (Iterator<Pilot> i = nPilots.iterator(); i.hasNext();) {
				Pilot p = i.next();
				InactivityPurge ip = dao.getInactivity(p.getID());
				if (ip == null)
					results.put(p, "Sent Reminder due to no logins within " + notifyDays + " days");
			}
			
			// Save the results
			ctx.setAttribute("results", results, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/inactivityList.jsp");
		result.setSuccess(true);
	}
}