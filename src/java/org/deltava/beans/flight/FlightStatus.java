// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.ViewEntry;

/**
 * An enumeration to store Flight Report states. 
 * @author Luke
 * @version 8.4
 * @since 8.1
 */

public enum FlightStatus implements ViewEntry {
	DRAFT("opt2", ""), SUBMITTED("opt1", ""), HOLD("warn", "hold"), OK(null, "approve"), REJECTED("err", "reject");

	private final String _viewClass;
	private final String _verb;
	
	/**
	 * Creates the enumeration value.
	 * @param viewClass the row CSS class name
	 */
	FlightStatus(String viewClass, String verb) {
		_viewClass = viewClass;
		_verb = verb;
	}
	
	/**
	 * Returns the status description. 
	 * @return the description
	 */
	public String getDescription() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
	
	/**
	 * Returns the operation verb name.
	 * @return the operation
	 */
	public String getVerb() {
		return _verb;
	}

	@Override
	public String getRowClassName() {
		return _viewClass;
	}
	
	/**
	 * Exception-safe way to load FlightStatus from Flight Report approval verb.
	 * @param verb the verb
	 * @return a FlightStatus, or null if unknown
	 */
	public static FlightStatus fromVerb(String verb) {
		for (FlightStatus fs : values()) {
			if (fs.getVerb().equalsIgnoreCase(verb))
				return fs;
		}
		
		return null;
	}
}