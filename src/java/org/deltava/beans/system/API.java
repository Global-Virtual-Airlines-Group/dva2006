// Copyright 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An enumeration of external APIs.
 * @author Luke
 * @version 11.6
 * @since 9.0
 */

public enum API {
	GoogleMaps, FlightAware, VATSIM, Google, RainViewer;
	
	/**
	 * Combines the API with a method name to generate an API call name.
	 * @param methodName the method name
	 * @return the concatenated API and method name
	 */
	public String createName(String methodName) {
		return name() + "-" + methodName;
	}
}