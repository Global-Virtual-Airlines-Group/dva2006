// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.ArrayList;

import org.deltava.beans.GeoLocation;

/**
 * A collection of points to store weather front/trough paths. 
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class WeatherLine extends ArrayList<GeoLocation> {

	private final Front _type;
	
	public WeatherLine(Front type) {
		super();
		_type = type;
	}
	
	/**
	 * Returns the line type.
	 * @return a Front
	 */
	public Front getType() {
		return _type;
	}
}