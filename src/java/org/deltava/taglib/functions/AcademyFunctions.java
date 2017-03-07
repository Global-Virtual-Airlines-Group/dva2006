// Copyright 2007, 2010, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.CalendarEntry;
import org.deltava.beans.academy.*;
import org.deltava.beans.servinfo.PilotRating;

/**
 * A JSP Function Library to define FlightAcademy-related functions.
 * @author Luke
 * @version 7.2
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
	
	/**
	 * Filters out Pilot Ratings that have been achieved via a Flight Academy course.
	 * @param ratings a Collection of PilotRatings
	 * @param certs a Collection of Certifications
	 * @return a Collection of filtered PilotRatings
	 */
	public static Collection<PilotRating> filterRatings(Collection<PilotRating> ratings, Collection<Certification> certs) {
		Collection<String> codes = certs.stream().map(Certification::getNetworkRatingCode).filter(Objects::nonNull).collect(Collectors.toSet());
		return ratings.stream().filter(r -> !codes.contains(r.getRatingCode())).collect(Collectors.toSet());
	}
	
	/**
	 * Filters a list of Certifications to only include Certifications obtained by a Pilot.
	 * @param codes a Collection containing the codes of the Pilot's Ceritification
	 * @param allCerts a Collection containing all Certifications
	 * @return a Collection of filtered Certifications
	 */
	public static Collection<Certification> filterCerts(Collection<String> codes, Collection<Certification> allCerts) {
		return allCerts.stream().filter(c -> codes.contains(c.getCode())).collect(Collectors.toSet());
	}
}