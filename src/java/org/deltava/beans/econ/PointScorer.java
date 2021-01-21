// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.Helper;
import org.deltava.beans.flight.*;

/**
 * An interface for classes that calculate elite level point scores to Flight Reports.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

@Helper(FlightEliteScore.class)
public abstract class PointScorer {
	
	private final Map<String, Instant> _firstEQ = new HashMap<String, Instant>();
	private final Map<String, Instant> _firstAP = new HashMap<String, Instant>();
	private final Map<String, Instant> _firstCountry = new HashMap<String, Instant>();
	
	private FlightEliteScore _score;
	
	/**
	 * Creates a PointScorer implementation.
	 * @param className the class name
	 * @return a PointScorer impl
	 */
	public static PointScorer init(String className) {
		try {
			Class<?> c = Class.forName(className);
			return (PointScorer) c.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Adds a Flight Report to the Pilot's flight history for first equipment/airport types.
	 * @param fr the FlightReport
	 */
	public void add(FlightReport fr) {
		_firstEQ.putIfAbsent(fr.getEquipmentType(), fr.getDate());
		_firstAP.putIfAbsent(fr.getAirportD().getIATA(), fr.getDate());
		_firstAP.putIfAbsent(fr.getAirportA().getIATA(), fr.getDate());
		_firstCountry.putIfAbsent(fr.getAirportD().getCountry().getCode(), fr.getDate());
		_firstCountry.putIfAbsent(fr.getAirportA().getCountry().getCode(), fr.getDate());
	}

	/**
	 * Scores a Flight Report. 
	 * @param pkg the ScorePackage
	 * @param lvl the Pilot's current EliteLevel
	 * @return the number of status points earned
	 */
	public abstract FlightEliteScore score(ScorePackage pkg, EliteLevel lvl);
	
	/**
	 * Scores a non-ACARS Flight Report. 
	 * @param fr the FlightReport
	 * @param lvl the Pilot's current EliteLevel
	 * @return the number of status points earned
	 */
	public FlightEliteScore score(FlightReport fr, EliteLevel lvl) {
		reset(fr.getID(), lvl);
		if (!canScore(fr))
			return null;
		
		_score.setDistance(fr.getDistance());
		_score.setAuthorID(fr.getAuthorID());
		
		scoreIf(Math.max(100, fr.getDistance() / 4), "Base Miles", true, false);
		scoreIf(50, "Promotion Leg", !fr.getCaptEQType().isEmpty());
		scoreIf(500, "New Aircraft - " + fr.getEquipmentType(), isNewEquipment(fr.getEquipmentType(), fr.getDate()));
		scoreIf(200, "New Airport - " + fr.getAirportD().getIATA(), isNewAirport(fr.getAirportD().getIATA(), fr.getDate()));
		scoreIf(200, "New Airport - " + fr.getAirportA().getIATA(), isNewAirport(fr.getAirportA().getIATA(), fr.getDate()));
		scoreIf(25, "Online Event", (fr.getDatabaseID(DatabaseID.EVENT) > 0));
		scoreIf(Math.round(_score.getPoints() * lvl.getBonusFactor()), lvl.getName() + " Supplement", lvl.getBonusFactor() > 0f);
		return _score;
	}

	/**
	 * Returns the score bundle.
	 * @return a FlightEliteScore, or null
	 */
	public FlightEliteScore getScore() {
		return _score;
	}
	
	/**
	 * Adds a conditional bonus entry to the flight score.
	 * @param pts the number of points
	 * @param msg the entry message
	 * @param condition the condition
	 */
	protected void scoreIf(int pts, String msg, boolean condition) {
		scoreIf(pts, msg, condition, true);
	}
	
	/**
	 * Adds a conditional entry to the flight score.
	 * @param pts the number of points
	 * @param msg the entry message
	 * @param condition the condition
	 * @param isBonus TRUE if a bonus entry, otherwise FALSE
	 */
	protected void scoreIf(int pts, String msg, boolean condition, boolean isBonus) {
		if ((condition) && (pts > 0))
			_score.add(pts, msg, isBonus);
	}
	
	/**
	 * Resets the flight score. The list of previous Airports, Countries and equipment types are unchanged.
	 * @param id the Flight Report database ID
	 * @param lvl the Pilot's current EliteLevel
	 */
	protected void reset(int id, EliteLevel lvl) {
		_score = new FlightEliteScore(id);
		_score.setEliteLevel(lvl.getName(), lvl.getYear());
	}
	
	/**
	 * Checks whether a flight report can be scored.
	 * @param fr the FlightReport
	 * @return TRUE if the flight report is non-null and neither draft nor rejected, otherwise FALSE
	 */
	protected static boolean canScore(FlightReport fr) {
		return ((fr != null) && (fr.getStatus() != FlightStatus.DRAFT) && (fr.getStatus() != FlightStatus.REJECTED));
	}

	/**
	 * Returns if an equipment type has been used before a particular date.
	 * @param eqType the equipment type
	 * @param dt the date/time
	 * @return TRUE if the equipment type has not been used before this date, otherwise FALSE
	 */
	protected boolean isNewEquipment(String eqType, Instant dt) {
		Instant fdt = _firstEQ.get(eqType);
		return ((fdt == null) || dt.isBefore(fdt));
	}
	
	/**
	 * Returns if an Airport has been visited before a particular date.
	 * @param iata the Airport's IATA code
	 * @param dt the date/time
	 * @return TRUE if the Airport has not been visited before this date, otherwise FALSE
	 */
	protected boolean isNewAirport(String iata, Instant dt) {
		Instant fdt = _firstAP.get(iata);
		return ((fdt == null) || dt.isBefore(fdt));
	}
	
	/**
	 * Returns if a Country has been visited before a particular date.
	 * @param code the ISO-3166 country code
	 * @param dt the date/time
	 * @return TRUE if the Country has not been visited before this date, otherwise FALSE
	 */
	protected boolean isNewCountry(String code, Instant dt) {
		Instant fdt = _firstCountry.get(code);
		return ((fdt == null) || dt.isBefore(fdt));
	}
}