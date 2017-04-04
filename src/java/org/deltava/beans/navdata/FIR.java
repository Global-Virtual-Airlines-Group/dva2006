// Copyright 2010, 2012, 2014, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

/**
 * A bean to store data about a Flight Information Region or ARTCC.
 * @author Luke
 * @version 7.3
 * @since 3.2
 */

public class FIR extends Airspace {
	
	private boolean _oceanic;
	private boolean _aux;
	
	private final Collection<String> _aliases = new TreeSet<String>();

	/**
	 * Creates the bean.
	 * @param id the FIR ID
	 */
	public FIR(String id) {
		super(id, AirspaceType.CTR);
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
	 * Returns the aliases of this FIR.
	 * @return a Collection of aliases
	 * @see FIR#addAlias(String)
	 */
	public Collection<String> getAliases() {
		return new ArrayList<String>(_aliases);
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
		StringBuilder buf = new StringBuilder(getID());
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
}