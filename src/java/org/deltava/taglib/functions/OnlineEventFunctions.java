// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.*;

import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.event.*;

/**
 * A JSP Function Library to store Online Event-related functions.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class OnlineEventFunctions {

	/**
	 * Returns whether an Online Event is in progress.
	 * @param e the Event bean
	 * @return TRUE if the Event is active, otherwise FALSE
	 */
	public static boolean isOpen(Event e) {
		return (e.getStatus() == Event.ACTIVE);
	}

	/**
	 * Returns whether a Pilot is signed up for an Online Event. <i>The Signups for the Online Event
	 * must be populated prior to this being called</i>.
	 * @param e the Event bean
	 * @param pilotID the Pilot's database ID
	 * @return TRUE if the Pilot is signed up, otherwise FALSE
	 * @see Event#isSignedUp(int)
	 */
	public static boolean isSignedUp(Event e, int pilotID) {
		return e.isSignedUp(pilotID);
	}

	/**
	 * Returns whether a Pilot has a Flight Assignment for an Online Event. <i>The Flight Assignments
	 * for the Online Event must be populated prior to this being called</i>.
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
	
	/**
	 * Returns all Signups for a particular Online Event route. <i>The Signups for the Online Event
	 * must be populated prior to this being called</i>.
	 * @param e the Event bean
	 * @param routeID the route database ID
	 * @return a Collection of Signup beans
	 */
	public static Collection<Signup> routeSignups(Event e, int routeID) {
		Collection<Signup> results = new LinkedHashSet<Signup>();
		for (Iterator<Signup> i = e.getSignups().iterator(); i.hasNext(); ) {
			Signup s = i.next();
			if (s.getRouteID() == routeID)
				results.add(s);
		}
		
		return results;
	}
}