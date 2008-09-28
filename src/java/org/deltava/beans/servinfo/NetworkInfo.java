// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.util.cache.Cacheable;

import org.deltava.util.GeoUtils;

/**
 * A bean to store aggregated network information.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class NetworkInfo implements java.io.Serializable, Cacheable {

    private OnlineNetwork _net;
    private int _version;
    private Date _validDate;
    
    private boolean _isCached;
    private boolean _isExpired;
    
    private final Map<String, Pilot> _pilots = new TreeMap<String, Pilot>();
    private final Map<String, Controller> _controllers = new TreeMap<String, Controller>();
    
    /**
     * Initializes this bean for a particular network.
     * @param net the network
     * @see NetworkInfo#getNetwork()
     */
    public NetworkInfo(OnlineNetwork net) {
        super();
        _net = net;
    }
    
    /**
     * Returns the ServInfo data format version.
     * @return the data format version, usually 7
     * @see NetworkInfo#setVersion(String)
     */
    public int getVersion() {
        return _version;
    }
    
    /**
     * Returns if this data was cached by the DAO.
     * @return TRUE if the data is cached, otherwise FALSE
     * @see NetworkInfo#setCached()
     */
    public boolean getCached() {
    	return _isCached;
    }
    
    /**
     * Returns if this date is probably out of date.
     * @return TRUE if the data is out of date, otherwise FALSE
     * @see NetworkInfo#setExpired()
     */
    public boolean getExpired() {
    	return _isExpired;
    }
    
    /**
     * Returns the network.
     * @return the network
     */
    public OnlineNetwork getNetwork() {
        return _net;
    }
    
    /**
     * Returns the effective date of this data.
     * @return the date/time the data was generated
     * @see NetworkInfo#setValidDate(Date)
     */
    public Date getValidDate() {
        return _validDate;
    }
    
    /**
     * Returns the active Controllers on this network.
     * @return a Collection of Controller beans
     * @see NetworkInfo#getControllers(GeoLocation)
     * @see NetworkInfo#getPilots()
     */
    public Collection<Controller> getControllers() {
        return new LinkedHashSet<Controller>(_controllers.values());
    }
    
    /**
     * Returns all Controllers within a specific distance of a particular point. 
     * @param gl the location
     * @return a Collection of Controller beans
     * @see NetworkInfo#getControllers()
     */
    public Collection<Controller> getControllers(GeoLocation gl) {
    	if (gl == null)
    		return getControllers();
    	
    	// Strip out based on distance and facility type
    	Collection<Controller> results = new LinkedHashSet<Controller>();
    	for (Iterator<Controller> i = _controllers.values().iterator(); i.hasNext(); ) {
    		Controller c = i.next();
    		int maxDistance = 200;
    		switch (c.getFacility()) {
    			case Controller.OBSERVER:
    			case Controller.DEL:
    				maxDistance = 30;
    				break;
    				
    			case Controller.GND:
    				maxDistance = 60;
    				break;
    				
    			case Controller.TWR:
    				maxDistance = 125;
    				break;
    				
    			case Controller.APP:
    				maxDistance = 350;
    				break;
    				
    			case Controller.CTR:
    				maxDistance = 1500;
    				break;
    				
    			case Controller.FSS:
    				maxDistance = 2500;
    				break;
    		}
    		
    		// Add if we're not too far away
    		if (GeoUtils.distance(gl, c.getPosition()) <= maxDistance)
    			results.add(c);
    	}
    	
    	return results;
    }
    
    /**
     * Returns the active Pilots on this network.
     * @return a Collection of Pilot beans
     * @see NetworkInfo#getControllers()
     */
    public Collection<Pilot> getPilots() {
        return new ArrayList<Pilot>(_pilots.values());
    }
    
    /**
     * Returns the entry for a particular network user, if online.
     * @param networkID the user's network ID
     * @return a NetworkUser bean, or null if not found
     */
    public NetworkUser get(int networkID) {
    	Collection<NetworkUser> allUsers = new LinkedHashSet<NetworkUser>(_controllers.values());
    	allUsers.addAll(_pilots.values());
    	for (Iterator<NetworkUser> i = allUsers.iterator(); i.hasNext(); ) {
    		NetworkUser usr = i.next();
    		if (usr.getID() == networkID)
    			return usr;
    	}
    	
    	return null;
    }
    
    /**
     * Assigns Pilot IDs to online Pilots and Controllers that have user accounts.
     * @param idMap a Map of Integers, keyed by network ID with the database ID as the value.
     */
    public void setPilotIDs(Map<String, Integer> idMap) {
    	
    	// Mash pilots + controllers together
    	Set<NetworkUser> users = new HashSet<NetworkUser>(_pilots.values());
    	users.addAll(_controllers.values());
    	
    	// Assign database IDs to active Pilots
    	for (Iterator<NetworkUser> i = users.iterator(); i.hasNext(); ) {
    		NetworkUser usr = i.next();
    		String netID = String.valueOf(usr.getID());
    		
    		if (idMap.containsKey(netID)) {
    			Integer id = idMap.get(netID);
    			usr.setPilotID(id.intValue());
    		}
    	}
    }
    
    /**
     * Updates the ServInfo data format revision.
     * @param version the data format, typically 7
     * @see NetworkInfo#getVersion()
     */
    public void setVersion(String version) {
        try {
            _version = Integer.parseInt(version);
        } catch (NumberFormatException nfe) {
            _version = 7;
        }
    }
    
    /**
     * Updates the validity date of this data.
     * @param d the date/time this data was generated
     * @see NetworkInfo#getValidDate()
     */
    public void setValidDate(Date d) {
        _validDate = d;
    }
    
    /**
     * Adds a Controller entry to the data.
     * @param c the Controller bean
     * @see NetworkInfo#getControllers()
     * @see NetworkInfo#getController(String)
     * @see NetworkInfo#add(Pilot)
     */
    public void add(Controller c) {
        _controllers.put(c.getCallsign(), c);
    }
    
    /**
     * Adds a Pilot entry to the data.
     * @param p the Pilot bean
     * @see NetworkInfo#getPilots()
     * @see NetworkInfo#getPilot(String)
     * @see NetworkInfo#add(Controller)
     */
    public void add(Pilot p) {
        _pilots.put(p.getCallsign(), p);
    }
    
    /**
     * Returns a specific online Pilot entry.
     * @param callsign the callsign
     * @return a Pilot bean, or null if the callsign was not found
     * @see NetworkInfo#add(Pilot)
     * @see NetworkInfo#getPilots()
     * @see NetworkInfo#getController(String)
     */
    public Pilot getPilot(String callsign) {
       return _pilots.get(callsign); 
    }
    
    /**
     * Returns a specific online Controller entry.
     * @param callsign the callsign
     * @return a Controller bean, or null if the callsign was not found
     * @see NetworkInfo#add(Controller)
     * @see NetworkInfo#getControllers()
     * @see NetworkInfo#getPilot(String)
     */
    public Controller getController(String callsign) {
       return _controllers.get(callsign);
    }
    
    /**
	 * Marks this data as cached. <i>This flag cannot be cleared</i>.
	 * @see NetworkInfo#getCached()
	 */
    public void setCached() {
    	_isCached = true;
    }
    
    /**
	 * Marks this data as expired. <i>This flag cannot be cleared</i>.
	 * @see NetworkInfo#getExpired()
	 */
    public void setExpired() {
    	_isExpired = true;
    }
    
    /**
     * Returns the network's hash code.
     */
    public int hashCode() {
       return _net.hashCode();
    }
  
    /**
     * Returns this object's cache key.
     * @return the network name
     */
    public Object cacheKey() {
       return _net;
    }
}