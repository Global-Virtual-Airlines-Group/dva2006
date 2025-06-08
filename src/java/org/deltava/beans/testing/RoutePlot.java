// Copyright 2008, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.schedule.*;

/**
 * An interface to mark Route Plotting questions and question profiles.
 * @author Luke
 * @version 12.0
 * @since 2.3
 */

public interface RoutePlot extends RoutePair {

	/**
	 * Updates the departure Airport.
	 * @param a the departure Airport bean
	 */
	public void setAirportD(Airport a);
	
	/**
	 * Updates the arrival Airport.
	 * @param a the arrival Airport bean
	 */
	public void setAirportA(Airport a);
}