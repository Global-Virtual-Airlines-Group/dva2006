// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A Flight Route loaded from an external party. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public interface ExternalFlightRoute {
	
	/**
	 * Returns the source of this route.
	 * @return the source name
	 */
	public abstract String getSource();

	/**
	 * Sets the source of this route.
	 * @param src the source name
	 */
	public abstract void setSource(String src);
}