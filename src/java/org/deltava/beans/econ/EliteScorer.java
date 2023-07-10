// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoField;

import org.deltava.beans.acars.RunwayDistance;
import org.deltava.beans.flight.*;

import org.deltava.util.MutableInteger;
import org.deltava.util.system.SystemData;

/**
 * A flight scorer for Delta Virtual Airlines. This extends the default implementation by restricting flights to a maximum number of non-ACARS flights per month. 
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

public class EliteScorer extends PointScorer {
	
	private static final int MAX_NON_ACARS = SystemData.getInt("econ.elite.maxNonACARS", 5);
	
	private final Map<Integer, MutableInteger> _nonACARSCounts = new HashMap<Integer, MutableInteger>();
	
	private static Integer getNonACARSKey(Instant dt) {
		return Integer.valueOf(dt.get(ChronoField.MONTH_OF_YEAR) * 100 + dt.get(ChronoField.DAY_OF_MONTH));
	}
	
	/**
	 * Adds a Flight Report. This will calculate number of non-ACARS flights per month.
	 */
	@Override
	public void add(FlightReport fr) {
		super.add(fr);
		Integer k = getNonACARSKey(fr.getDate());
		MutableInteger i = _nonACARSCounts.getOrDefault(k, new MutableInteger(0));
		i.inc();
		_nonACARSCounts.put(k, i);
	}
	
	/**
	 * Scores an ACARS Flight Report.
	 * @param pkg the ScorePackage
	 * @param lvl the Pilot's Elite status level
	 * @return a FlightScoreEntry
	 */
	@Override
	public FlightEliteScore score(ScorePackage pkg, EliteLevel lvl) {
		FDRFlightReport ffr = pkg.getFlightReport();

		// Calculate base miles, break out if needed
		if (score(ffr, lvl) == null) return null;
		
		// Calculate landing score bonus
		if (pkg.getRunwayA() instanceof RunwayDistance ra) {
			double ls = LandingScorer.score(ffr.getLandingVSpeed(), ra.getDistance());
			LandingRating lr = LandingRating.rate((int)ls);
			addBonus(20, "Acceptable Landing", (lr == LandingRating.ACCEPTABLE));
			addBonus(50, "Good Landing", (lr == LandingRating.GOOD));
		}
		
		// Calculate no acceleration bonus
		
		return _score;
	}

	/**
	 * Scores a Flight Report.
	 * @param fr the FlightReport
	 * @param lvl the Pilot's Elite status level
	 * @return a FlightScoreEntry
	 */
	@Override
	public FlightEliteScore score(FlightReport fr, EliteLevel lvl) {
		if (!canScore(fr)) return null;
		reset(fr.getID(), lvl);

		// Set base information
		_score.setDistance(fr.getDistance());
		_score.setAuthorID(fr.getAuthorID());

		// Check for non-ACARS flights this month
		if (!fr.hasAttribute(FlightReport.ATTR_ACARS)) {
			int nonACARSCount = _nonACARSCounts.getOrDefault(getNonACARSKey(fr.getDate()), new MutableInteger(0)).getValue().intValue();
			if (nonACARSCount > MAX_NON_ACARS) {
				setBase(Math.max(500, fr.getDistance() / 5), "Non-ACARS Base Miles");
				return _score;
			}
		}
		
		setBase(Math.max(500, fr.getDistance() / 4), "ACARS/XACARS/simFDR Base Miles");
		addBonus(50, "Promotion Leg", !fr.getCaptEQType().isEmpty());
		addBonus(500, "New Aircraft - " + fr.getEquipmentType(), isNewEquipment(fr.getEquipmentType(), fr.getDate()));
		addBonus(200, "New Airport - " + fr.getAirportD().getIATA(), isNewAirport(fr.getAirportD().getIATA(), fr.getDate()));
		addBonus(200, "New Airport - " + fr.getAirportA().getIATA(), isNewAirport(fr.getAirportA().getIATA(), fr.getDate()));
		addBonus(25, fr.getNetwork() + " Online Flight", (fr.getNetwork() != null));
		addBonus(50, "Online Event", (fr.getDatabaseID(DatabaseID.EVENT) > 0));
		addBonus(Math.round(_score.getPoints() * lvl.getBonusFactor()), lvl.getName() + " Supplement", lvl.getBonusFactor() > 0f);
		return _score;
	}
}