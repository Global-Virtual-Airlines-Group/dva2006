// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Aircraft type information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Aircraft implements Comparable, Cacheable {
	
	private String _name;
	private int _maxRange;
	
	private final Collection<String> _iataCodes = new TreeSet<String>();
	private final Collection<AirlineInformation> _airlines = new HashSet<AirlineInformation>();

	/**
	 * Initializes the bean.
	 * @param name the equipment name
	 * @throws NullPointerException if name is null 
	 */
	public Aircraft(String name) {
		super();
		setName(name);
	}
	
	/**
	 * Returns the aircraft name.
	 * @return the name
	 * @see Aircraft#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the maximum range of the aircraft.
	 * @return the range in miles
	 * @see Aircraft#setRange(int)
	 */
	public int getRange() {
		return _maxRange;
	}
	
	/**
	 * Returns the aircraft's IATA equipment code(s).
	 * @return a sorted Collection of IATA codes
	 * @see Aircraft#addIATA(String)
	 * @see Aircraft#setIATA(Collection)
	 */
	public Collection<String> getIATA() {
		return new TreeSet<String>(_iataCodes);
	}
	
	/**
	 * Returns all web applications using this aircraft type.
	 * @return a Collection of AirlineInformation beans
	 * @see Aircraft#isUsed(String)
	 * @see Aircraft#addApp(AirlineInformation)
	 * @see AirlineInformation
	 */
	public Collection<AirlineInformation> getApps() {
		return new LinkedHashSet<AirlineInformation>(_airlines);
	}
	
	/**
	 * Returns wether a particular web application uses this aircraft type.
	 * @param code the web application airline code
	 * @return TRUE if the aircraft is used by this web application, otherwise FALSE
	 * @see Aircraft#getApps()
	 * @see Aircraft#addApp(AirlineInformation)
	 */
	public boolean isUsed(String code) {
		for (Iterator<AirlineInformation> i = _airlines.iterator(); i.hasNext(); ) {
			AirlineInformation ai = i.next();
			if (ai.getCode().equalsIgnoreCase(code))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Marks this aircraft type as used by a particular web application.
	 * @param ai the AirlineInformation bean
	 * @see Aircraft#isUsed(String)
	 * @see Aircraft#getApps()
	 * @see AirlineInformation
	 */
	public void addApp(AirlineInformation ai) {
		_airlines.add(ai);
	}
	
	/**
	 * Updates the aircraft name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see Aircraft#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the maximum range of the aircraft.
	 * @param range the range in miles
	 * @throws IllegalArgumentException if range is zero or negative
	 * @see Aircraft#getRange()
	 */
	public void setRange(int range) {
		if (range < 1)
			throw new IllegalArgumentException("Invalid Range - " +  range);
		
		_maxRange = range;
	}
	
	/**
	 * Links an IATA equipment code to this aircraft.
	 * @param code the equipment code
	 * @throws NullPointerException if code is null
	 * @see Aircraft#getIATA()
	 * @see Aircraft#setIATA(Collection)
	 */
	public void addIATA(String code) {
		_iataCodes.add(code.trim().toUpperCase());
	}
	
	/**
	 * Updates this aircraft's IATA codes.
	 * @param codes a Collection of codes
	 * @see Aircraft#addIATA(String)
	 * @see Aircraft#getIATA()
	 */
	public void setIATA(Collection<String> codes) {
		_iataCodes.clear();
		if (codes != null)
			_iataCodes.addAll(codes);
	}

	/**
	 * Compares two aircraft by comparing their names.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Aircraft a2 = (Aircraft) o;
		return _name.compareTo(a2._name);
	}
	
	public boolean equals(Object o) {
		return (o instanceof Aircraft) ? (compareTo(o) == 0) : false;
	}

	/**
	 * Returns the aircraft name.
	 */
	public String toString() {
		return _name;
	}
	
	/**
	 * Returns the aircraft name hash code.
	 */
	public int hashCode() {
		return _name.hashCode();
	}
	
	/**
	 * Returns the aircrat name.
	 */
	public Object cacheKey() {
		return _name;
	}
}