// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.time.Instant;
import java.util.*;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store AI-generated Flight Analysis data.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class FlightAnalysis extends DatabaseBean {
	
	private final String _msg;
	private Instant _createdOn = Instant.now();
	
	private int _safetyScore;
	private int _comfortScore;
	
	private final Collection<String> _factors = new ArrayList<String>();

	/**
	 * Creates the bean.
	 * @param id the Flight Report database ID
	 * @param msg the flight analysis text
	 */
	public FlightAnalysis(int id, String msg) {
		super();
		setID(id);
		_msg = msg;
	}
	
	/**
	 * Returns the creation date of this Flight Analysis.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the safety score.
	 * @return the score
	 */
	public int getSafetyScore() {
		return _safetyScore;
	}
	
	/**
	 * Returns the comfort score.
	 * @return the score
	 */
	public int getComfortScore() {
		return _comfortScore;
	}
	
	/**
	 * Returns the total flight score.
	 * @return the total score
	 */
	public int getScore() {
		return Math.min(100, _safetyScore + _comfortScore);
	}
	
	/**
	 * Returns the flight analysis text.
	 * @return the text
	 */
	public String getMessage() {
		return _msg;
	}
	
	/**
	 * Returns the flight analysis factor messages.
	 * @return a Colleciton of factor messages
	 */
	public Collection<String> getFactors() {
		return _factors;
	}

	/**
	 * Adds a flight analysis factor.
	 * @param msg the factor text
	 */
	public void addFactor(String msg) {
		_factors.add(msg);
	}
	
	/**
	 * Updates the creation date of this flight analysis.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}

	/**
	 * Update the flight scores. 
	 * @param safetyScore the safety score, from 0 to 100
	 * @param comfortScore the comfort score, from 0 to 100
	 */
	public void setScores(int safetyScore, int comfortScore) {
		_safetyScore = Math.max(0, safetyScore);
		_comfortScore = Math.max(0, comfortScore);
	}
}