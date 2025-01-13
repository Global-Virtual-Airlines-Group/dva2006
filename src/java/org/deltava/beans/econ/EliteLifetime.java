// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import org.deltava.beans.Auditable;

/**
 * A bean to store lifetime Elite status levels.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class EliteLifetime implements Auditable, EliteLevelBean, EliteTotals, Comparable<EliteLifetime> {
	
	private String _name;
	private String _code;
	private EliteLevel _lvl;
	
	private int _legs;
	private int _distance;

	/**
	 * Creates the bean.
	 * @param name the status level name 
	 */
	public EliteLifetime(String name) {
		super();
		setName(name);
	}
	
	/**
	 * Returns the status level name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the status level abbreviation.
	 * @return the abbreviation
	 */
	public String getCode() {
		return _code;
	}

	@Override
	public int getLegs() {
		return _legs;
	}
	
	@Override
	public int getDistance() {
		return _distance;
	}
	
	@Override
	public int getPoints() {
		return 0;
	}
	
	@Override
	public int getColor() {
		return _lvl.getColor();
	}

	@Override
	public EliteLevel getLevel() {
		return _lvl;
	}

	@Override
	public void setLevel(EliteLevel lvl) {
		_lvl = lvl;
	}
	
	/**
	 * Updates the number of lifetime flight legs required for this status.
	 * @param l the number of legs
	 */
	public void setLegs(int l) {
		_legs = Math.max(0, l);
	}
	
	/**
	 * Updates the number of lifetime flight distance required for this status.
	 * @param d the distance in miles
	 */
	public void setDistance(int d) {
		_distance = Math.max(0, d);
	}
	
	/**
	 * Updates the name of this status level.
	 * @param name the level name
	 * @throws NullPointerException if name is null
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the abbreviation of this status level.
	 * @param c the code
	 * @throws NullPointerException if name is null
	 */
	public void setCode(String c) {
		_code = c.toUpperCase();
	}
	
	@Override
	public String getAuditID() {
		return toString();
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public int compareTo(EliteLifetime el2) {
		int tmpResult = _lvl.compareTo(el2._lvl);
		return (tmpResult == 0) ? _name.compareTo(el2._name) : tmpResult;
	}
}