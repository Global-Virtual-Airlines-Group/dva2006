// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.event.Event;

/**
 * A JSP Function Library to store Online Event-related functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
 
public class OnlineEventFunctions {

	public static boolean isOpen(Event e) {
		return (e.getStatus() == Event.ACTIVE);
	}
	
	public static boolean isSignedUp(Event e, int pilotID) {
		return e.isSignedUp(pilotID);
	}
	
	public static boolean isAssigned(Event e, int pilotID) {
		return e.isAssigned(pilotID);
	}
}