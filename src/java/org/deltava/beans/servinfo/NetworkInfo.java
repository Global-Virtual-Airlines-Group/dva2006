// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store aggregated network information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NetworkInfo implements java.io.Serializable, Cacheable {

    private String _name;
    private int _version;
    private Date _validDate;
    private boolean _isCached;
    
    private Map _pilots;
    private Map _controllers;
    
    /**
     * Initializes this bean for a particular network name.
     * @param name the network name (IVAO/VATSIM)
     * @throws NullPointerException if name is null
     * @see NetworkInfo#getName()
     */
    public NetworkInfo(String name) {
        super();
        _name = name.toUpperCase();
        _pilots = new TreeMap();
        _controllers = new TreeMap();
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
     * Returns if this data was cached by the DAO
     * @return TRUE if the data is cached, otherwise FALSE
     */
    public boolean getCached() {
    	return _isCached;
    }
    
    /**
     * Returns the network name.
     * @return the network name (IVAO/VATSIM)
     */
    public String getName() {
        return _name;
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
     * @see NetworkInfo#getPilots()
     */
    public Collection getControllers() {
        return new ArrayList(_controllers.values());
    }
    
    /**
     * Returns the active Pilots on this network.
     * @return a Collection of Pilot beans
     * @see NetworkInfo#getControllers()
     */
    public Collection getPilots() {
        return new ArrayList(_pilots.values());
    }
    
    /**
     * Assigns Pilot IDs to online Pilots and Controllers that have user accounts.
     * @param idMap a Map of Integers, keyed by network ID with the database ID as the value.
     */
    public void setPilotIDs(Map idMap) {
    	
    	// Mash pilots + controllers together
    	Set users = new HashSet(_pilots.values());
    	users.addAll(_controllers.values());
    	
    	// Assign database IDs to active Pilots
    	for (Iterator i = users.iterator(); i.hasNext(); ) {
    		NetworkUser usr = (NetworkUser) i.next();
    		String netID = String.valueOf(usr.getID());
    		
    		if (idMap.containsKey(netID)) {
    			Integer id = (Integer) idMap.get(netID);
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
       return (Pilot) _pilots.get(callsign); 
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
       return (Controller) _controllers.get(callsign);
    }
    
    /**
	 * Marks this data as cached.
	 * @see NetworkInfo#getCached()
	 */
    public void setCached() {
    	_isCached = true;
    }
    
    /**
     * Returns the network's hash code.
     */
    public int hashCode() {
       return _name.hashCode();
    }
  
    /**
     * Returns this object's cache key.
     * @return the network name
     */
    public Object cacheKey() {
       return getName();
    }
}