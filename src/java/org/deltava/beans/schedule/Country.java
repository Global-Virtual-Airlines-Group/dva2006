// Copyright 2010, 2011, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.ComboAlias;

/**
 * A bean to store country names and ISO-3316 codes.
 * @author Luke
 * @version 8.7
 * @since 3.2
 */

public class Country implements java.io.Serializable, Comparable<Country>, ComboAlias {
	
	/**
	 * International Airspace.
	 */
	public static final Country INTL = new Country("??", "International", null);
	
	/**
	 * Unknown Country.
	 */
	public static final Country UNKNOWN = new Country("ZZ", "Unknown", null);

	private static final Map<String, Country> _countries = new TreeMap<String, Country>() {{ put(INTL.getCode(), INTL); put(UNKNOWN.getCode(), UNKNOWN); }};
	
	private final String _code;
	private final String _name;
	private final Continent _continent;
	
	/**
	 * Initializes a country bean.
	 * @param code the ISO-3316 country code
	 * @param name the country name
	 * @param cont the continent
	 */
	public static synchronized void init(String code, String name, Continent cont) {
		Country c = new Country(code, name, cont);
		_countries.putIfAbsent(c.getCode(), c);
	}
	
	/**
	 * Returns a country.
	 * @param code the ISO-3316 country code
	 * @return a Country, or null if not found
	 * @throws NullPointerException if code is null
	 */
	public static Country get(String code) {
		return _countries.getOrDefault(code.toUpperCase(), Country.UNKNOWN);
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
	 * @param cont the Continent
	 * @throws NullPointerException if code is null
	 */
	private Country(String code, String name, Continent cont) {
		super();
		_code = code.toUpperCase().trim();
		_name = name;
		_continent = cont;
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
	
	/**
	 * Returns the continent.
	 * @return the continent
	 */
	public Continent getContinent() {
		return _continent;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return _code;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_name);
		buf.append(" (");
		buf.append(_code);
		buf.append(')');
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return _code.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Country) && ((Country) o).getCode().equals(_code);
	}
	
	@Override
	public int compareTo(Country c2) {
		return _name.compareTo(c2._name);
	}
}