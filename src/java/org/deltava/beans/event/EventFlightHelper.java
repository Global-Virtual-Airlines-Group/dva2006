// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.Helper;
import org.deltava.beans.flight.*;

/**
 * A utility class to determine what Online Events a flight may have participated in. 
 * @author Luke
 * @version 10.0
 * @since 9.0
 */

@Helper(FlightReport.class)
public class EventFlightHelper {

	private final FlightReport _fr;
	private int _timeBuffer = 30;
	private final Collection<String> _msgs = new ArrayList<String>();
	
	/**
	 * Initializes the Helper.
	 * @param fr a Flight Report
	 */
	public EventFlightHelper(FlightReport fr) {
		super();
		_fr = fr;
	}
	
	/**
	 * Returns the analysis messages.
	 * @return a Collection of messages
	 */
	public Collection<String> getMessages() {
		return _msgs;
	}
	
	/**
	 * Sets the time buffer around the Online Event start/end times.
	 * @param min the buffer in minutes
	 */
	public void setTimeBuffer(int min) {
		_timeBuffer = Math.max(0, min);
	}
	
	/**
	 * Returns whether the Flight matches a particular Online Event. 
	 * @param e an Event
	 * @return TRUE if the flight matches participation criteria, otherwise FALSE
	 */
	public boolean matches(Event e) {
		try {
			// Check that the network/route match
			if (e.getNetwork() != _fr.getNetwork()) throw new IllegalArgumentException("Flight not flown on " + e.getNetwork());
			if (!(_fr instanceof FDRFlightReport)) throw new IllegalArgumentException("Flight not flown using ACARS/XACARS/simFDR");
			if (e.getRoutes().stream().filter(_fr::matches).findAny().isEmpty()) throw new IllegalArgumentException("Flight not valid Event route");
		
			// Calculate takeoff/landing times
			FDRFlightReport ffr = (FDRFlightReport) _fr;
			if (ffr.getTakeoffTime() == null) throw new IllegalArgumentException("No takeoff time recorded");
			if (ffr.getLandingTime() == null) throw new IllegalArgumentException("No landing time recorded");
		
			// Check takeoff / landing times - only give them grace on the back end
			Instant edb = e.getEndTime().plus(_timeBuffer, ChronoUnit.MINUTES);
			boolean ttOK = ffr.getTakeoffTime().isAfter(e.getStartTime()) && ffr.getTakeoffTime().isBefore(e.getEndTime());
			if (!ttOK) {
				Duration td = Duration.between(e.getStartTime(), ffr.getTakeoffTime());
				_msgs.add(String.format("Takeoff time %d minutes %s Online Event started", Long.valueOf(td.toMinutes()), td.isNegative() ? "before" : "after"));	
			} else
				_msgs.add("Takeoff during Online Event");
			
			boolean ltOK = ffr.getLandingTime().isAfter(e.getStartTime()) && ffr.getLandingTime().isBefore(e.getEndTime());
			boolean ltGrace = ffr.getLandingTime().isAfter(e.getStartTime()) && ffr.getLandingTime().isBefore(edb);
			if (!ltOK) {
				Duration ld = Duration.between(e.getEndTime(), ffr.getLandingTime());
				_msgs.add(String.format("Landing time %d minutes %s Online Event ended", Long.valueOf(ld.toMinutes()), ld.isNegative() ? "before" : "after"));
			} else
				_msgs.add("Landing during Online Event");
			
			return ttOK && (ltOK || ltGrace);
		} catch (IllegalArgumentException ie) {
			_msgs.add(ie.getMessage());
			return false;
		}
	}
}