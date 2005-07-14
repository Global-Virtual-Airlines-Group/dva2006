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
    
    private Set _pilots;
    private Set _controllers;
    
    /**
     * Initializes this bean for a particular network name.
     * @param name the network name (IVAO/VATSIM)
     * @throws NullPointerException if name is null
     * @see NetworkInfo#getName()
     */
    public NetworkInfo(String name) {
        super();
        _name = name.toUpperCase();
        _pilots = new TreeSet();
        _controllers = new TreeSet();
    }
    
    // TODO JavaDoc
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
    
    public Date getValidDate() {
        return _validDate;
    }
    
    /**
     * Returns the active Controllers on this network.
     * @return a Collection of Controller beans
     * @see NetworkInfo#getPilots()
     */
    public Collection getControllers() {
        return new ArrayList(_controllers);
    }
    
    /**
     * Returns the active Pilots on this network.
     * @return a Collection of Pilot beans
     * @see NetworkInfo#getControllers()
     */
    public Collection getPilots() {
        return new ArrayList(_pilots);
    }
    
    public void setPilotIDs(Map idMap) {
    	
    	// Mash pilots + controllers together
    	Set users = new HashSet(_pilots);
    	users.addAll(_controllers);
    	
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
    
    public void setVersion(String version) {
        try {
            _version = Integer.parseInt(version);
        } catch (NumberFormatException nfe) {
            _version = 7;
        }
    }
    
    public void setValidDate(Date d) {
        _validDate = d;
    }
    
    public void add(Controller c) {
        _controllers.add(c);
    }
    
    public void add(Pilot p) {
        _pilots.add(p);
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