// Copyright 2012, 2016, 2017, 2018, 2019, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.Helper;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.Runway;
import org.deltava.beans.stats.LandingStatistics;

import org.deltava.util.*;

/**
 * A utility class to grade flights.
 * @author Luke
 * @version 11.4
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
	private static Tuple<FlightScore, String> score(int fpm, double rwyPct) {
		if (fpm < -600)
			return Tuple.create(FlightScore.DANGEROUS, String.format("Excessive sink rate - %d feet/min", Integer.valueOf(fpm)));
		else if (rwyPct > 0.45)
			return Tuple.create(FlightScore.DANGEROUS, String.format("Excessive touchdown runway usage - %.1f%%", Double.valueOf(rwyPct * 100)));
		else if ((fpm < -375) || (fpm > -125) || (rwyPct > 0.35) || (rwyPct < 0.075))
			return Tuple.create(FlightScore.ACCEPTABLE, String.format("Sink rate %d feet/min, Runway usage %.1f%%", Integer.valueOf(fpm), Double.valueOf(rwyPct * 100)));

		return Tuple.create(FlightScore.OPTIMAL, null);
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
		return score((int) ls.getAverageSpeed(), rwyPct).getLeft();
	}
	
	/*
	 * Scores a flight based on runway data.
	 */
	private static FlightScore scoreRunways(ScorePackage pkg) {

		Runway rA = pkg.getRunwayA();
		if (!(rA instanceof RunwayDistance ad))
			return FlightScore.INCOMPLETE;
		
		// If we use too much of the takeoff runway, uh oh
		FlightScore score = FlightScore.ACCEPTABLE;
		Runway rD = pkg.getRunwayD();
		if (rD instanceof RunwayDistance dd) {
			int rwyRemaining = dd.getLength() - dd.getDistance();
			if (rwyRemaining < 500) {
				pkg.add(String.format("Insufficient Takeoff Runway length remaining - %d feet", Integer.valueOf(rwyRemaining)));
				return FlightScore.DANGEROUS;
			} else if (rwyRemaining > 1500)
				score = FlightScore.OPTIMAL;
			else
				pkg.add(String.format("Insufficient Takeoff Runway length remaining - %d feet", Integer.valueOf(rwyRemaining)));
		}
		
		// Calculate landing data
		double rwyPct = (double)ad.getDistance() / ad.getLength();
		Tuple<FlightScore, String> ls = score(pkg.getFlightReport().getLandingVSpeed(), rwyPct);
		if (ls.getRight() != null)
			pkg.add(ls.getRight());
		
		return (ls.getLeft().compareTo(score) < 0) ? score : ls.getLeft();
	}

	/**
	 * Scores a Check Ride. This will review flight parameters to ensure that 
	 * @param pkg a ScorePackage
	 * @return a FlightScore
	 */
	public static FlightScore score(ScorePackage pkg) {
		
		FlightScore fs = scoreRunways(pkg);
		FDRFlightReport fr = pkg.getFlightReport();
		int endFuel = (fr.getGateFuel() > 0) ? fr.getGateFuel() : fr.getLandingFuel();
		if (fr.getTakeoffWeight() > pkg.getAircraft().getMaxTakeoffWeight()) {
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
			pkg.add("Takeoff weight excteeds MTOW");
		} else if (fr.getLandingWeight() > pkg.getAircraft().getMaxLandingWeight()) {
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
			pkg.add("Landing weight exceeds MLW");
		} else if (endFuel < pkg.getAircraft().getBaseFuel()) {
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
			pkg.add(String.format("Insufficient fuel at Gate - %d lbs", Integer.valueOf(endFuel)));
		} else if ((pkg.getRunwayD() != null) && (pkg.getRunwayD().getLength() < pkg.getOptions().getTakeoffRunwayLength())) {
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
			pkg.add("Insufficient Takeoff Runway length");
		} else if ((pkg.getRunwayA() != null) && (pkg.getRunwayA().getLength() < pkg.getOptions().getLandingRunwayLength())) {
			fs = FlightScore.max(fs, FlightScore.DANGEROUS);
			pkg.add("Insufficient Landing Runway length");
		}
		
		// Calculate warning count
		FlightScore es = FlightScore.OPTIMAL;
		Map<Warning, MutableInteger> warnCount = new TreeMap<Warning, MutableInteger>();
		for (ACARSRouteEntry re : pkg.getData()) {
			for (Warning w : re.getWarnings()) {
				es = FlightScore.max(es, w.getScore());
				MutableInteger cnt = warnCount.getOrDefault(w, new MutableInteger(0));
				cnt.inc();
				warnCount.put(w, cnt);
			}
		}
		
		// Add to messages
		for (Map.Entry<Warning, MutableInteger> me : warnCount.entrySet())
			pkg.add(String.format("Flight Data Warning - %s (x%s)", me.getKey().getDescription(), me.getValue()));
		
		pkg.setResult(FlightScore.max(fs, es));
		return pkg.getResult();
	}
}