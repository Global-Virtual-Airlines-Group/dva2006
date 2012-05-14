// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A helper class to calculate diversion airports. Airports are given a score based on maximum
 * runway length in excess of minimums and distance from the destination.
 * @author Luke
 * @version 4.2
 * @since 4.2
 */

@Helper(RoutePair.class)
public class AlternateAirportHelper {

	private AlternateAirportHelper() {
		super();
	}
	
	private static class Score implements Comparable<Score> {
		private final int _distance;
		private final int _aggScore;
		private final int _rwyScore;
		private final int _dstScore;
		
		Score(int rwyDelta, int distance, int max) {
			super();
			_rwyScore = Math.min(50, (rwyDelta / 250));
			_dstScore = Math.min(50, ((max - distance) / 10));
			_aggScore = ((_rwyScore * 4) + (_dstScore * 6)) / 10;
			_distance = distance;
		}
		
		public String toString() {
			return "R:" + _rwyScore + ",D:" + _dstScore + ",DST:" + _distance;
		}
		
		public int compareTo(Score s2) {
			int tmpResult = Integer.valueOf(_aggScore).compareTo(Integer.valueOf(s2._aggScore));
			return (tmpResult == 0) ? Integer.valueOf(_distance).compareTo(Integer.valueOf(s2._distance)) : tmpResult;
		}
	}
	
	/**
	 * Calculates alternate airports matching distance and runway length criteria. 
	 * @param ac the Aircraft used
	 * @param dst the destination Airport (or current position)
	 * @return a List of Airports, sorted by distance from the destination
	 */
	public static List<Airport> calculateAlternates(Aircraft ac, GeoLocation dst) {
		
		int maxDistance = ac.getCruiseSpeed() / 2;
		Map<Score, Airport> results = new TreeMap<Score, Airport>(Collections.reverseOrder());
		Collection<Airport> airports = new HashSet<Airport>(SystemData.getAirports().values());
		String dstCode = (dst instanceof ICAOAirport) ? ((ICAOAirport) dst).getICAO() : null;
		
		// Filter airports
		for (Airport ap : airports) {
			int distance = ap.getPosition().distanceTo(dst);
			int rwyDelta = (ap.getMaximumRunwayLength() - ac.getTakeoffRunwayLength());
			if ((rwyDelta < 0) || (distance > maxDistance) || (ap.getICAO().equals(dstCode)))
				continue;
			
			// Calculate the score
			Score s = new Score(rwyDelta, distance, maxDistance);
			results.put(s, ap);
		}
		
		// Sort based on distance to destination
		return new ArrayList<Airport>(results.values());
	}
}