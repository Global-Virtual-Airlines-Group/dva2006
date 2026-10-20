// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean to store AI-generated Flight Analysis data.
 * @author Luke
 * @version 13.0
 * @since 12.5
 */

public class FlightAnalysis extends AIResponse {
	
	private final String _msg;
	
	private int _safety;
	private int _maxSafety = 100;
	private int _comfort;
	private int _maxComfort = 100;
	
	private final Collection<String> _factors = new ArrayList<String>();
	
	/**
	 * Creates the bean.
	 * @param id the Flight Report database ID
	 * @param msg the flight analysis text
	 */
	public FlightAnalysis(int id, String msg) {
		super(id);
		_msg = msg;
	}
	
	/**
	 * Returns the safety score.
	 * @return the score
	 */
	public int getSafety() {
		return _safety;
	}
	
	/**
	 * Returns the comfort score.
	 * @return the score
	 */
	public int getComfort() {
		return _comfort;
	}
	
	/**
	 * Returns the maximum possible safety score.
	 * @return the maximum score
	 */
	public int getMaxSafety() {
		return _maxSafety;
	}
	
	/**
	 * Returns the maximum possible comfort score.
	 * @return the maximum score
	 */
	public int getMaxComfort() {
		return _maxComfort;
	}
	
	/**
	 * Returns the total flight score.
	 * @return the total score
	 */
	public int getScore() {
		return Math.min(100, _safety + _comfort);
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
	 * Updates the flight scores. These will be trimmed based on the maximum score in each criteria, so call {@Link FlightAnalysis#setMaxmimums(int, int)} first!
	 * @param safety the safety score
	 * @param comfort the comfort score
	 */
	public void setScores(int safety, int comfort) {
		_safety = Math.min(_maxSafety, Math.max(0, safety));
		_comfort= Math.min(_maxComfort, Math.max(0, comfort));
	}
	
	/**
	 * Updates the maximum flight scores. 
	 * @param safety the maximum safety score
	 * @param comfort the maximum comfort score
	 */
	public void setMaxmimums(int safety, int comfort) {
		_maxSafety = Math.max(0, safety);
		_maxComfort = Math.max(0, comfort);
	}
}