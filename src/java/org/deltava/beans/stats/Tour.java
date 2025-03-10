// Copyright 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightData;
import org.deltava.beans.schedule.ScheduleEntry;
import org.deltava.beans.system.AirlineInformation;

/**
 * A bean to store Flight Tour data.
 * @author Luke
 * @version 11.6
 * @since 10.0
 */

public class Tour extends DatabaseDocumentBean implements Auditable, ComboAlias, ViewEntry, FeedbackBean {

	private String _name;
	private TourStatus _status = TourStatus.PLANNING;
	private boolean _active;
	private Instant _startDate;
	private Instant _endDate;
	private final Collection<OnlineNetwork> _networks = new TreeSet<OnlineNetwork>();
	private boolean _acarsRequired;
	private boolean _allowOffline;
	private boolean _matchEQ;
	private boolean _matchLeg;
	
	private final List<ScheduleEntry> _flights = new ArrayList<ScheduleEntry>();
	private int _flightCount;
	
	private final Collection<TourProgress> _progress = new TreeSet<TourProgress>(Collections.reverseOrder());
	private final Collection<Feedback> _feedback = new LinkedHashSet<Feedback>();
	
	private AirlineInformation _owner;
	
	/**
	 * Creates the bean.
	 * @param name the name
	 */
	public Tour(String name) {
		super();
		setName(name);
	}
	
	/**
	 * Returns the Tour name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns whether the Tour is active.
	 * @return TRUE if active, otherwise FALSE
	 */
	public boolean getActive() {
		return _active;
	}
	
	/**
	 * Returns the status of the Tour.
	 * @return the TourStatus
	 */
	public TourStatus getStatus() {
		return _status;
	}
	
	/**
	 * Returns whether ACARS flights are required for this Tour.
	 * @return TRUE if ACARS required, otherwise FALSE
	 */
	public boolean getACARSOnly() {
		return _acarsRequired;
	}
	
	/**
	 * Returns whether offline flights are permitted for this Tour.
	 * @return TRUE if offline flights are permitted, otherwise FALSE
	 */
	public boolean getAllowOffline() {
		return _allowOffline || _networks.isEmpty();
	}
	
	/**
	 * Returns whether the equipment used for a flight must match the equipment in the Tour definition. 
	 * @return TRUE if equipment type must match, otherwise FALSE
	 */
	public boolean getMatchEquipment() {
		return _matchEQ;
	}
	
	/**
	 * Returns whether the flight number and leg used for a flight must match the flight and leg in the Tour definition. 
	 * @return TRUE if flight number and leg must match, otherwise FALSE
	 */
	public boolean getMatchLeg() {
		return _matchLeg;
	}
	
	/**
	 * Returns the Online Networks eligibile for use in this Tour.
	 * @return a Collection of OnlineNetworks
	 */
	public Collection<OnlineNetwork> getNetworks() {
		return _networks;
	}
	
	@Override
	public Collection<Feedback> getFeedback() {
		return _feedback;
	}
	
	/**
	 * Returns the Tour start date.
	 * @return the start date/time
	 */
	public Instant getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the Tour end date.
	 * @return the end date/time
	 */
	public Instant getEndDate() {
		return _endDate;
	}
	
	/**
	 * Returns the Virtual Airline that owns this Tour.
	 * @return an AirlineInformation
	 */
	public AirlineInformation getOwner() {
		return _owner;
	}
	
	/**
	 * Returns wether the Tour is active and in effect on a given date.
	 * @param dt the date/time, or null for now
	 * @return TRUE if the Tour is active and the date is within the start/end dates
	 */
	public boolean isActiveOn(Instant dt) {
		Instant d = (dt == null) ? Instant.now() : dt;
		return _active && d.isAfter(_startDate) && d.isBefore(_endDate);
	}
	
	/**
	 * Returns the ordered list of Flights for this Tour.
	 * @return a List of ScheduleEntry beans
	 */
	public List<ScheduleEntry> getFlights() {
		return List.copyOf(_flights);
	}
	
	/**
	 * Returns the number of Flight legs in this Tour.
	 * @return the number of legs
	 */
	public int getFlightCount() {
		return _flights.isEmpty() ? _flightCount : _flights.size();
	}
	
	/**
	 * Returns the leg number for a given Flight.
	 * @param f a Flight
	 * @return the leg number, or zero if not matched
	 */
	public int getLegIndex(FlightData f) {
		for (int x = 0; x < _flights.size(); x++) {
			Flight leg = _flights.get(x);
			if (legMatches(f, leg))
				return x + 1;
		}
		
		return 0;
	}
	
	/**
	 * Returns the IDs of the Pilots that have completed this Tour.
	 * @return a Collection of Pilot database IDs
	 */
	public Collection<Integer> getCompletionIDs() {
		return _progress.stream().filter(tp -> (tp.getLegs() == getFlightCount())).map(TourProgress::getID).collect(Collectors.toSet());
	}
	
	/**
	 * Returns the IDs of Pilots that have completed at least one Flight in this Tour.
	 * @return a Collection of TourProgress beans
	 */
	public Collection<TourProgress> getProgress() {
		return _progress;
	}
	
	/**
	 * Clears the list of Flights.
	 */
	public void clearFlights() {
		_flights.clear();
	}
	
	/**
	 * Clears the list of Online Networks.
	 */
	public void clearNetworks() {
		_networks.clear();
	}
	
	@Override
	public boolean isCrossApp() {
		return false;
	}
	
	/**
	 * Updates the Tour name.
	 * @param name the name
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the status of the Tour.
	 * @param st the TourStatus
	 */
	public void setStatus(TourStatus st) {
		_status = st;
	}
	
	/**
	 * Updates whether the Tour is active.
	 * @param isActive TRUE if acrtive, otherwise FALSE
	 */
	public void setActive(boolean isActive) {
		_active = isActive;
		if (_active)
			_status = TourStatus.COMPLETE;
	}
	
	/**
	 * Updates whether ACARS flights are required for this Tour.
	 * @param acarsOnly TRUE if ACARS required, otherwise FALSE
	 */
	public void setACARSOnly(boolean acarsOnly) {
		_acarsRequired = acarsOnly;
	}
	
	/**
	 * Updates whether offline flights are permitted for this Tour.
	 * @param allowOffline TRUE if offline flights permitted, otherwise FALSE
	 */
	public void setAllowOffline(boolean allowOffline) {
		_allowOffline = allowOffline;
	}

	/**
	 * Updates whether the equipment used for a flight must match the equipment in the Tour definition. 
	 * @param matchEQ TRUE if equipment type must match, otherwise FALSE
	 */
	public void setMatchEquipment(boolean matchEQ) {
		_matchEQ = matchEQ;
	}

	/**
	 * Returns whether the flight number and leg used for a flight must match the flight and leg in the Tour definition. 
	 * @param matchLeg TRUE if flight number and leg must match, otherwise FALSE
	 */
	public void setMatchLeg(boolean matchLeg) {
		_matchLeg = matchLeg;
	}
	
	/**
	 * Adds a valid Online Network to this Tour. 
	 * @param net an OnlineNetwork
	 */
	public void addNetwork(OnlineNetwork net) {
		_networks.add(net);
	}
	
	/**
	 * Updates the Start date for this Tour.
	 * @param dt the start date/time
	 * @throws NullPointerException if dt is null
	 * @throws IllegalArgumentException if the start date is after the end date
	 */
	public void setStartDate(Instant dt) {
		if ((_endDate != null) && !dt.isBefore(_endDate))
			throw new IllegalArgumentException("Start Date " + dt + " after " + _endDate);
		
		_startDate = dt;
	}
	
	/**
	 * Updates the End date for this Tour.
	 * @param dt the end date/time
	 * @throws NullPointerException if dt is null
	 * @throws IllegalArgumentException if the end date is before the start date
	 */
	public void setEndDate(Instant dt) {
		if ((_startDate != null) && !dt.isAfter(_startDate))
			throw new IllegalArgumentException("End Date " + dt + " before " + _startDate);
		
		_endDate = dt;
	}
	
	/**
	 * Adds a Flight leg to this Tour.
	 * @param se a ScheduleEntry
	 */
	public void addFlight(ScheduleEntry se) {
		_flights.add(se);
	}
	
	/**
	 * Updates the number of Flight legs in this Tour.
	 * @param cnt the number of legs
	 * @throws IllegalStateException if flights have already been added to this Tour
	 */
	public void setFlightCount(int cnt) {
		if (!_flights.isEmpty())
			throw new IllegalStateException("Flights already popualated");
		
		_flightCount = cnt; 
	}
	
	/**
	 * Updates the Virtual Airline that owns this Tour.
	 * @param ai an AirlineInformation beans
	 */
	public void setOwner(AirlineInformation ai) {
		_owner = ai;
	}

	/**
	 * Adds a Pilot's Tour progress ID to this Tour.
	 * @param tp a TourProgress bean
	 */
	public void addProgress(TourProgress tp) {
		_progress.add(tp);
	}
	
	@Override
	public void addFeedback(Feedback tf) {
		_feedback.add(tf);
	}
	
	/**
	 * Checks whether a Flight matches a particular Tour leg.
	 * @param f the Flight to check
	 * @param leg the Tour leg
	 * @return TRUE if the flight numbers and route pairs match, otherwise FALSE
	 */
	public boolean legMatches(FlightData f, Flight leg) {
		boolean isOK = !_matchLeg || (FlightNumber.compare(f, leg) == 0); 
		boolean legOK = f.matches(leg);
		return  isOK && legOK;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return getHexID();
	}
	
	@Override
	public String getAuditID() {
		return getHexID();
	}
	
	@Override
	public String toString() {
		return _name;
	}

	@Override
	public String getRowClassName() {
		if (!_active) return "warn";
		Instant now = Instant.now();
		if (now.isBefore(_startDate))
			return "opt1";
		if (now.isAfter(_endDate))
			return "opt3";

		return null;
	}
}