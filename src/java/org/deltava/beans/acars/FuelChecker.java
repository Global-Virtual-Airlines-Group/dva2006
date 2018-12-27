// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

/**
 * An interface to describe beans used for in-flight refuling checks.
 * @author Luke
 * @version 8.5
 * @since 8.5
 */

public interface FuelChecker {

	/**
	 * Returns the date/time of this entry.
	 * @return the date/time
	 */
	public Instant getDate();
	
	/**
	 * Returns the total amount of fuel remaining.
	 * @return the amount of fuel in pounds
	 */
	public int getFuelRemaining();
	
	/**
	 * Returns whether an ACARS state flag is set.
	 * @param flag the ACARSFlags
	 * @return TRUE if set, otherwise FALSE 
	 */
	public boolean isFlagSet(ACARSFlags flag);
}