// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.ComboAlias;

/**
 * A bean to store country names and ISO-3316 codes.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class Country implements java.io.Serializable, Comparable<Country>, ComboAlias {

	private static final Map<String, Country> _countries = new TreeMap<String, Country>();
	
	private String _code;
	private String _name;
	
	/**
	 * Initializes a country bean.
	 * @param code the ISO-3316 country code
	 * @param name the country name
	 */
	public static synchronized void init(String code, String name) {
		Country c = new Country(code, name);
		if (!_countries.containsKey(c.getCode()))
			_countries.put(c.getCode(), c);
	}
	
	/**
	 * Returns a country.
	 * @param code the ISO-3316 country code
	 * @return a Country, or null if not found
	 * @throws NullPointerException if code is null
	 */
	public static Country get(String code) {
		return _countries.get(code.toUpperCase());
	}
	
	/**
	 * Returns all Countries.
	 * @return a Collection of Country beans
	 */
	public static Collection<Country> getAll() {
		return new ArrayList<Country>(_countries.values());
	}
	
	/**
	 * Creates the bean. This is private so no country can be initialized twice.
	 * @param code the ISO-3316 country code
	 * @param name the country name
	 * @throws NullPointerException if code is null
	 */
	private Country(String code, String name) {
		super();
		_code = code.toUpperCase().trim();
		_name = name;
	}
	
	/**
	 * Returns the ISO-3316 country code.
	 * @return the code
	 */
	public String getCode() {
		return _code;
	}

	/**
	 * Returns the country name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	public String getComboName() {
		return _name;
	}
	
	public String getComboAlias() {
		return _code;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(_name);
		buf.append(" (");
		buf.append(_code);
		buf.append(')');
		return buf.toString();
	}
	
	public int hashCode() {
		return _code.hashCode();
	}
	
	public boolean equals(Object o) {
		return ((o instanceof Country) && ((Country) o).getCode().equals(_code));
	}
	
	public int compareTo(Country c2) {
		return _name.compareTo(c2._name);
	}
}