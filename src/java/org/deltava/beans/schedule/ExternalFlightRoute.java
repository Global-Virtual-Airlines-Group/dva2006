// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A Flight Route loaded from an external party. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class ExternalFlightRoute extends FlightRoute {
	
	private String _source;

	public String getComboAlias() {
		return getRoute();
	}

	public String getComboName() {
		return getRoute();
	}
	
	/**
	 * Returns the source of this route.
	 * @return the source name
	 */
	public String getSource() {
		return _source;
	}
	
	/**
	 * Sets the source of this route.
	 * @param src the source name
	 */
	public void setSource(String src) {
		_source = src;
	}

	public int hashCode() {
		return getRoute().hashCode();
	}
	
	public String toString() {
		return getRoute();
	}
}