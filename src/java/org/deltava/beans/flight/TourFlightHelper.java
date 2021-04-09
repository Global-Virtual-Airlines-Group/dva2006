// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.deltava.beans.*;
import org.deltava.beans.stats.Tour;

import org.deltava.comparators.FlightReportComparator;

/**
 * A helper class to calculate Flight Tour eligibility.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

@Helper(Tour.class)
public class TourFlightHelper {
	
	private final FlightData _fr;
	private final boolean _allowSubmitted;
	
	private final List<FlightReport> _flights = new ArrayList<FlightReport>();
	private final List<String> _msgs = new ArrayList<String>();

	/**
	 * Initializes the Helper.
	 * @param fr the FlightData to calculate eligbility for
	 * @param allowSubmitted TRUE to allow submitted previous flights to count for eligibility, otherwise FALSE
	 */
	public TourFlightHelper(FlightData fr, boolean allowSubmitted) {
		super();
		_fr = fr;
		_allowSubmitted = allowSubmitted;
	}
	
	/**
	 * Returns the eligibility messages.
	 * @return a Collection of messages
	 */
	public Collection<String> getMessages() {
		return _msgs;
	}
	
	/**
	 * Returns if there is an eligibility message.
	 * @return TRUE if there is at least one message, otherwise FALSE
	 */
	public boolean hasMessage() {
		return (_msgs.size() > 1);
	}
	
	private boolean filter(FlightReport fr) {
		if (fr.getStatus() == FlightStatus.OK) return true;
		return _allowSubmitted && ((fr.getStatus() == FlightStatus.SUBMITTED) || (fr.getStatus() == FlightStatus.HOLD));
	}
	
	/**
	 * Adds previous flights to the Helper. 
	 * @param flights a Collection of FlightReport beans
	 */
	public void addFlights(Collection<FlightReport> flights) {
		flights.stream().filter(this::filter).forEach(_flights::add);
		_flights.sort(new FlightReportComparator(FlightReportComparator.SUBMISSION));
	}
	
	/**
	 * Returns whether a given Flight Tour has been completed.
	 * @param t the Tour
	 * @return TRUE if completed, otherwise FALSE
	 */
	public boolean isComplete(Tour t) {
		Collection<Integer> idxs = IntStream.rangeClosed(1, t.getFlightCount()).boxed().collect(Collectors.toCollection(TreeSet::new));
		_flights.stream().mapToInt(fr -> fr.getDatabaseID(DatabaseID.TOUR)).filter(id -> (id != 0)).boxed().forEach(idxs::remove);
		return idxs.isEmpty();
	}
	
	/**
	 * Determines whether the Flight is a valid Flight Tour leg.
	 * @param t the Tour to check
	 * @return the leg index within the Tour, or zero if not found
	 */
	public int isLeg(Tour t) {
		_msgs.clear();
		if (t == null) {
			_msgs.add("Tour not fond");
			return 0;
		}
		
		// Check that we match the tour
		int idx = t.getLegIndex(_fr);
		if (idx < 1) return 0;
		
		// Check if Tour is active
		if (!t.isActiveOn(_fr.getDate())) {
			_msgs.add(String.format("Tour %s not active on %D", t.getName(), _fr.getDate()));
			return 0;
		}
		
		// Check for ACARS usage
		if (t.getACARSOnly() && (_fr.getFDR() == null)) {
			_msgs.add(String.format("Tour %s requires ACARS/XACARS/simFDR", t.getName()));
			return 0;
		}
		
		// Check online network
		if (!t.getAllowOffline() && !t.getNetworks().isEmpty()) { // FIXME: What happens if allowOffline is false but network is different?
			if ((_fr.getNetwork() == null) || !t.getNetworks().contains(_fr.getNetwork())) {
				_msgs.add(String.format("Tour %s requires %s", t.getName(), t.getNetworks()));
				return 0;
			}
		}
		
		// Check equipment
		Flight lg = t.getFlights().get(idx - 1);
		if (t.getMatchEquipment() && !_fr.getEquipmentType().equals(lg.getEquipmentType())) {
			_msgs.add(String.format("Tour %s Leg %d requires %s", t.getName(), Integer.valueOf(idx), lg.getEquipmentType()));
			return 0;
		}
		
		// Check if the flight number needs to match
		if (t.getMatchLeg() && (FlightNumber.compare(_fr, lg) != 0)) {
			_msgs.add(String.format("Tour %s Leg %d should be filed as %s" ,t.getName(), Integer.valueOf(idx), lg.getFlightCode()));
			return 0;
		}
		
		// Have we not completed this leg, but have completed the previous?
		Flight pl = (idx > 1) ? t.getFlights().get(idx - 2) : null;
		List<FlightReport> tourFlights = _flights.stream().filter(f -> f.getDatabaseID(DatabaseID.TOUR) == t.getID()).collect(Collectors.toList()); // check if previous legs are in time frame for tour
		boolean isLegComplete = tourFlights.stream().anyMatch(f -> t.legMatches(_fr, f));
		boolean isPrevComplete = (pl == null) || tourFlights.stream().anyMatch(f -> t.legMatches(f, pl));
		if (isLegComplete)
			_msgs.add(String.format("Tour %s Leg %d already completed", t.getName(), Integer.valueOf(idx)));
		if (!isPrevComplete)
			_msgs.add(String.format("Tour %s Leg %d not completed", t.getName(), Integer.valueOf(idx - 1)));
		
		return (!isLegComplete && isPrevComplete) ? idx : 0;
	}
}