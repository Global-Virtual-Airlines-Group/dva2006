// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.Iterator;

import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.event.Event;

/**
 * A JSP Function Library to store Online Event-related functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class OnlineEventFunctions {

	/**
	 * Returns wether an Online Event is in progress.
	 * @param e the Event bean
	 * @return TRUE if the Event is active, otherwise FALSE
	 */
	public static boolean isOpen(Event e) {
		return (e.getStatus() == Event.ACTIVE);
	}

	/**
	 * Returns wether a Pilot is signed up for an Online Event. <i>The Signups for the Online Event must be
	 * populated prior to this being called</i>.
	 * @param e the Event bean
	 * @param pilotID the Pilot's database ID
	 * @return TRUE if the Pilot is signed up, otherwise FALSE
	 * @see Event#isSignedUp(int)
	 */
	public static boolean isSignedUp(Event e, int pilotID) {
		return e.isSignedUp(pilotID);
	}

	/**
	 * Returns wether a Pilot has a Flight Assignment for an Online Event. <i>The Flight Assignments for the Online
	 * Event must be populated prior to this being called</i>.
	 * @param e the Event bean
	 * @param pilotID the Pilot's database ID
	 * @return TRUE if the Pilot has a Flight Assignment, otherwise FALSE
	 */
	public static boolean isAssigned(Event e, int pilotID) {
		for (Iterator<AssignmentInfo> i = e.getAssignments().iterator(); i.hasNext();) {
			AssignmentInfo ai = i.next();
			if (ai.getPilotID() == pilotID)
				return true;
		}

		return false;
	}
}