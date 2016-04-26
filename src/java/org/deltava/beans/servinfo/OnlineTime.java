// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.beans.Helper;
import org.deltava.beans.flight.FlightReport;

/**
 * A utility class to calculate time spend online.
 * @author Luke
 * @version 7.0
 * @since 3.6
 */

@Helper(FlightReport.class)
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
		for (PositionData pd : positions) {
			long posTime = pd.getDate().toEpochMilli();
			if (lastTime != 0) {
				long gap = (posTime - lastTime) / 1000;
				if (gap <= gapSeconds)
					totalTime += gap;
			}
			
			lastTime = posTime;
		}
		
		return (int) totalTime;
	}
}