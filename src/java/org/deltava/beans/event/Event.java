// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;

/**
 * A class to store Online Event information.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class Event extends ImageBean implements ComboAlias, TimeSpan {
	
    private String _name;
    private String _briefing;
    private Instant _startTime;
    private Instant _endTime;
    private Instant _signupDeadline;
    private String _bannerExt;
    
    private Status _status;
    private OnlineNetwork _network = OnlineNetwork.VATSIM;
    
    private boolean _canSignup;
    private String _signupURL;
    
    private AirlineInformation _owner;
    private final Collection<AirlineInformation> _airlines = new TreeSet<AirlineInformation>();
    private final Collection<Chart> _charts = new TreeSet<Chart>();
    private final Collection<Signup> _signups = new LinkedHashSet<Signup>();
    private final Collection<Route> _routes = new TreeSet<Route>();
    private final List<AssignmentInfo> _assignments = new ArrayList<AssignmentInfo>();
    private final Collection<DispatchRoute> _dspRoutes = new LinkedHashSet<DispatchRoute>();
    private final Collection<String> _eqTypes = new TreeSet<String>();
    private final Collection<String> _contactAddrs = new LinkedHashSet<String>();
    
    /**
     * Creates a new Online Event.
     * @param name the event name
     * @throws NullPointerException if name is null
     * @see Event#getName()
     */
    public Event(String name) {
        super();
        _name = name;
    }
    
    /**
     * Returns the Online Event name.
     * @return the event name
     */
    public String getName() {
        return _name;
    }
    
    @Override
    public String getComboName() {
        return getName();
    }
    
    @Override
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
    
    /*
     * Null-safe helper method to compare dates.
     */
    private static boolean before(Instant d2) {
    	return (d2 == null) ? true : (System.currentTimeMillis() < d2.toEpochMilli());
    }
    
    /**
     * Returns the status of this Online Event.
     * @return the Status
     * @see Event#setStatus(Status)
     */
    public Status getStatus() {
        // If the event was canceled, then return that, otherwise calculate the status
        if (_status == Status.CANCELED)
            return Status.CANCELED;
        else if (before(_signupDeadline))
            return Status.OPEN;
        else if (before(_startTime))
            return Status.CLOSED;
        else if (before(_endTime))
            return Status.ACTIVE;

        return Status.COMPLETE;
    }
    
    /**
     * Returns the Airline that owns this Online Event.
     * @return an AirlineInformation bean
     * @see Event#setOwner(AirlineInformation)
     * @see Event#getAirlines()
     */
    public AirlineInformation getOwner() {
    	return _owner;
    }

    /**
     * Returns the Airlines participating in this Online Event.
     * @return a Collection of AirlineInformation beans
     * @see Event#addAirline(AirlineInformation)
     */
    public Collection<AirlineInformation> getAirlines() {
    	return _airlines;
    }
    
    /**
     * Returns the Online Network for this Event.
     * @return the network code
     * @see Event#setNetwork(OnlineNetwork)
     */
    public OnlineNetwork getNetwork() {
        return _network;
    }
    
    @Override
    public Instant getStartTime() {
        return _startTime;
    }
    
    @Override
    public Instant getDate() {
    	return _startTime;
    }
    
    @Override
    public Instant getEndTime() {
        return _endTime;
    }
    
    /**
     * Returns the final date/time for pilots to sign up for this Event.
     * @return the date/time signups close
     * @see Event#setSignupDeadline(Instant)
     * @see Event#getStartTime()
     * @see Event#getEndTime()
     */
    public Instant getSignupDeadline() {
        return _signupDeadline;
    }
    
    /**
     * Returns whether Pilots may sign up for this Event.
     * @return TRUE if signups are permitted, otherwise FALSE
     * @see Event#setCanSignup(boolean)
     */
    public boolean getCanSignup() {
    	return _canSignup;
    }
    
    /**
     * Returns the external URL used to sign up at.
     * @return the signup URL
     * @see Event#setSignupURL(String)
     */
    public String getSignupURL() {
    	return _signupURL;
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
     * Returns the e-mail contact addresses for this Online Event.
     * @return a Collection of email addresses
     * @see Event#addContactAddr(String)
     */
    public Collection<String> getContactAddrs() {
    	return _contactAddrs;
    }
    
    /**
     * Returns the available equipment types for this Online Event.
     * @return a Collection of equipment names
     * @see Event#addEquipmentType(String)
     */
    public Collection<String> getEquipmentTypes() {
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
     * Returns the Dispatch Routes for this Online Event.
     * @return a Collection of DispatchRoute beans
     * @see Event#addRoute(DispatchRoute)
     */
    public Collection<DispatchRoute> getDispatchRoutes() {
    	return _dspRoutes;
    }
    
    /**
     * Returns the Routes for this Online Event.
     * @return a Collection of Route beans
     * @see Event#addRoute(Route)
     * @see Event#getActiveRoutes()
     */
    public Collection<Route> getRoutes() {
    	return _routes;
    }
    
    /**
     * Retrieves a specifc route for this Online Event.
     * @param routeID the Route ID
     * @return a Route bean, or null if not found
     */
    public Route getRoute(int routeID) {
    	return _routes.stream().filter(r -> r.getRouteID() == routeID).findAny().orElse(null);
    }

    /**
     * Returns the active Routes for this Online Event.
     * @return a Collection of Route beans
     * @see Event#addRoute(Route)
     * @see Event#getRoutes()
     */
    public Collection<Route> getActiveRoutes() {
    	return _routes.stream().filter(r -> r.getActive() && r.isAvailable()).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    /**
     * Returns the Airports available for this Online Event, from the available Routes. 
     * @return a Collection of Airport beans
     * @see Event#getRoutes()
     */
    public Collection<Airport> getAirports() {
    	Set<Airport> results = new HashSet<Airport>();
    	_routes.forEach(r -> { results.add(r.getAirportD()); results.add(r.getAirportA()); });
    	return results;
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
    	return _signups.stream().filter(s -> (s.getPilotID() == pilotID)).findAny().orElse(null);
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
	 * Queries if the Online Event has a banner image.
	 * @return TRUE if the event has an image, otherwise FALSE
	 * @see Event#setBannerExtension(String)
	 */
	public boolean getHasBanner() {
		return (_bannerExt != null);
	}
    
    /**
     * Returns whether a Pilot is signed up for this Online Event.
     * @param pilotID the Pilot's database ID
     * @return TRUE if the Pilot has signed up, otherwise FALSE
     * @see Event#getSignups()
     * @see Event#getSignup(int)
     * @see Event#addSignup(Signup)
     */
    public boolean isSignedUp(int pilotID) {
    	return (getSignup(pilotID) != null);
    }
    
    /**
     * Returns if an Airport is a destination of any of the Routes.
     * @param a the Airport bean
     * @return TRUE if a destination for any Route, otherwise FALSE
     * @see Event#isOrigin(Airport)
     */
    public boolean isDestination(Airport a) {
    	return _routes.stream().anyMatch(r -> r.getAirportA().equals(a));
    }
    
    /**
     * Returns if an Airport is an origin of any of the Routes.
     * @param a the Airport bean
     * @return TRUE if an origin for any Route, otherwise FALSE
     * @throws NullPointerException if a is null
     * @see Event#isDestination(Airport)
     */
    public boolean isOrigin(Airport a) {
    	return _routes.stream().anyMatch(r -> r.getAirportD().equals(a));
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
     * @see Event#setEndTime(Instant)
     * @see Event#setSignupDeadline(Instant)
     */
    public void setStartTime(Instant dt) {
        _startTime = dt;
        if (dt != null)
        	_signupDeadline = dt.minusSeconds(60000);
    }
    
    /**
     * Updates the end time for this Online Event.
     * @param dt the end date/time
     * @throws IllegalArgumentException if dt is before the start time
     * @throws NullPointerException if the start time is null
     * @see Event#getEndTime()
     * @see Event#setStartTime(Instant)
     * @see Event#setSignupDeadline(Instant)
     */
    public void setEndTime(Instant dt) {
        if ((dt != null) && dt.isBefore(_startTime))
            throw new IllegalArgumentException("End Time cannot be before Start Time");
        
        _endTime = dt;
    }
    
    /**
     * Updates the signup deadline for this Online Event.
     * @param dt the signup deadline date/time
     * @throws IllegalArgumentException if dt is before the start time
     * @throws NullPointerException if the start time is null
     * @see Event#getSignupDeadline()
     * @see Event#setStartTime(Instant)
     * @see Event#setEndTime(Instant)
     */
    public void setSignupDeadline(Instant dt) {
        if ((dt != null) && dt.isAfter(_startTime))
            throw new IllegalArgumentException("Signup Deadline cannot be after Start Time");
        
        _signupDeadline = dt;
    }
    
    /**
     * Updates the Network used for this Online Event.
     * @param net the network
     * @throws IllegalArgumentException if id is negative or invalid
     * @see Event#getNetwork()
     */
    public void setNetwork(OnlineNetwork net) {
        _network = net;
    }
    
    /**
     * Updates the status of this Online Event.
     * @param status the Status, if not CANCELED then OPEN
     * @see Event#getStatus()
     */
    public void setStatus(Status status) {
        _status = (status == Status.CANCELED) ? Status.CANCELED : Status.OPEN;
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
     * Updates the external URL used to sign up for this Online Event.
     * @param url the signup URL
     * @see Event#getSignupURL()
     */
    public void setSignupURL(String url) {
    	_signupURL = url;
    }
    
	/**
	 * Sets if this Eventhas a banner image available.
	 * @param ext the banner extension, or null
	 * @see Event#getHasBanner()
	 */
	public void setBannerExtension(String ext) {
		_bannerExt = StringUtils.isEmpty(ext) ? null : ext.toLowerCase();
	}

	/**
	 * Sets the Airline that owns this Online Event.
	 * @param ai the owning Airline's AirlineInformation bean
	 * @see Event#getOwner()
	 */
	public void setOwner(AirlineInformation ai) {
		_owner = ai;
		_airlines.add(ai);
	}
	
	/**
	 * Adds a participatig Airline to this Online Event.
	 * @param ai the participating airline's AirlineInformation bean
	 * @see Event#getAirlines()
	 */
	public void addAirline(AirlineInformation ai) {
		if (ai != null)
			_airlines.add(ai);
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
     */
    public void addRoute(Route r) {
    	_routes.add(r);
    }
    
    /**
     * Adds a Dispatch Route to this Online Event.
     * @param dr a DispatchRoute bean
     * @see Event#getDispatchRoutes()
     */
    public void addRoute(DispatchRoute dr) {
    	_dspRoutes.add(dr);
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
     * Adds a contact address to this Online Event.
     * @param addr the e-mail address
     * @throws NullPointerException if addr is null
     * @see Event#getContactAddrs()
     */
    public void addContactAddr(String addr) {
    	_contactAddrs.add(addr.trim());
    }

    /**
     * Compares to events by comparing their start date/times.
     */
    @Override
    public int compareTo(Object o2) {
        Event e2 = (Event) o2;
        int tmpResult = _startTime.compareTo(e2.getStartTime());
        return (tmpResult == 0) ? super.compareTo(e2) : tmpResult;
    }
}