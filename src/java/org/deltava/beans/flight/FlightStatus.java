// Copyright 2018, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.*;

/**
 * An enumeration to store Flight Report states. 
 * @author Luke
 * @version 10.2
 * @since 8.1
 */

public enum FlightStatus implements ViewEntry, EnumDescription {
	DRAFT("opt2", "", false), SUBMITTED("opt1", "", false), HOLD("warn", "hold", false), OK(null, "approve", true), REJECTED("err", "reject", true);

	private final String _viewClass;
	private final String _verb;
	private final boolean _isComplete;
	
	/**
	 * Creates the enumeration value.
	 * @param viewClass the row CSS class name
	 * @param verb the verb name
	 * @param isComplete TRUE if a final status, otherwise FALSE
	 */
	FlightStatus(String viewClass, String verb, boolean isComplete) {
		_viewClass = viewClass;
		_verb = verb;
		_isComplete = isComplete;
	}
	
	/**
	 * Returns the operation verb name.
	 * @return the operation
	 */
	public String getVerb() {
		return _verb;
	}
	
	/**
	 * Returns whether this is a final state for a Flight Report.
	 * @return TRUE if final, otherwise FALSE
	 */
	public boolean getIsComplete() {
		return _isComplete;
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