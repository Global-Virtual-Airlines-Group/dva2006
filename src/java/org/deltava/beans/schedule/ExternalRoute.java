// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to store an external Flight Route. 
 * @author Luke
 * @version 3.4
 * @since 2.6
 */

public class ExternalRoute extends FlightRoute implements ExternalFlightRoute {
	
	private String _source;

	/**
	 * Creates a new External route.
	 * @param src the route source
	 */
	public ExternalRoute(String src) {
		super();
		_source = src;
	}
	
	public String getSource() {
		return _source;
	}
	
	public void setSource(String src) {
		_source = src;
	}
	
	public String getComboAlias() {
		return getRoute();
	}

	public String getComboName() {
		return toString();
	}
}