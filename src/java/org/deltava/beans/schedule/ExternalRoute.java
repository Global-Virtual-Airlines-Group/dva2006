// Copyright 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to store an external Flight Route. 
 * @author Luke
 * @version 4.0
 * @since 2.6
 */

public class ExternalRoute extends FlightRoute implements ExternalFlightRoute {
	
	public static final String INTERNAL = "Internal";
	
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
	
	/**
	 * Returns whether this is an internal used route and should not be displayed.
	 * @return TRUE if internal, otherwise FALSE
	 */
	public boolean isInternal() {
		return INTERNAL.equals(_source);
	}
	
	public String getComboAlias() {
		return getRoute();
	}

	public String getComboName() {
		return toString();
	}
}