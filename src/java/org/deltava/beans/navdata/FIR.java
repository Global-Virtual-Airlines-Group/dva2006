// Copyright 2010, 2012, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.GeoLocation;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store data about a Flight Information Region or ARTCC.
 * @author Luke
 * @version 6.0
 * @since 3.2
 */

public class FIR implements Comparable<FIR>, Cacheable {
	
	private final String _id;
	private boolean _oceanic;
	private boolean _aux;
	private String _name;
	
	private final Collection<String> _aliases = new TreeSet<String>();
	private final Collection<GeoLocation> _border = new LinkedHashSet<GeoLocation>();

	/**
	 * Creates the bean.
	 * @param id the FIR ID
	 * @throws NullPointerException if id is null
	 * @see FIR#getID() 
	 */
	public FIR(String id) {
		super();
		_id = id.trim().toUpperCase();
	}

	/**
	 * Returns the FIR name.
	 * @return the name
	 * @see FIR#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the FIR ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns if this is an Oceanic sector.
	 * @return TRUE if Oceanic, otherwise FALSE
	 * @see FIR#setOceanic(boolean)
	 */
	public boolean isOceanic() {
		return _oceanic;
	}
	
	/**
	 * Returns if this is an Auxilliary sector.
	 * @return TRUE if Auxilliary, otherwise FALSE
	 * @see FIR#setAux(boolean)
	 */
	public boolean isAux() {
		return _aux;
	}
	
	/**
	 * Returns the coordinates of the FIR boundary.
	 * @return a Collection of GeoLocations
	 * @see FIR#addBorderPoint(GeoLocation)
	 */
	public Collection<GeoLocation> getBorder() {
		return new ArrayList<GeoLocation>(_border);
	}
	
	/**
	 * Returns the aliases of this FIR.
	 * @return a Collection of aliases
	 * @see FIR#addAlias(String)
	 */
	public Collection<String> getAliases() {
		return new ArrayList<String>(_aliases);
	}
	
	/**
	 * Sets the FIR name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see FIR#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Sets whether this is an Auxilliary sector.
	 * @param isAux TRUE if auxilliary, otherwise FALSE
	 * @see FIR#isAux()
	 */
	public void setAux(boolean isAux) {
		_aux = isAux;
	}
	
	/**
	 * Sets whether this is an Oceanic sector.
	 * @param isOceanic TRUE if oceanic, otherwise FALSE
	 * @see FIR#isOceanic()
	 */
	public void setOceanic(boolean isOceanic) {
		_oceanic = isOceanic;
	}
	
	/**
	 * Adds a point to the FIR boundary.
	 * @param loc a GeoLocation
	 * @see FIR#getBorder()
	 */
	public void addBorderPoint(GeoLocation loc) {
		_border.add(loc);
	}
	
	/**
	 * Adds an alias for this FIR.
	 * @param code the alias
	 * @throws NullPointerException if code is null
	 * @see FIR#getAliases()
	 */
	public void addAlias(String code) {
		_aliases.add(code.trim().toUpperCase());
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_id);
		if (_oceanic)
			buf.append(" Oceanic");
		
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public Object cacheKey() {
		return toString();
	}

	/**
	 * Compares two FIRs by comparing their IDs.
	 */
	@Override
	public int compareTo(FIR f2) {
		int tmpResult = _id.compareTo(f2._id);
		return (tmpResult == 0) ? Boolean.valueOf(_oceanic).compareTo(Boolean.valueOf(f2._oceanic)) : tmpResult;
	}
}