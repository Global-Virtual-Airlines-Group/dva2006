// Copyright 2005, 2006, 2007, 2008, 2011, 2012, 2016, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.util.cache.Cacheable;

/**
 * A class containing Water Cooler channel data.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class Channel implements Comparable<Channel>, Auditable, Cacheable, ComboAlias, ViewEntry {

    public static final Channel ALL = new AllChannel("All Discussions", "ALL", true);
    public static final Channel SHOTS = new AllChannel("Screen Shots", "SSHOTS", false);
    
    /**
     * Subdata enumeration.
     */
    public enum InfoType {
    	AIRLINE, READ, WRITE, NOTIFY
    }
    
    private final String _name;
    private String _desc;
    private boolean _active = true;
    private boolean _allowNewPosts = true;
    private final Collection<String> _airlines = new TreeSet<String>();
    private final Collection<String> _rRoles = new TreeSet<String>();
    private final Collection<String> _wRoles = new TreeSet<String>();
    private final Collection<String> _nRoles = new TreeSet<String>();
    
    private int _threadCount;
    private int _postCount;
    private int _viewCount;
    
    private String _lastSubject;
    private int _lastThreadID;
    
    static class AllChannel extends Channel {
    	
    	private static final Collection<String> RROLES = Arrays.asList("*");
    	
    	private final String _myName;
    	private final boolean _topOfList;
    
    	AllChannel(String name, String alias, boolean isTopOfList) {
    		super(alias);
    		_myName = name;
    		_topOfList = isTopOfList;
    	}
    	
    	@Override
    	public String getComboName() {
    		return _myName;
    	}
    	
    	@Override
    	public String getComboAlias() {
    		return getName();
    	}
    	
    	@Override
    	public final Collection<String> getReadRoles() {
    		return RROLES;
    	}
    	
    	@Override
    	public final Collection<String> getWriteRoles() {
    		return Collections.emptySet();
    	}
    	
    	@Override
    	public final Collection<String> getNotifyRoles() {
    		return Collections.emptySet();
    	}
    	
    	@Override
    	public final int compareTo(Channel c2) {
    		return _topOfList ? -1 : getName().compareTo(c2.getName());
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
    }
    
    /**
     * Returns the Channel name.
     * @return the channel name
     */
    public String getName() {
        return _name;
    }
    
    @Override
    public String getComboName() {
    	return _name;
    }
    
    @Override
    public String getComboAlias() {
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
     * @return a Collection of roles authorized to view this Channel
     * @see Channel#addRole(InfoType, String)
     */
    public Collection<String> getReadRoles() {
        return _rRoles;
    }
    
    /**
     * Returns the roles authorized to post in this Channel.
     * @return a Collection of roles authorized to view this Channel
     * @see Channel#addRole(InfoType, String)
     */
    public Collection<String> getWriteRoles() {
        return _wRoles;
    }

    /**
     * Returns the roles notified when a new post is created in this Channel.
     * @return a Collection of roles
     * @see Channel#addRole(InfoType, String)
     */
    public Collection<String> getNotifyRoles() {
    	return _nRoles;
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
     * Queries if new Threads or Replies are allowed.
     * @return TRUE if new threads/replies are allowed, otherwise FALSE
     * @see Channel#setAllowNewPosts(boolean)
     */
    public boolean getAllowNewPosts() {
    	return _allowNewPosts;
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
    public void setAirlines(Collection<String> aCodes) {
    	_airlines.clear();
    	for (Iterator<String> i = aCodes.iterator(); i.hasNext(); )
    	   addAirline(i.next());
    }
    
    /**
     * Add a security role to this Channel.
     * @param type the role type
     * @param roleName the name of the role to add to this Channel's access list
     * @see Channel#setRoles(InfoType, Collection)
     * @see Channel#getReadRoles()
     * @see Channel#getWriteRoles()
     * @see Channel#getNotifyRoles()
     */
    public void addRole(InfoType type, String roleName) {
    	switch (type) {
    		case READ:
    			_rRoles.add(roleName);
    			break;
    			
    		case WRITE:
    			_wRoles.add(roleName);
    			break;
    			
    		case NOTIFY:
    			_nRoles.add(roleName);
    			break;
    			
    		default:
    			break;
    	}
    }
    
    /**
     * Updates the security roles for this Channel.
     * @param type the role type
     * @param roles the roles to add to this Channel's access list
     * @see Channel#getReadRoles()
     * @see Channel#getWriteRoles()
     * @see Channel#addRole(InfoType, String)
     */
    public void setRoles(InfoType type, Collection<String> roles) {
    	if ((type != InfoType.NOTIFY) && ((roles == null) || roles.isEmpty())) {
    		setRoles(type, Arrays.asList("*"));
    		return;
    	}
    	
    	switch (type) {
			case READ:
				_rRoles.clear();
				_rRoles.addAll(roles);
				break;
			
			case WRITE:
				_wRoles.clear();
				_wRoles.addAll(roles);
				break;
			
			case NOTIFY:
				_nRoles.clear();
				if (roles != null)
					_nRoles.addAll(roles);
				break;
				
			default:
				break;
    	}
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
     * Updates whether new Threads or Replies are allowed.
     * @param allow TRUE if new posts allowed, otherwise FALSE
     * @see Channel#getAllowNewPosts()
     */
    public void setAllowNewPosts(boolean allow) {
    	_allowNewPosts = allow;
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
     */
    public void setLastThreadID(int id) {
    	DatabaseBean.validateID(_lastThreadID, id, true);
        _lastThreadID = id;
    }
    
    /**
     * Updates the number of Message Threads in this Channel.
     * @param count the number of threads
     * @see Channel#getThreadCount()
     */
    public void setThreadCount(int count) {
        _threadCount = Math.max(0, count);
    }
    
    /**
     * Updates the number of posts in this Channel.
     * @param count the number of posts
     * @see Channel#getPostCount()
     */
    public void setPostCount(int count) {
        _postCount = Math.max(0, count);
    }
    
    /**
     * Updates the number of times all Threads in this Channel have been viewed.
     * @param count the number of thread views
     * @see Channel#getThreadCount()
     */
    public void setViewCount(int count) {
        _viewCount = Math.max(0, count);
    }
    
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Object cacheKey() {
        return getName();
    }
    
    /**
     * Compares the channel to another object.
     * @return TRUE if o2 is a channel with the same name, or a String with the same name
     */
    @Override
    public boolean equals(Object o2) {
        return (o2 instanceof Channel ch2) && _name.equals(ch2._name);
    }
    
    @Override
    public int hashCode() {
        return _name.hashCode();
    }
    
    @Override
    public int compareTo(Channel c2) {
    	return _name.compareTo(c2._name);
    }
    
    @Override
    public String getRowClassName() {
    	if (!_allowNewPosts)
    		return "opt2";
    
    	return _active ? null : "warn"; 
    }

	@Override
	public String getAuditID() {
		return _name;
	}
}