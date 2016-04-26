// Copyright 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.beans.system.AirlineInformation;

/**
 * A bean to store data about an MVS voice channel. 
 * @author Luke
 * @version 7.0
 * @since 4.0
 */

public class Channel extends DatabaseBean implements ViewEntry {
	
	/**
	 * Lobby name.
	 */
	public static final String DEFAULT_NAME = "Lobby";
	
	/**
	 * Access rights enumeration.
	 */
	public enum Access {
		VIEW, TALK, TALK_IF_PRESENT, ADMIN
	}
	
	private String _name;
	private String _desc;
	private SampleRate _rate;
	private int _maxUsers;
	private String _freq;
	private int _range;
	
	private Pilot _owner;
	private boolean _isDefault;
	
	private final Map<Access, Collection<String>> _roles = new HashMap<Access, Collection<String>>() {{
		put(Access.VIEW, new TreeSet<String>());
		put(Access.TALK, new TreeSet<String>());
		put(Access.TALK_IF_PRESENT, new TreeSet<String>());
		put(Access.ADMIN, new TreeSet<String>());
	}};
	
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
	 * Returns the range of a range-limited Channel.
	 * @return the range in miles from the sender
	 */
	public int getRange() {
		return _range;
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
	
	public Collection<String> getRoles(Access a) {
		return _roles.get(a);
	}
	
	public Collection<String> getViewRoles() {
		return getRoles(Access.VIEW);
	}
	
	public Collection<String> getTalkRoles() {
		return getRoles(Access.TALK);
	}
	
	public Collection<String> getDynTalkRoles() {
		return getRoles(Access.TALK_IF_PRESENT);
	}
	
	public Collection<String> getAdminRoles() {
		return getRoles(Access.ADMIN);
	}
	
	public void addRole(Access a, String role) {
		if (_roles.containsKey(a))
			_roles.get(a).add(role);
	}
	
	public void addRoles(Access a, Collection<String> roles) {
		if ((roles != null) && _roles.containsKey(a))
			_roles.get(a).addAll(roles);
	}
	
	/**
	 * Clears all access role lists.
	 */
	public void clearRoles() {
		for (Collection<String> roles : _roles.values())
			roles.clear();
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
	 * @param a an AirlineInformation bean
	 */
	public void addAirline(AirlineInformation a) {
		_airlines.add(a);
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return ((o instanceof Channel) && (compareTo((Channel) o) == 0));
	}

	/**
	 * Compares two Channels by comparing their names.
	 */
	public int compareTo(Channel c2) {
		return _name.compareTo(c2._name);
	}
	
	@Override
	public String toString() {
		return _name;
	}

	@Override
	public String getRowClassName() {
		return getIsTemporary() ? "opt3" : null;
	}
}