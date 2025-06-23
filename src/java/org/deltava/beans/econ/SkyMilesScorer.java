// Copyright 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoField;

import org.deltava.beans.acars.RunwayDistance;
import org.deltava.beans.flight.*;

import org.deltava.util.MutableInteger;
import org.deltava.util.system.SystemData;

/**
 * A flight scorer for Delta Virtual Airlines. This extends the default implementation by restricting flights to a maximum number of non-ACARS flights per month. 
 * @author Luke
 * @version 12.0
 * @since 11.0
 */

public class SkyMilesScorer extends EliteScorer {
	
	private static final int MAX_NON_ACARS = SystemData.getInt("econ.elite.maxNonACARS", 5);
	private static final int MAX_ACCEL_PCT = SystemData.getInt("econ.elite.maxAccelPct", 20);
	
	private static final int MIN_DISTANCE = 500;
	
	private final Map<Integer, MutableInteger> _nonACARSCounts = new HashMap<Integer, MutableInteger>();
	
	/*
	 * Helper method to generate the key value used to lookup the number of non-ACARS flights in a month.
	 */
	private static Integer getNonACARSKey(Instant dt) {
		LocalDate ld = LocalDate.ofInstant(dt, ZoneOffset.UTC);
		return Integer.valueOf(ld.get(ChronoField.YEAR) * 100 + ld.get(ChronoField.MONTH_OF_YEAR));
	}
	
	/**
	 * Adds a Flight Report. This will calculate number of non-ACARS flights per month.
	 */
	@Override
	public void add(FlightReport fr) {
		super.add(fr);
		if (fr.hasAttribute(FlightReport.ATTR_ACARS)) return;
		Integer k = getNonACARSKey(fr.getDate());
		MutableInteger i = _nonACARSCounts.getOrDefault(k, new MutableInteger(0));
		i.inc();
		_nonACARSCounts.put(k, i);
	}
	
	@Override
	public FlightEliteScore score(ScorePackage pkg, EliteLevel lvl) {
		FDRFlightReport ffr = pkg.getFlightReport();

		// Calculate base miles, break out if needed
		if (score(ffr, lvl) == null) return null;
		
		// Calculate landing score bonus
		if (pkg.getRunwayA() instanceof RunwayDistance ra) {
			double ls = LandingScorer.score(ffr.getLandingVSpeed(), ra.getDistance());
			LandingRating lr = LandingRating.rate((int)ls);
			addBonus(Math.max(125, Math.round(ffr.getDistance() * 0.15f)), "Acceptable Landing", (lr == LandingRating.ACCEPTABLE));
			addBonus(Math.max(350, Math.round(ffr.getDistance() * 0.375f)), "Good Landing", (lr == LandingRating.GOOD));
		}
		
		// Calculate minimal acceleration bonus
		if (ffr instanceof ACARSFlightReport afr) {
			int accTime = afr.getTime(2) + afr.getTime(4); long totalTime = afr.getBlockTime().toSeconds();
			long accPct = (totalTime == 0) ? 0 : accTime * 100 / totalTime;
			if (accPct < MAX_ACCEL_PCT) {
				addBonus(Math.max(MIN_DISTANCE, ffr.getDistance() / 2), String.format("Minimal Time Acceleration - %d%%", Long.valueOf(accPct)), true);
				_score.setDistance(Math.max(MIN_DISTANCE, ffr.getDistance()));
			} else if (_score.getYear() <= 2023)
				_score.setDistance(Math.max(MIN_DISTANCE, ffr.getDistance()));
			else {
				ffr.addStatusUpdate(0, HistoryType.ELITE, String.format("Time Acceleration %d%%, limiting Distance credit to %d%%", Long.valueOf(accPct), Long.valueOf(100 - accPct)));
				_score.setDistance(Math.max(MIN_DISTANCE, Math.round(ffr.getDistance() * (1f - (accPct / 100f)))));
			}
			
			// Calculate on-time and Gate bonuss
			addBonus(Math.round(ffr.getDistance() * 0.4f), "Early Arrival", (afr.getOnTime() == OnTime.EARLY));
			addBonus(Math.round(ffr.getDistance() * 0.15f), "On Time Arrival", (afr.getOnTime() == OnTime.ONTIME));
			addBonus(350, "Gate to Gate", (pkg.getGateD() != null) && (pkg.getGateA() != null));
		}
		
		return _score;
	}

	@Override
	public FlightEliteScore score(FlightReport fr, EliteLevel lvl) {
		if (!canScore(fr)) return null;
		reset(fr.getID(), lvl);
		_score.setAuthorID(fr.getAuthorID());

		// Check for non-ACARS flights this month
		boolean isACARS = fr.hasAttribute(FlightReport.ATTR_ACARS); 
		if (!isACARS) {
			int cnt = _nonACARSCounts.getOrDefault(getNonACARSKey(fr.getDate()), new MutableInteger(0)).intValue();
			if (cnt >= MAX_NON_ACARS) {
				setBase(fr.getDistance() / 2, String.format("Non-ACARS (%d) Base Miles", Integer.valueOf(cnt)));
				_score.setDistance(Math.max(MIN_DISTANCE, fr.getDistance()));
				_score.setScoreOnly(true);
				return _score;
			}
		}
		
		_score.setDistance(Math.max(MIN_DISTANCE, isACARS ? fr.getDistance() : (fr.getDistance() / 2))); // This is for pending reports - if scored as part of a package it will get overwritten
		setBase(fr.getDistance(), isACARS ? "ACARS/XACARS/simFDR Base Miles" : "Base Miles");
		addBonus(250, "Promotion Leg", !fr.getCaptEQType().isEmpty());
		addBonus(500, "New Aircraft - " + fr.getEquipmentType(), isNewEquipment(fr.getEquipmentType(), fr.getDate()));
		addBonus(200, "New Airport - " + fr.getAirportD().getIATA(), isNewAirport(fr.getAirportD().getIATA(), fr.getDate()));
		addBonus(200, "New Airport - " + fr.getAirportA().getIATA(), isNewAirport(fr.getAirportA().getIATA(), fr.getDate()));
		addBonus(100, fr.getNetwork() + " Online Flight", (fr.getNetwork() != null));
		addBonus(250, "Online Event", (fr.getDatabaseID(DatabaseID.EVENT) > 0));
		addBonus(200, "Flight Tour", (fr.getDatabaseID(DatabaseID.TOUR) > 0));
		addBonus(250, "Dispatcher Bonus", fr.hasAttribute(FlightReport.ATTR_DISPATCH));
		addBonus(250, "SimBrief Usage", fr.hasAttribute(FlightReport.ATTR_SIMBRIEF));
		addBonus(Math.round(_score.getPoints() * lvl.getBonusFactor()), lvl.getName() + " Supplement", lvl.getBonusFactor() > 0f);
		return _score;
	}
}