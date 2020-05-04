// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An enumeration of external APIs.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum API {
	GoogleMaps, FlightAware, VATSIM, Google, WeatherCo;
	
	/**
	 * Combines the API with a method name to generate an API call name.
	 * @param methodName the method name
	 * @return the concatenated API and method name
	 */
	public String createName(String methodName) {
		return name() + "-" + methodName;
	}
}