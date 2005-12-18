// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.cooler;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.util.cache.Cacheable;

/**
 * A class containing Water Cooler channel data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Channel implements java.io.Serializable, Cacheable, ViewEntry {

    public static final Channel ALL = new AllChannel();
    
    public static final int INFOTYPE_AIRLINE = 0;
    public static final int INFOTYPE_ROLE = 1;
    
    private String _name;
    private String _desc;
    private boolean _active;
    private Set<String> _roles;
    private Set<String> _airlines;
    
    private int _threadCount;
    private int _postCount;
    private int _viewCount;
    
    private String _lastSubject;
    private int _lastThreadID;
    
    static class AllChannel extends Channel implements ComboAlias {
    	
    	private static Collection<String> ROLES = Arrays.asList(new String[] {"*"});
    
    	AllChannel() {
    		super("ALL");
    	}
    	
    	public String getComboName() {
    		return "All Discussions";
    	}
    	
    	public String getComboAlias() {
    		return getName();
    	}
    	
    	public final Collection<String> getRoles() {
    		return ROLES;
    	}
    }
    
    /**
     * Creates a new Channel object with a given Channel name.
     * @param name the Channel name 
     * @throws NullPointerException if name is null
     * @see Channel#getName()
     */
    public Channel(String name) {
        super();
        _name = name.trim();
        _airlines = new TreeSet<String>();
        _roles = new TreeSet<String>();
    }
    
    /**
     * Returns the Channel name.
     * @return the channel name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the Channel description.
     * @return the channel description
     * @see Channel#setDescription(String)
     */
    public String getDescription() {
        return _desc;
    }
    
    /**
     * Returns the roles authorized to access this Channel.
     * @return a sorted Set of roles authorized to view this Channel
     * @see Channel#addRole(String)
     */
    public Collection<String> getRoles() {
        return _roles;
    }
    
    /**
     * Returns the total number of thread views for Threads in this Channel. 
     * @return the number of views
     * @see Channel#setViewCount(int)
     */
    public int getViewCount() {
        return _viewCount;
    }
    
    /**
     * Returns the total number of message threads in this Channel.
     * @return the number of threads
     * @see Channel#setThreadCount(int)
     */
    public int getThreadCount() {
        return _threadCount;
    }
    
    /**
     * Returns the number of posts in this Channel.
     * @return the number of posts
     * @see Channel#setPostCount(int)
     */
    public int getPostCount() {
        return _postCount;
    }

    /**
     * Returns the airlines associated with this Channel.
     * @return a sorted Set of Airline codes for this Channel
     * @see Channel#addAirline(String)
     */
    public Collection<String> getAirlines() {
        return _airlines;
    }
    
    /**
     * Returns the title of the latest post in this Channel.
     * @return the post title
     * @see Channel#getLastSubject()
     */
    public String getLastSubject() {
        return _lastSubject;
    }
    
    /**
     * Returns ID of the last Message Thread in this Channel.
     * @return the database ID of the latest post
     * @see Channel#setLastThreadID(int)
     */
    public int getLastThreadID() {
        return _lastThreadID;
    }
    
    /**
     * Queries if the Channel is active.
     * @return TRUE if the Channel is active, otherwise FALSE
     * @see Channel#setActive(boolean)
     */
    public boolean getActive() {
        return _active;
    }
    
    /**
     * Queries if the Channel supports a particular airline.
     * @param aCode the Airline Code
     * @return TRUE if the Channel is supported by an airline, FALSE otherwisse
     * @see org.deltava.beans.schedule.Airline
     * @see Channel#addAirline(String)
     */
    public boolean hasAirline(String aCode) {
        return (aCode == null) ? false : _airlines.contains(aCode);
    }

    /**
     * Queries if a Person with a particular role can access this Channel.
     * @param roleName
     * @return TRUE if Persons with this role can access this Channel, FALSE otherwise
     * @see Channel#addRole(String)
     */
    public boolean hasRole(String roleName) {
        return (roleName == null) ? false : _roles.contains(roleName);
    }
    
    /**
     * Add an Airline to the list of enabled airlines.
     * @param aCode the Airline code to add
     * @throws NullPointerException if the airline code is null
     * @see org.deltava.beans.schedule.Airline
     * @see Channel#getAirlines()
     * @see Channel#hasAirline(String)
     */
    public void addAirline(String aCode) {
        _airlines.add(aCode.toUpperCase().trim());
    }
    
    /**
     * Updates the enabled airlines.
     * @param aCodes the Airline codes to enable
     * @throws NullPointerException if aCodes is null
     * @see Channel#getAirlines()
     * @see Channel#hasAirline(String)
     */
    public void setAirlines(Collection aCodes) {
    	_airlines.clear();
    	for (Iterator i = aCodes.iterator(); i.hasNext(); )
    	   addAirline((String) i.next());
    }
    
    /**
     * Add a security role to this Channel.
     * @param roleName the name of the role to add to this Channel's access list
     * @see Channel#setRoles(Collection)
     * @see Channel#getRoles()
     */
    public void addRole(String roleName) {
       _roles.add(roleName);
    }
    
    /**
     * Updates the security roles for this Channel.
     * @param roles the roles to add to this Channel's access list
     * @see Channel#getRoles()
     * @see Channel#addRole(String)
     */
    public void setRoles(Collection roles) {
    	_roles.clear();
    	for (Iterator i = roles.iterator(); i.hasNext(); )
    	   addRole((String) i.next());
    }
    
    /**
     * Update the Channel description.
     * @param desc the Channel description
     * @see Channel#getDescription()
     */
    public void setDescription(String desc) {
        _desc = desc;
    }
    
    /**
     * Set the channel active/inactive.
     * @param active TRUE if the Channel is active, otherwise FALSE
     * @see Channel#getActive()
     */
    public void setActive(boolean active) {
        _active = active;
    }
    
    /**
     * Updates the last subject posted to in this Channel.
     * @param subj the thread subject
     * @see Channel#getLastSubject()
     */
    public void setLastSubject(String subj) {
        _lastSubject = subj;
    }
    
    /**
     * Updates the ID of the last Message Thread in this Channel.
     * @param id the database ID
     * @throws IllegalArgumentException if id is negative
     * @see Channel#getLastThreadID()
     * @see DatabaseBean#validateID(int, int)
     */
    public void setLastThreadID(int id) {
        if (id != 0)
            DatabaseBean.validateID(_lastThreadID, id);
        
        _lastThreadID = id;
    }
    
    /**
     * Updates the number of Message Threads in this Channel.
     * @param count the number of threads
     * @throws IllegalArgumentException if count is negative
     * @see Channel#getThreadCount()
     */
    public void setThreadCount(int count) {
        if (count < 0)
            throw new IllegalArgumentException("Invalid Thread Count - " + count);
            
        _threadCount = count;
    }
    
    /**
     * Updates the number of posts in this Channel.
     * @param count the number of posts
     * @throws IllegalArgumentException if count is negative
     * @see Channel#getPostCount()
     */
    public void setPostCount(int count) {
        if (count < 0)
            throw new IllegalArgumentException("Invalid Post Count - " + count);
        
        _postCount = count;
    }
    
    /**
     * Updates the number of times all Threads in this Channel have been viewed.
     * @param count the number of thread views
     * @throws IllegalArgumentException if count is negative
     * @see Channel#getThreadCount()
     */
    public void setViewCount(int count) {
        if (count < 0)
            throw new IllegalArgumentException("Invalid Thread View Count - " + count);
        
        _viewCount = count;
    }
    
    /**
     * Converts the channel profile to a String.
     * @return the channel name
     */
    public String toString() {
        return getName();
    }

    /**
     * Returns the cache key.
     * @see Cacheable
     */
    public Object cacheKey() {
        return getName();
    }
    
    /**
     * Compares the channel to another object.
     * @return TRUE if o2 is a channel with the same name, or a String with the same name
     */
    public boolean equals(Object o2) {
        if (o2 instanceof Channel) {
            return _name.equals(((Channel) o2).getName());
        } else if (o2 instanceof String) {
            return _name.equals(o2);
        } 
           
        return false;
    }
    
    /**
     * Returns the hashcode of the channel name.
     * @return the channel name's hashcode
     */
    public int hashCode() {
        return _name.hashCode();
    }
    
    /**
     * Returns the CSS row class name.
     * @return the CSS class name
     */
    public String getRowClassName() {
    	return _active ? null : "warn"; 
    }
}