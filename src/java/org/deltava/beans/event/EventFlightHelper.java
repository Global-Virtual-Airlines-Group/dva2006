// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.Helper;
import org.deltava.beans.flight.*;
import org.deltava.beans.servinfo.PositionData;

/**
 * A utility class to determine what Online Events a flight may have participated in. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

@Helper(FlightReport.class)
public class EventFlightHelper {

	private final FlightReport _fr;
	private final Collection<PositionData> _onlineData = new TreeSet<PositionData>();
	private String _msg;
	
	/**
	 * Initializes the Helper.
	 * @param fr a Flight Report
	 */
	public EventFlightHelper(FlightReport fr) {
		super();
		_fr = fr;
	}
	
	/**
	 * Adds the Flight's online track data.
	 * @param trackData a Collection of PositionData beans
	 */
	public void addOnlineTrack(Collection<PositionData> trackData) {
		_onlineData.addAll(trackData);
	}

	/**
	 * Returns the analysis message.
	 * @return the message
	 */
	public String getMessage() {
		return _msg;
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
			if (e.getRoutes().stream().filter(_fr::matches).findAny().isEmpty()) throw new IllegalArgumentException("Flight not valid Event route");
		
			// Calculate takeoff/landing times
			Instant takeoffTime = null; Instant landingTime = null;
			if (_fr instanceof FDRFlightReport) {
				FDRFlightReport ffr = (FDRFlightReport) _fr;
				takeoffTime = ffr.getTakeoffTime();
				landingTime = ffr.getLandingTime();
			} else {
				if (_onlineData.size() < 3) throw new IllegalArgumentException("Insufficient Online Track data for non-FDR Flight");
				takeoffTime = _onlineData.stream().filter(pd -> ((pd.getAirSpeed() > 90) && pd.distanceTo(_fr.getAirportD()) < 20)).map(PositionData::getDate).findFirst().orElse(null);
				landingTime = _onlineData.stream().filter(pd -> ((pd.getAirSpeed() < 30) && pd.distanceTo(_fr.getAirportA()) < 20)).map(PositionData::getDate).findFirst().orElse(null);
			}
		
			// Check if takeoff or landing was at a featured airport
			boolean hasFA = !e.getFeaturedAirports().isEmpty();
			boolean faTakeoff = !hasFA || e.getFeaturedAirports().contains(_fr.getAirportD());
			boolean faLanding = !hasFA || e.getFeaturedAirports().contains(_fr.getAirportA());
			
			// Check takeoff / landing times
			boolean ttOK = (takeoffTime != null) && takeoffTime.isAfter(e.getStartTime()) && takeoffTime.isBefore(e.getEndTime());
			boolean ltOK = (landingTime != null) && landingTime.isAfter(e.getStartTime()) && landingTime.isBefore(e.getEndTime());
		
			// If takeoff at a Featured Airport, ensure it occured within event times
			boolean takeoffOK = faTakeoff && ttOK;
			boolean landingOK = faLanding && ltOK;
			_msg = "TakeoffTimeOK=" + ttOK + ", LandingTimeOK=" + ltOK + ", TakeoffOK=" + takeoffOK + ", LandingOK=" + landingOK;
			return takeoffOK || landingOK;
		} catch (IllegalArgumentException ie) {
			_msg = ie.getMessage();
			return false;
		}
	}
}