// Copyright 2009, 2010, 2011, 2016, 2017, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.UseCount;

/**
 * A bean to store an external Flight Route. 
 * @author Luke
 * @version 11.2
 * @since 2.6
 */

public class ExternalRoute extends FlightRoute implements ExternalFlightRoute, UseCount {
	
	public static final String INTERNAL = "Internal";
	
	private String _source;
	private int _count;

	/**
	 * Creates a new External route.
	 * @param src the route source
	 */
	public ExternalRoute(String src) {
		super();
		_source = src;
	}
	
	@Override
	public String getSource() {
		return _source;
	}
	
	@Override
	public int getUseCount() {
		return _count;
	}
	
	@Override
	public void setSource(String src) {
		_source = src;
	}
	
	/**
	 * Updates the number of times this route was used.
	 * @param cnt the number of times
	 */
	public void setUseCount(int cnt) {
		_count = cnt;
	}
	
	/**
	 * Returns whether this is an internal used route and should not be displayed.
	 * @return TRUE if internal, otherwise FALSE
	 */
	public boolean isInternal() {
		return INTERNAL.equals(_source);
	}
	
	@Override
	public String getComboAlias() {
		return getRoute();
	}

	@Override
	public String getComboName() {
		return toString();
	}
}