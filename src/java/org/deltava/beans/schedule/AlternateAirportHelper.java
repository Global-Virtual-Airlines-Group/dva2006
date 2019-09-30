// Copyright 2012, 2015, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.comparators.GeoComparator;

import org.deltava.util.system.SystemData;

/**
 * A helper class to calculate diversion airports. Airports are given a score based on maximum
 * runway length in excess of minimums and distance from the destination.
 * @author Luke
 * @version 8.7
 * @since 4.2
 */

@Helper(RoutePair.class)
public class AlternateAirportHelper {
	
	// Optimum distance from destination in miles
	private static final int MIN_DEST_DELTA = 55;
	
	private final String _appCode;

	/**
	 * Creates the helper
	 * @param appCode the virtual airline code
	 */
	public AlternateAirportHelper(String appCode) {
		super();
		_appCode = appCode;
	}
	
	private class Score implements Comparable<Score> {
		private final int _distance;
		private final int _airlineScore;
		private final int _aggScore;
		private final int _rwyScore;
		private final int _dstScore;
		
		Score(int airlines, int rwyDelta, int distance, int max) {
			super();
			_rwyScore = Math.min(50, (rwyDelta / 250));
			_dstScore = Math.min(50, ((max - distance) / 10));
			_airlineScore = Math.min(40, (airlines * 10));
			_aggScore = ((_rwyScore * 9) + (_dstScore * 6) + (_airlineScore * 5)) / 20;
			_distance = distance;
		}
		
		@Override
		public String toString() {
			return "RWY:" + _rwyScore + ",DST:" + _dstScore + ",AL:" + _airlineScore + ",DIST:" + _distance;
		}
		
		@Override
		public int compareTo(Score s2) {
			int tmpResult = Integer.compare(_aggScore, s2._aggScore);
			return (tmpResult == 0) ? Integer.compare(_distance, s2._distance) : tmpResult;
		}
	}
	
	/**
	 * Calculates alternate airports matching distance and runway length criteria. 
	 * @param ac the Aircraft used
	 * @param dst the destination Airport (or current position)
	 * @return a List of Airports, sorted by distance from the destination
	 */
	public List<Airport> calculateAlternates(Aircraft ac, GeoLocation dst) {
		
		int maxDistance = ac.getCruiseSpeed() * 3 / 4;
		TreeSet<Airport> airports = new TreeSet<Airport>(new GeoComparator(dst));
		airports.addAll(SystemData.getAirports().values());
		String dstCode = (dst instanceof ICAOAirport) ? ((ICAOAirport) dst).getICAO() : null;
		
		// Filter airports
		AircraftPolicyOptions opts = ac.getOptions(_appCode);
		Map<Score, Airport> results = new TreeMap<Score, Airport>(Collections.reverseOrder());
		for (Airport ap : airports) {
			int airlines = ap.getAirlineCodes().size();
			int distance = ap.getPosition().distanceTo(dst);
			int rwyDelta = (ap.getMaximumRunwayLength() - opts.getTakeoffRunwayLength());
			if ((distance > maxDistance) && (results.size() > 2))
				break;
			if ((rwyDelta < 0) || (ap.getICAO().equals(dstCode)))
				continue;
			
			// Adjust the distance if we are less than the minimum delta
			if ((dstCode != null) && (distance < MIN_DEST_DELTA))
				distance += (Math.abs(distance - MIN_DEST_DELTA) * 4);
			
			// Calculate the score
			Score s = new Score(airlines, rwyDelta, distance, maxDistance);
			results.put(s, ap);
		}
		
		return new ArrayList<Airport>(results.values());
	}
}