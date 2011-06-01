// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.beans.system.AirlineInformation;

/**
 * A bean to store data about an MVS voice channel. 
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public class Channel extends DatabaseBean implements ViewEntry {
	
	/**
	 * Access rights enumeration.
	 */
	public enum Access {
		VIEW, TALK, ADMIN
	}
	
	public static final int JOIN_ROLE = 0;
	public static final int TALK_ROLE = 1;
	public static final int ADMIN_ROLE = 2;

	private String _name;
	private String _desc;
	private SampleRate _rate;
	private int _maxUsers;
	private String _freq;
	
	private GeoLocation _center;
	private int _range;
	
	private Pilot _owner;
	private boolean _isDefault;
	
	private final Collection<String> _viewRoles = new TreeSet<String>();
	private final Collection<String> _talkRoles = new TreeSet<String>();
	private final Collection<String> _adminRoles = new TreeSet<String>();
	
	private final Collection<AirlineInformation> _airlines = new TreeSet<AirlineInformation>();
	
	/**
	 * Creates a new Channel.
	 * @param name the Channel name
	 * @throws NullPointerException if name is null
	 */
	public Channel(String name) {
		super();
		setName(name);
	}
	
	/**
	 * Returns the center of a range-limited Channel.
	 * @return the Center GeoLocation, or null if not range-limited
	 */
	public GeoLocation getCenter() {
		return _center;
	}
	
	/**
	 * Returns the range of a range-limited Channel.
	 * @return the range in miles
	 */
	public int getRange() {
		return (_center != null) ? _range : 0;
	}
	
	/**
	 * Returns the Channel's sampling rate.
	 * @return the rate
	 */
	public SampleRate getSampleRate() {
		return _rate;
	}
	
	/**
	 * Returns the maximum number of users in this channel.
	 * @return the maximum number of users
	 */
	public int getMaxUsers() {
		return _maxUsers;
	}
	
	/**
	 * Returns the Airlines associated with this channel.
	 * @return a Collection of AirlineInformation beans
	 */
	public Collection<AirlineInformation> getAirlines() {
		return _airlines;
	}
	
	/**
	 * Returns the Channel name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the channel description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the channel frequency.
	 * @return the frequency
	 */
	public String getFrequency() {
		return _freq;
	}
	
	/**
	 * Returns the Channel owner.
	 * @return the owning Pilot, or null if not temporary
	 */
	public Pilot getOwner() {
		return _owner;
	}
	
	/**
	 * Returns if this is a temporary channel that will be removed when its owner/creator logs out.
	 * @return TRUE if temporary, otherwise FALSE
	 */
	public boolean getIsTemporary() {
		return (_owner != null);
	}
	
	/**
	 * Returns if this is the default Channel.
	 * @return TRUE if the default Channel, otherwise FALSE
	 */
	public boolean getIsDefault() {
		return _isDefault;
	}
	
	public Collection<String> getViewRoles() {
		return _viewRoles;
	}
	
	public Collection<String> getTalkRoles() {
		return _talkRoles;
	}
	
	public Collection<String> getAdminRoles() {
		return _adminRoles;
	}
	
	public void addViewRole(String role) {
		_viewRoles.add(role);
	}
	
	public void addViewRoles(Collection<String> roles) {
		_viewRoles.addAll(roles);
	}
	
	public void addTalkRole(String role) {
		_talkRoles.add(role);
	}
	
	public void addTalkRoles(Collection<String> roles) {
		_talkRoles.addAll(roles);
	}
	
	public void addAdminRole(String role) {
		_adminRoles.add(role);
	}
	
	/**
	 * Updates the Channel's sampling rate.
	 * @param rt the rate
	 */
	public void setSampleRate(SampleRate rt) {
		_rate = rt;
	}
	
	/**
	 * Updates the maximum number of users in the channel.
	 * @param users the maximum number of users
	 */
	public void setMaxUsers(int users) {
		_maxUsers = Math.max(0, users);
	}
	
	/**
	 * Sets the Center of a range-limited channel.
	 * @param loc the Center
	 */
	public void setCenter(GeoLocation loc) {
		_center = loc;
	}
	
	/**
	 * Sets the range of a range-limited channel.
	 * @param range the range in miles
	 */
	public void setRange(int range) {
		_range = Math.max(0, range);
	}
	
	/**
	 * Sets the owner of a temporary Channel.
	 * @param usr the Owner
	 */
	public void setOwner(Pilot usr) {
		_owner = usr;
	}

	/**
	 * Updates whether this is the default Channel.
	 * @param isDefault TRUE if default, otherwise FALSE
	 */
	public void setIsDefault(boolean isDefault) {
		_isDefault = isDefault;
	}
	
	/**
	 * Updates the Channel name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the channel description.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates the channel frequency.
	 * @param freq the frequency
	 */
	public void setFrequency(String freq) {
		_freq = freq;
	}
	
	/**
	 * Sets the Airlines associated with this channel.
	 * @param airlines a Collection of AirlineInformation beans
	 */
	public void setAirlines(Collection<AirlineInformation> airlines) {
		_airlines.clear();
		if (airlines != null)
			_airlines.addAll(airlines);
	}
	
	public int hashCode() {
		return _name.hashCode();
	}
	
	public boolean equals(Object o) {
		return ((o instanceof Channel) && (compareTo((Channel) o) == 0));
	}

	/**
	 * Compares two Channels by comparing their names.
	 */
	public int compareTo(Channel c2) {
		return _name.compareTo(c2._name);
	}
	
	public String toString() {
		return _name;
	}
	
	public String getRowClassName() {
		if (_isDefault)
			return "opt2";
		
		return getIsTemporary() ? "opt3" : null;
	}
}