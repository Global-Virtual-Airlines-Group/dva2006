// Copyright 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.Helper;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.Runway;
import org.deltava.beans.stats.LandingStatistics;

/**
 * A utility class to grade flights.
 * @author Luke
 * @version 8.0
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

	/**
	 * Scores a Check Ride. This will review flight parameters to ensure that 
	 * @param pkg a ScorePackage
	 * @return a FlightScore
	 */
	public static FlightScore score(ScorePackage pkg) {
		
		FlightScore fs = score(pkg.getFlightReport(), pkg.getRunwayD(), pkg.getRunwayA());
		FDRFlightReport fr = pkg.getFlightReport();
		if (fr.getTakeoffWeight() > pkg.getAircraft().getMaxTakeoffWeight())
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
		else if (fr.getLandingWeight() > pkg.getAircraft().getMaxLandingWeight())
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
		else if (fr.getGateFuel() < pkg.getAircraft().getBaseFuel())
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
		else if (pkg.getRunwayD().getLength() < pkg.getAircraft().getTakeoffRunwayLength())
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
		else if (pkg.getRunwayA().getLength() < pkg.getAircraft().getLandingRunwayLength())
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
		
		FlightScore es = FlightScore.OPTIMAL;
		for (ACARSRouteEntry re : pkg.getData()) {
			for (Warning w : re.getWarnings())
				es = FlightScore.max(es, w.getScore());
		}
		
		return FlightScore.max(fs, es);
	}
}