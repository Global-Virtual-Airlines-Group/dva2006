// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

/**
 * A utility class to calculate time spend online.
 * @author Luke
 * @version 3.6
 * @since 3.6
 */

public class OnlineTime {

	// singleton
	private OnlineTime() {
		super();
	}

	/**
	 * Calculates the time spent online from a series of positions.
	 * @param positions a Collection of PositionData beans
	 * @param maxGap the maximum gap between positions in minutes
	 * @return the estimated online time in seconds
	 */
	public static int calculate(Collection<PositionData> positions, int maxGap) {
		if (positions == null) return 0;
		
		int gapSeconds = Math.max(maxGap, 3) * 60; 
		long lastTime = 0; long totalTime = 0;
		for (Iterator<PositionData> i = positions.iterator(); i.hasNext(); ) {
			PositionData pd = i.next();
			long posTime = pd.getDate().getTime();
			if (lastTime != 0) {
				long gap = (posTime - lastTime) / 1000;
				if (gap <= gapSeconds)
					totalTime += gap;
			}
			
			lastTime = posTime;
		}
		
		// Return total time
		return (int) totalTime;
	}
}