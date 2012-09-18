// Copyright 2007, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.CalendarEntry;
import org.deltava.beans.academy.*;

/**
 * A JSP Function Library to define FlightAcademy-related functions.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class AcademyFunctions {
	
	// static class
	private AcademyFunctions() {
		super();
	}

	/**
	 * Returns whether this object is an Instruction Flight.
	 * @param ce the CalendarEntry
	 * @return TRUE if the entry is an InstructionFlight bean, otherwise FALSE
	 */
	public static boolean isFlight(CalendarEntry ce) {
		return (ce instanceof InstructionFlight);
	}
	
	/**
	 * Returns whether this object is an Instruction Session.
	 * @param ce the CalendarEntry
	 * @return TRUE if the entry is an InstructionSession bean, otherwise FALSE
	 */
	public static boolean isSession(CalendarEntry ce) {
		return (ce instanceof InstructionSession);
	}
	
	/**
	 * Returns whether this object is an Instructor busy time entry.
	 * @param ce the CalendarEntry
	 * @return TRUE if the entry is an InstructionBusy bean, otherwise FALSE
	 */
	public static boolean isBusy(CalendarEntry ce) {
		return (ce instanceof InstructionBusy);
	}
	
	/**
	 * Returns whether the Course is currently active.
	 * @param c the Course
	 * @return TRUE if the Course status is active, otherwise FALSE
	 */
	public static boolean isActive(Course c) {
		return ((c != null) && (c.getStatus() == Status.STARTED));
	}
}