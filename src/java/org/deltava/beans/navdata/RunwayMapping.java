// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store runway remappings.
 * @author Luke
 * @version 8.3
 * @since 8.3
 */

public class RunwayMapping implements Cacheable, Comparable<RunwayMapping> {
	
	private final String _icao;
	
	private String _oldCode;
	private String _newCode;

	/**
	 * Creates the bean.
	 * @param airportICAO the airport ICAO code 
	 */
	public RunwayMapping(String airportICAO) {
		super();
		_icao = airportICAO.toUpperCase();
	}
	
	/**
	 * Returns the old runway code.
	 * @return the code
	 */
	public String getOldCode() {
		return _oldCode;
	}
	
	/**
	 * Returns the new runway code.
	 * @return the code
	 */
	public String getNewCode() {
		return _newCode;
	}
	
	/**
	 * Returns the airport ICAO code.
	 * @return the code
	 */
	public String getICAO() {
		return _icao;
	}

	/**
	 * Updates the old runway code.
	 * @param r the code
	 */
	public void setOldCode(String r) {
		_oldCode = r.toUpperCase();
	}

	/**
	 * Updates the new runway code.
	 * @param r the code
	 */
	public void setNewCode(String r) {
		_newCode = r.toUpperCase();
	}

	@Override
	public Object cacheKey() {
		return _icao + "!!" + _oldCode;
	}
	
	@Override
	public int hashCode() {
		return cacheKey().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_icao);
		buf.append('-').append(_oldCode);
		buf.append('-').append(_newCode);
		return buf.toString();
	}

	@Override
	public int compareTo(RunwayMapping rm2) {
		int tmpResult = _icao.compareTo(rm2._icao);
		return (tmpResult == 0) ? _oldCode.compareTo(rm2._oldCode) : tmpResult;
	}
}