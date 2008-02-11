// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.ComboAlias;
import static org.deltava.beans.MapEntry.COLORS;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A class for storing Airline information.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class Airline implements java.io.Serializable, ComboAlias, Comparable<Airline>, Cacheable {

	private String _code;
	private String _name;
	private String _color;
	private boolean _active = true;
	
	private final Collection<String> _apps = new TreeSet<String>();
	private final Collection<String> _codes = new HashSet<String>();
	
	/**
	 * Create a new Airline using a code.
	 * @param code the Airline code
	 * @throws NullPointerException if the name is null
	 * @see Airline#getCode()
	 */
	public Airline(String code) {
	    this(code, "");
	}
	
	/**
	 * Create a new Airline using a code and a name.
	 * @param code the Airline code
	 * @param name the Airline name
	 * @throws NullPointerException If either the name or the code are null
	 * @see Airline#getCode()
	 * @see Airline#getName()
	 */
	public Airline(String code, String name) {
		super();
		setCode(code);
		_name = name.trim();
		_codes.add(_code);
	}
	
	/**
	 * Returns the airline code.
	 * @return the airline code
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * Returns the web applications this airline is enabled for.
	 * @return a Collection of application codes
	 * @see Airline#setApps(Collection)
	 * @see Airline#addApp(String)
	 * @see org.deltava.beans.system.AirlineInformation#getCode()
	 */
	public Collection<String> getApplications() {
		return _apps;
	}
	
	/**
	 * Returns all valid airline codes for this Airline.
	 * @return a Collection of Airline codes
	 * @see Airline#addCode(String)
	 */
	public Collection<String> getCodes() {
		return _codes;
	}
	
	/**
	 * Returns the airline name.
	 * @return the airline name
	 * @see Airline#getName()
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns if the airline is active.
	 * @return TRUE if the airline is enabled, otherwise FALSE
	 * @see Airline#setActive(boolean)
	 */
	public boolean getActive() {
	    return _active;
	}
	
	/**
	 * The color to use then displaying this Airline's destinations or routes in a Google Map.
	 * @return the color name
	 * @see Airline#setColor(String)
	 * @see org.deltava.beans.MapEntry#COLORS
	 */
	public String getColor() {
		return _color;
	}
	
	/**
	 * Updates the airline's activity flag.
	 * @param active TRUE if the airline is enabled, otherwise FALSE
	 * @see Airline#getActive()
	 */
	public void setActive(boolean active) {
	    _active = active;
	}
	
	/**
	 * Adds a web application to this Airline.
	 * @param app the application code
	 * @see Airline#setApps(Collection)
	 * @see Airline#getApplications()
	 * @see org.deltava.beans.system.AirlineInformation#getCode()
	 */
	public void addApp(String app) {
		_apps.add(app);
	}

	/**
	 * Updates the web applications this Airline is enabled for.
	 * @param apps a Collection of application codes
	 * @see Airline#getApplications()
	 * @see Airline#addApp(String)
	 * @see org.deltava.beans.system.AirlineInformation#getCode()
	 */
	public void setApps(Collection<String> apps) {
		_apps.clear();
		_apps.addAll(apps);
	}
	
	/**
	 * Adds an alternate airline code to this Airline.
	 * @param code the airline code
	 * @throws NullPointerException if code is null
	 * @see Airline#getCodes()
	 */
	public void addCode(String code) {
		_codes.add(code.trim().toUpperCase());
	}
	
	/**
	 * Updates the Airline's primary code.
	 * @param code the airline code
	 * @throws NullPointerException if code is null
	 * @see Airline#getCode()
	 */
	public void setCode(String code) {
		_code = code.trim().toUpperCase();
	}
	
	/**
	 * Updates the color used when displaying this Airline's routes and destinations in a Google Map.
	 * @param color the color code
	 * @throws IllegalArgumentException if not a valid Google Map color
	 * @see Airline#getColor()
	 * @see org.deltava.beans.MapEntry#COLORS
	 */
	public void setColor(String color) {
		if (StringUtils.arrayIndexOf(COLORS, color) == -1)
			throw new IllegalArgumentException("Invalid Google Map color - " + color);
		
		_color = color;
	}
	
	/**
	 * Clears and updates the list of alternate airline codes.
	 * @param codes a Collection of airline codes
	 * @throws NullPointerException if codes is null
	 * @see Airline#addCode(String)
	 * @see Airline#getCodes()
	 */
	public void setCodes(Collection<String> codes) {
		_codes.clear();
		_codes.add(_code);
		for (Iterator<String> i = codes.iterator(); i.hasNext(); )
			addCode(i.next());
	}

	public String getComboAlias() {
		return _code;
	}

	public String getComboName() {
		StringBuilder buf = new StringBuilder(getCode());
		buf.append(" - ");
		buf.append(getName());
		return buf.toString();
	}

	/**
	 * Updates the airline name.
	 * @param name the Airline Name
	 * @throws NullPointerException if name is null
	 * @see Airline#getName()
	 */
	public void setName(String name) {
	    _name = name.trim();
	}
	
	/**
	 * Airline object comparator - compare the codes.
	 */
	public int compareTo(Airline a2) {
		return _code.compareTo(a2._code);
	}
	
	public final boolean equals(Object o2) {
	    return (o2 instanceof Airline) ? (compareTo((Airline) o2) == 0) : false;
	}
	
	public int hashCode() {
		return _code.hashCode();
	}
	
	/**
	 * Returns the airline code.
	 */
	public String toString() {
		return _code;
	}
	
	/**
	 * Cache key.
	 * @return the Airline code
	 */
	public Object cacheKey() {
	    return _code;
	}
}