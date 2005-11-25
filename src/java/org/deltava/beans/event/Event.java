// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.beans.event;

import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.DatabaseBean;

import org.deltava.beans.schedule.*;
import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.util.StringUtils;

/**
 * A class to store Online Event information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Event extends DatabaseBean implements Comparable, ComboAlias {

    public static final int NET_VATSIM = 0;
    public static final int NET_IVAO = 1;
    
    /**
     * Online Network names.
     */
    public static final String[] NETWORKS = {"VATSIM", "IVAO"};
    
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
    
    private Set<Chart> _charts;
    private List<FlightPlan> _plans;
    
    private Set<Signup> _signups;
    private Set<Route> _routes;
    private List<AssignmentInfo> _assignments;
    private Set<String> _eqTypes;
    
    // TODO JavaDoc
    public Event(String name) {
        super();
        setName(name);
        _charts = new TreeSet<Chart>();
        _plans = new ArrayList<FlightPlan>();
        _signups = new HashSet<Signup>();
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
     * @see Event#getSignupDeadline()
     */
    public Date getStartTime() {
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
    
    public Date getSignupDeadline() {
        return _signupDeadline;
    }

    public Collection<Chart> getCharts() {
        return _charts;
    }
    
    public Collection<FlightPlan> getPlans() {
        return _plans;
    }
    
    public Set<String> getEquipmentTypes() {
       return _eqTypes;
    }
    
    public Collection<Signup> getSignups() {
        return _signups;
    }
    
    public Collection<Route> getRoutes() {
    	return _routes;
    }
    
    public Collection<Airport> getAirports() {
    	Set<Airport> results = new HashSet<Airport>();
    	for (Iterator<Route> i = _routes.iterator(); i.hasNext(); ) {
    		Route r = i.next();
    		results.add(r.getAirportD());
    		results.add(r.getAirportA());
    	}
    	
    	return results;
    }
    
    public Route getRoute(String routePair) {
    	for (Iterator i = _routes.iterator(); i.hasNext(); ) {
    		Route r = (Route) i.next();
    		if (routePair.equals(r.getComboAlias()))
    			return r;
    	}
    	
    	return null;
    }
    
    public Signup getSignup(int pilotID) {
    	for (Iterator i = _signups.iterator(); i.hasNext(); ) {
    		Signup s = (Signup) i.next();
    		if (s.getPilotID() == pilotID)
    			return s;
    	}
    	
    	return null;
    }
    
    public List<AssignmentInfo> getAssignments() {
        return _assignments;
    }
    
    public boolean isAssigned(int pilotID) {
       for (Iterator i = _assignments.iterator(); i.hasNext(); ) {
          AssignmentInfo ai = (AssignmentInfo) i.next();
          if (ai.getPilotID() == pilotID)
             return true;
       }
       
       return false;
    }
    
    public boolean isSignedUp(int pilotID) {
       for (Iterator i = _signups.iterator(); i.hasNext(); ) {
          Signup s = (Signup) i.next();
          if (s.getPilotID() == pilotID)
             return true;
       }
       
       return false;
    }
    
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
    
    public void addEquipmentType(String eqType) {
       _eqTypes.add(eqType);
    }
    
    public void setStartTime(Date dt) {
        _startTime = dt;
        if (dt != null)
        	_signupDeadline = new Date(dt.getTime() - 60000);
    }
    
    public void setEndTime(Date dt) {
        if ((dt != null) && (dt.before(_startTime)))
            throw new IllegalArgumentException("End Time cannot be before Start Time");
        
        _endTime = dt;
    }
    
    public void setSignupDeadline(Date dt) {
        if ((dt != null) && (dt.after(_startTime)))
            throw new IllegalArgumentException("Signup Deadline cannot be after Start Time");
        
        _signupDeadline = dt;
    }
    
    public void setNetwork(int id) {
        if ((id < 0) || (id >= NETWORKS.length))
            throw new IllegalArgumentException("Invalid Network ID - " + id);
        
        _network = id;
    }
    
    public void setNetwork(String networkName) {
    	setNetwork(StringUtils.arrayIndexOf(NETWORKS, networkName));
    }

    public void setStatus(int status) {
        _status = (status == Event.CANCELED) ? Event.CANCELED : Event.OPEN;
    }
    
    public void addPlan(FlightPlan fp) {
        _plans.add(fp);
    }
    
    public void addSignup(Signup s) {
        _signups.add(s);
    }
    
    public void addRoute(Route r) {
    	_routes.add(r);
    }
    
    public void addAssignment(AssignmentInfo ai) {
        _assignments.add(ai);
    }
    
    public void addChart(Chart c) {
        _charts.add(c);
    }
    
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
        return _startTime.compareTo(e2.getStartTime());
    }
}