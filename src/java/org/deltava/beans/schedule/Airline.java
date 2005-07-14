package org.deltava.beans.schedule;

import java.io.Serializable;

import org.deltava.beans.ComboAlias;
import org.deltava.util.cache.Cacheable;

/**
 * A class for storing airline information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Airline implements Serializable, ComboAlias, Comparable, Cacheable {

	private String _code;
	private String _name;
	private boolean _active = true;
	
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
		_code = code.trim().toUpperCase();
		_name = name.trim();
	}
	
	/**
	 * Returns the airline code.
	 * @return the airline code
	 */
	public String getCode() {
		return _code;
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
	 */
	public boolean getActive() {
	    return _active;
	}
	
	/**
	 * Updates the airline's activity flag.
	 * @param active TRUE if the airline is enabled, otherwise FALSE
	 */
	public void setActive(boolean active) {
	    _active = active;
	}

	public String getComboAlias() {
		return getCode();
	}

	public String getComboName() {
		return getName();
	}

	/**
	 * Updates the airline name.
	 * @param name the Airline Name
	 * @throws NullPointerException if name is null
	 */
	public void setName(String name) {
	    _name = name.trim();
	}
	
	/**
	 * Airline object comparator - compare the codes
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o2) {
		Airline a2 = (Airline) o2;
		return _code.compareTo(a2.getCode());
	}
	
	public final boolean equals(Object o2) {
	    return (o2 instanceof Airline) ? (compareTo(o2) == 0) : false;
	}
	
	public final int hashCode() {
	    return _code.hashCode();
	}
	
	/**
	 * Cache key.
	 */
	public Object cacheKey() {
	    return getCode();
	}
}