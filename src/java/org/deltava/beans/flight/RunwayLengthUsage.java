// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An interface for beans that display runway usage on takeoff and landing 
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public interface RunwayLengthUsage {

	/**
	 * Returns the runway length.
	 * @return the length in feet
	 */
	public int getLength();
	
	/**
	 * Returns the runway usage from the threshold.
	 * @return the distance in feet.
	 */
	public int getDistance();
	
	/**
	 * Returns the percentage of the runway used.
	 * @return the percentage from 0 to 1
	 */
	public default double getPercentage() {
		return getDistance() * 1d / getLength();
	}
}