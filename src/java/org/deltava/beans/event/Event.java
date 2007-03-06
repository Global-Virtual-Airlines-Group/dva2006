// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.util.StringUtils;

/**
 * A class to store Online Event information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Event extends DatabaseBean implements ComboAlias, CalendarEntry {

    public static final int NET_VATSIM = 0;
    public static final int NET_IVAO = 1;
    public static final int NET_INTVAS = 2;
    
    /**
     * Online Network names.
     */
    public static final String[] NETWORKS = {OnlineNetwork.VATSIM, OnlineNetwork.IVAO, OnlineNetwork.INTVAS};
    
    public static final int OPEN = 0;
    public static final int CANCELED = 1;
    public static final int CLOSED = 2;
    public static final int ACTIVE = 3;
    public static final int COMPLETE = 4;
    
    /**
     * Event status names.
     */
    public static final String[] STATUS = {"Open", "Canceled", "Closed", "Active", "Complete"};
    
    private String _name;
    private String _briefing;
    private Date _startTime;
    private Date _endTime;
    private Date _signupDeadline;
    
    private int _status;
    private int _network;
    private boolean _canSignup;
    
    private Set<Chart> _charts;
    private Collection<FlightPlan> _plans;
    
    private Set<Signup> _signups;
    private Set<Route> _routes;
    private List<AssignmentInfo> _assignments;
    private Set<String> _eqTypes;
    
    /**
     * Creates a new Online Event.
     * @param name the event name
     * @throws NullPointerException if name is null
     * @see Event#getName()
     */
    public Event(String name) {
        super();
        setName(name);
        _charts = new TreeSet<Chart>();
        _plans = new ArrayList<FlightPlan>();
        _signups = new LinkedHashSet<Signup>();
        _assignments = new ArrayList<AssignmentInfo>();
        _eqTypes = new TreeSet<String>();
        _routes = new TreeSet<Route>();
    }
    
    /**
     * Returns the Online Event name.
     * @return the event name
     */
    public String getName() {
        return _name;
    }
    
    public String getComboName() {
        return getName();
    }
    
    public String getComboAlias() {
        return String.valueOf(getID());
    }
    
    /**
     * Returns the Online Event pilot briefing.
     * @return the briefing
     */
    public String getBriefing() {
        return _briefing;
    }
    
    /**
     * Null-safe helper method to compare dates.
     */
    private boolean before(Date d2) {
    	Date d = new Date();
    	return (d2 == null) ? true : d.before(d2);
    }
    
    /**
     * Returns the status of this Online Event.
     * @return the status code
     * @see Event#setStatus(int)
     * @see Event#getStatusName()
     */
    public int getStatus() {
        // If the event was canceled, then return that, otherwise calculate the status
        if (_status == Event.CANCELED)
            return _status;
        
        if (before(_signupDeadline)) {
            return Event.OPEN;
        } else if (before(_startTime)) {
            return Event.CLOSED;
        } else if (before(_endTime)) {
            return Event.ACTIVE;
        } else {
            return Event.COMPLETE;
        }
    }
    
    /**
     * Returns the status of this Online Event.
     * @return the status name
     * @see Event#getStatus()
     * @see Event#setStatus(int)
     */
    public String getStatusName() {
    	return STATUS[getStatus()];
    }
    
    /**
     * Returns the Online Network code for this Event.
     * @return the network code
     * @see Event#getNetworkName()
     * @see Event#setNetwork(int)
     * @see Event#setNetwork(String)
     */
    public int getNetwork() {
        return _network;
    }
    
    /**
     * Returns the Online Network name for this Event.
     * @return the network name
     * @see Event#getNetwork()
     * @see Event#setNetwork(int)
     * @see Event#setNetwork(String)
     */
    public String getNetworkName() {
    	return NETWORKS[getNetwork()];
    }
    
    /**
     * Returns the starting time for this Event.
     * @return the date/time the Event starts
     * @see Event#setStartTime(Date)
     * @see Event#getEndTime()
     * @see Event#getDate()
     * @see Event#getSignupDeadline()
     */
    public Date getStartTime() {
        return _startTime;
    }
    
    /**
     * Returns the start time for this Event.
     * @see CalendarEntry#getDate()
     */
    public Date getDate() {
    	return _startTime;
    }
    
    /**
     * Returns the ending time for this Event.
     * @return the date/time the Event ends
     * @see Event#setEndTime(Date)
     * @see Event#getStartTime()
     * @see Event#getSignupDeadline()
     */
    public Date getEndTime() {
        return _endTime;
    }
    
    /**
     * Returns the final date/time for pilots to sign up for this Event.
     * @return the date/time signups close
     * @see Event#setSignupDeadline(Date)
     * @see Event#getStartTime()
     * @see Event#getEndTime()
     */
    public Date getSignupDeadline() {
        return _signupDeadline;
    }
    
    /**
     * Returns wether Pilots may sign up for this Event.
     * @return TRUE if signups are permitted, otherwise FALSE
     * @see Event#setCanSignup(boolean)
     */
    public boolean getCanSignup() {
    	return _canSignup;
    }

    /**
     * Returns the Charts available for this Online Event.
     * @return a Collection of Chart beans
     * @see Event#addChart(Chart)
     * 	@see Event#addCharts(Collection)
     */
    public Collection<Chart> getCharts() {
        return _charts;
    }
    
    /**
     * Returns the Flight Plans available for this Online Event.
     * @return a Collection of FlightPlan beans
     * @see Event#addPlan(FlightPlan)
     */
    public Collection<FlightPlan> getPlans() {
        return _plans;
    }

    /**
     * Returns the available equipment types for this Online Event.
     * @return a Collection of equipment names
     * @see Event#addEquipmentType(String)
     */
    public Set<String> getEquipmentTypes() {
       return _eqTypes;
    }
    
    /**
     * Returns the signups for this Online Event.
     * @return a Collection of Signup beans
     * @see Event#addSignup(Signup)
     * @see Event#isSignedUp(int)
     */
    public Collection<Signup> getSignups() {
        return _signups;
    }
    
    /**
     * Returns the Routes available for this Online Event.
     * @return a Collection of Route beans
     * @see Event#addRoute(Route)
     * @see Event#getRoute(String)
     */
    public Collection<Route> getRoutes() {
    	return _routes;
    }
    
    /**
     * Returns the Airports available for this Online Event, from the available Routes. 
     * @return a Collection of Airport beans
     * @see Event#getRoutes()
     */
    public Collection<Airport> getAirports() {
    	Set<Airport> results = new HashSet<Airport>();
    	for (Iterator<Route> i = _routes.iterator(); i.hasNext(); ) {
    		Route r = i.next();
    		results.add(r.getAirportD());
    		results.add(r.getAirportA());
    	}
    	
    	return results;
    }
    
    /**
     * Returns a particular Route pair.
     * @param routePair a hyphen-separated pair of IATA codes
     * @return a Route bean, or null if route pair not found
     * @see Event#getRoutes()
     * @see Event#addRoute(Route)
     */
    public Route getRoute(String routePair) {
    	for (Iterator<Route> i = _routes.iterator(); i.hasNext(); ) {
    		Route r = i.next();
    		if (routePair.equals(r.getComboAlias()))
    			return r;
    	}
    	
    	return null;
    }
    
    /**
     * Returns a Signup for a particular Pilot.
     * @param pilotID the pilot's database ID
     * @return a Signup bean, or null if not found
     * @see Event#getSignups()
     * @see Event#addSignup(Signup)
     * @see Event#isSignedUp(int)
     */
    public Signup getSignup(int pilotID) {
    	for (Iterator<Signup> i = _signups.iterator(); i.hasNext(); ) {
    		Signup s = i.next();
    		if (s.getPilotID() == pilotID)
    			return s;
    	}
    	
    	return null;
    }
    
    /**
     * Returns all Flight Assignments for this Online Event.
     * @return a Collection of AssignmentInfo beans
     * @see Event#addAssignment(AssignmentInfo)
     */
    public Collection<AssignmentInfo> getAssignments() {
        return _assignments;
    }
    
    /**
     * Returns wether a Pilot is signed up for this Online Event.
     * @param pilotID the Pilot's database ID
     * @return TRUE if the Pilot has signed up, otherwise FALSE
     * @see Event#getSignups()
     * @see Event#getSignup(int)
     * @see Event#addSignup(Signup)
     */
    public boolean isSignedUp(int pilotID) {
       for (Iterator<Signup> i = _signups.iterator(); i.hasNext(); ) {
          Signup s = i.next();
          if (s.getPilotID() == pilotID)
             return true;
       }
       
       return false;
    }
    
    /**
     * Updates the briefing for this Online Event.
     * @param briefing the briefing text
     * @see Event#getBriefing()
     */
    public void setBriefing(String briefing) {
        _briefing = briefing;
    }
    
    /**
     * Updates the Online Event name.
     * @param name the name
     * @throws NullPointerException if name is null
     * @see Event#getName()
     */
    public void setName(String name) {
    	_name = name.trim();
    }
    
    /**
     * Adds an available equipment type for this event.
     * @param eqType the equipment code
     * @see Event#getEquipmentTypes()
     */
    public void addEquipmentType(String eqType) {
       _eqTypes.add(eqType);
    }
    
    /**
     * Updates this start time of this Online Event. If no signup deadline is currently set, it will default
     * to one hour before the start time.
     * @param dt the start date/time
     * @see Event#getStartTime()
     * @see Event#setEndTime(Date)
     * @see Event#setSignupDeadline(Date)
     */
    public void setStartTime(Date dt) {
        _startTime = dt;
        if (dt != null)
        	_signupDeadline = new Date(dt.getTime() - 60000);
    }
    
    /**
     * Updates the end time for this Online Event.
     * @param dt the end date/time
     * @throws IllegalArgumentException if dt is before the start time
     * @throws NullPointerException if the start time is null
     * @see Event#getEndTime()
     * @see Event#setStartTime(Date)
     * @see Event#setSignupDeadline(Date)
     */
    public void setEndTime(Date dt) {
        if ((dt != null) && (dt.before(_startTime)))
            throw new IllegalArgumentException("End Time cannot be before Start Time");
        
        _endTime = dt;
    }
    
    /**
     * Updates the signup deadline for this Online Event.
     * @param dt the signup deadline date/time
     * @throws IllegalArgumentException if dt is before the start time
     * @throws NullPointerException if the start time is null
     * @see Event#getSignupDeadline()
     * @see Event#setStartTime(Date)
     * @see Event#setEndTime(Date)
     */
    public void setSignupDeadline(Date dt) {
        if ((dt != null) && (dt.after(_startTime)))
            throw new IllegalArgumentException("Signup Deadline cannot be after Start Time");
        
        _signupDeadline = dt;
    }
    
    /**
     * Updates the Network used for this Online Event.
     * @param id the network id code
     * @throws IllegalArgumentException if id is negative or invalid
     * @see Event#setNetwork(String)
     * @see Event#getNetwork()
     * @see Event#getNetworkName()
     */
    public void setNetwork(int id) {
        if ((id < 0) || (id >= NETWORKS.length))
            throw new IllegalArgumentException("Invalid Network ID - " + id);
        
        _network = id;
    }
    
    /**
     * Updates the Network used for this Online Event.
     * @param networkName the network name
     * @throws IllegalArgumentException if networkName is invalid
     * @see Event#setNetwork(int)
     * @see Event#getNetworkName()
     * @see Event#getNetwork()
     */
    public void setNetwork(String networkName) {
    	setNetwork(StringUtils.arrayIndexOf(NETWORKS, networkName));
    }

    /**
     * Updates the status of this Online Event.
     * @param status the status code, if not CANCELED then OPEN
     * @see Event#getStatus()
     * @see Event#getStatusName()
     * @see Event#OPEN
     * @see Event#CANCELED
     */
    public void setStatus(int status) {
        _status = (status == Event.CANCELED) ? Event.CANCELED : Event.OPEN;
    }
    
    /**
     * Updates the signup available flag for this Online Event.
     * @param doSignup TRUE if signups are enabled, otherwise FALSE
     * @see Event#getCanSignup()
     */
    public void setCanSignup(boolean doSignup) {
    	_canSignup = doSignup;
    }
    
    /**
     * Adds a Flight Plan to this Online Event.
     * @param fp a FlightPlan bean
     * @see Event#getPlans()
     */
    public void addPlan(FlightPlan fp) {
        _plans.add(fp);
    }
    
    /**
     * Adds a Signup to this Online Event.
     * @param s a Signup bean
     * @see Event#getSignups()
     * @see Event#getSignup(int)
     * @see Event#isSignedUp(int)
     */
    public void addSignup(Signup s) {
        _signups.add(s);
    }
    
    /**
     * Adds a Flight Route to this Online Event.
     * @param r a Route bean
     * @see Event#getRoutes()
     * @see Event#getRoute(String)
     */
    public void addRoute(Route r) {
    	_routes.add(r);
    }
    
    /**
     * Adds a Flight Assignment to this Online Event.
     * @param ai an AssignmentInfo bean
     * @see Event#getAssignments()
     */
    public void addAssignment(AssignmentInfo ai) {
        _assignments.add(ai);
    }
    
    /**
     * Adds an available Chart to this Online Event.
     * @param c the Chart bean
     * @see Event#addCharts(Collection)
     * @see Event#getCharts()
     */
    public void addChart(Chart c) {
        _charts.add(c);
    }
    
    /**
     * Adds a number of Charts to this Online Event.
     * @param charts a Collection of Chart beans
     * @see Event#addChart(Chart)
     * @see Event#getCharts()
     */
    public void addCharts(Collection<Chart> charts) {
    	_charts.addAll(charts);
    }

    /**
     * Compares to events by comparing their start date/times.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @throws ClassCastException if o2 is not an Event
     */
    public int compareTo(Object o2) {
        Event e2 = (Event) o2;
        int tmpResult = _startTime.compareTo(e2.getStartTime());
        return (tmpResult == 0) ? super.compareTo(e2) : tmpResult;
    }
}