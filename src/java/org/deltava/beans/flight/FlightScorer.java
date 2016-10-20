// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.Helper;
import org.deltava.beans.acars.RunwayDistance;
import org.deltava.beans.navdata.Runway;
import org.deltava.beans.stats.LandingStatistics;

/**
 * A utility class to grade flights.
 * @author Luke
 * @version 7.2
 * @since 5.1
 */

@Helper(FlightReport.class)
public class FlightScorer {

	// static class
	private FlightScorer() {
		super();
	}

	/*
	 * Does the actual landing scoring.
	 */
	private static FlightScore score(int fpm, double rwyPct) {
		if ((fpm < -600) || (rwyPct > 0.45))
			return FlightScore.DANGEROUS;
		else if ((fpm < -300) || (fpm > -74) || (rwyPct > 0.35) || (rwyPct < 0.075))
			return FlightScore.ACCEPTABLE;

		return FlightScore.OPTIMAL;
	}

	/**
	 * Scores a landing.
	 * @param ls a LandingStats bean
	 * @return a FlightScore
	 */
	public static FlightScore score(LandingStatistics ls) {
		if ((ls.getAverageSpeed() == 0) || (ls.getAverageDistance() == 0))
			return FlightScore.INCOMPLETE;
		
		double rwyPct = ls.getAverageDistance() / ls.getDistanceStdDeviation();
		return score((int) ls.getAverageSpeed(), rwyPct);
	}
	
	/**
	 * Scores a flight.
	 * @param fr an FDRFlightReport
	 * @param rD the departure Runway
	 * @param rA the arrival Runway
	 * @return a FlightScore
	 */
	public static FlightScore score(FDRFlightReport fr, Runway rD, Runway rA) {
		if (!(rA instanceof RunwayDistance))
			return FlightScore.INCOMPLETE;
		
		// If we use too much of the takeoff runway, uh oh
		FlightScore score = FlightScore.ACCEPTABLE;
		if (rD instanceof RunwayDistance) {
			RunwayDistance dd = (RunwayDistance) rD;
			if ((dd.getLength() - dd.getDistance()) < 500)
				return FlightScore.DANGEROUS;
			else if (dd.getLength() - dd.getDistance() > 1500)
				score = FlightScore.OPTIMAL;
		}
		
		// Calculate landing data
		RunwayDistance ad = (RunwayDistance) rA;
		double rwyPct = (double)ad.getDistance() / ad.getLength();
		FlightScore ls = score(fr.getLandingVSpeed(), rwyPct);
		return (ls.compareTo(score) < 0) ? score : ls;
	}
}