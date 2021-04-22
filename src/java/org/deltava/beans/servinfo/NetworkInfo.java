// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2016, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.comparators.GeoComparator;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store aggregated network information.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class NetworkInfo implements Cacheable {

    private final OnlineNetwork _net;
    private int _version = 7;
    private Instant _validDate;
    
    private boolean _hasPilotIDs;
    
    private final Map<String, Pilot> _pilots = new TreeMap<String, Pilot>();
    private final Map<String, Controller> _controllers = new TreeMap<String, Controller>();
    private final Map<String, Server> _servers = new TreeMap<String, Server>();
    
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
     * @return the data format version, usually 7 for legacy ServInfo
     * @see NetworkInfo#setVersion(int)
     */
    public int getVersion() {
        return _version;
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
     * @see NetworkInfo#setValidDate(Instant)
     */
    public Instant getValidDate() {
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
     * Returns the FSD servers on this network.
     * @return a Collection of Server beans
     */
    public Collection<Server> getServers() {
    	return new ArrayList<Server>(_servers.values());
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
    	for (Map.Entry<String, Controller> me : _controllers.entrySet()) {
    		Controller c = me.getValue(); 
    		int maxDistance = switch (c.getFacility()) {
    			case OBS, DEL -> Math.max(c.getRange(),  30);
    			case GND -> Math.max(c.getRange(),  60);
    			case APP -> Math.max(c.getRange(),  350);
    			case CTR -> Math.max(c.getRange(),  1500);
    			case FSS -> Math.max(c.getRange(),  2500);
    			default -> Math.max(c.getRange(),  125);
    		};
    		
    		// Add if we're not too far away
    		if ((c.getPosition() != null) && (gl.distanceTo(c.getPosition()) <= maxDistance))
    			results.add(c);
    	}
    	
    	return results;
    }
    
    /**
     * Returns whether the internal database IDs for Pilots and Controllers have been loaded.
     * @return TRUE if loaded, otherwise FALSE
     * @see NetworkInfo#setPilotIDs(Map)
     */
    public boolean hasPilotIDs() {
    	return _hasPilotIDs;
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
    	return allUsers.stream().filter(usr -> (usr.getID() == networkID)).findAny().orElse(null);
    }
    
    /**
     * Assigns Pilot IDs to online Pilots and Controllers that have user accounts.
     * @param idMap a Map of Integers, keyed by network ID with the database ID as the value.
     */
    public void setPilotIDs(Map<String, Integer> idMap) {
    	
    	// Mash pilots + controllers together
    	Collection<NetworkUser> users = new HashSet<NetworkUser>(_pilots.values());
    	users.addAll(_controllers.values());
    	
    	// Assign database IDs to active Pilots
    	for (NetworkUser usr : users) {
   			Integer id = idMap.get(String.valueOf(usr.getID()));
   			if (id != null)
   				usr.setPilotID(id.intValue());
    	}
    	
    	_hasPilotIDs = true;
    }
    
    /**
     * Updates the ServInfo data format revision.
     * @param version the data format, typically 7
     * @see NetworkInfo#getVersion()
     */
    public void setVersion(int version) {
    	_version = version;
    }
    
    /**
     * Updates the validity date of this data.
     * @param d the date/time this data was generated
     * @see NetworkInfo#getValidDate()
     */
    public void setValidDate(Instant d) {
        _validDate = d;
    }
    
    /**
     * Adds a Controller entry to the data.
     * @param c the Controller bean
     * @see NetworkInfo#getControllers()
     * @see NetworkInfo#getController(String)
     * @see NetworkInfo#add(Pilot)
     * @see NetworkInfo#add(Server)
     */
    public void add(Controller c) {
    	if (c != null)
    		_controllers.put(c.getCallsign(), c);
    }
    
    /**
     * Adds a Pilot entry to the data.
     * @param p the Pilot bean
     * @see NetworkInfo#getPilots()
     * @see NetworkInfo#getPilot(String)
     * @see NetworkInfo#add(Controller)
     * @see NetworkInfo#add(Server)
     */
    public void add(Pilot p) {
    	if (p != null)
    		_pilots.put(p.getCallsign(), p);
    }
    
    /**
     * Adds a server entry to the data.
     * @param srv the Server bean
     * @see NetworkInfo#getServers()
     * @see NetworkInfo#add(Controller)
     * @see NetworkInfo#add(Pilot)
     */
    public void add(Server srv) {
    	_servers.put(srv.getName(), srv);
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
     * Returns a controller by Frequency.
     * @param freq the frequency
     * @param loc the location to sort, if multiple results found
     * @return a Controller bean, or null if the frequency was not found
     */
    public Controller getControllerByFrequency(String freq, GeoLocation loc) {
    	if ("122.8".equals(freq)) return null;
    	List<Controller> results = _controllers.values().stream().filter(c -> freq.equals(c.getFrequency())).collect(Collectors.toList());

    	// Sort by distance if a location specified
    	if ((loc != null) && (results.size() > 1)) {
    		results.removeIf(c -> (c.getPosition() == null));
    		Collections.sort(results, new GeoComparator(loc));
    	}
    	
    	return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Merges a Collection of radio positions to determine controller locations.
     * @param positions a Collection of RadioPosition beans
     */
    public void merge(Collection<RadioPosition> positions) {
    	for (RadioPosition rp : positions) {
    		Controller c = _controllers.get(rp.getCallsign());
    		if (c != null)
    			c.addPosition(rp);
    	}
    }
    
    @Override
    public NetworkInfo clone() {
    	NetworkInfo ni2 = new NetworkInfo(_net);
    	ni2._version = _version;
    	ni2._validDate = Instant.ofEpochMilli(_validDate.toEpochMilli());
    	ni2._pilots.putAll(_pilots);
    	ni2._controllers.putAll(_controllers);
    	ni2._servers.putAll(_servers);
    	ni2._hasPilotIDs = _hasPilotIDs;
    	return ni2;
    }
    
    @Override
    public int hashCode() {
       return _net.hashCode();
    }
  
    @Override
    public Object cacheKey() {
       return _net;
    }
}