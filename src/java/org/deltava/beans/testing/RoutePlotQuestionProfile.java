// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

/**
 * A bean to store route plotting question data.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class RoutePlotQuestionProfile extends MultiChoiceQuestionProfile implements RoutePlot {
	
	private Airport _airportD;
	private Airport _airportA;

	/**
	 * Creates a new route plotting question profile.
	 * @param text the Question text
	 * @throws NullPointerException if text is null
	 */
	public RoutePlotQuestionProfile(String text) {
		super(text);
	}

	/**
	 * Returns the arrival airport.
	 */
	public Airport getAirportA() {
		return _airportA;
	}

	/**
	 * Returns the departure airport.
	 */
	public Airport getAirportD() {
		return _airportD;
	}
	
	/**
	 * Returns the midpoint between the airports.
	 */
	public GeoLocation getMidPoint() {
		return new GeoPosition(_airportD).midPoint(_airportA);
	}
	
	/**
	 * Returns the distance between the airports.
	 */
	public int getDistance() {
		return new GeoPosition(_airportD).distanceTo(_airportA);
	}

	/**
	 * Updates the arrival airport.
	 */
	public void setAirportA(Airport a) {
		_airportA = a;
	}

	/**
	 * Updates the departure airport.
	 */
	public void setAirportD(Airport a) {
		_airportD = a;
	}
	
	/**
	 * Returns the CSS row class name if included in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return getActive() ? "opt1" : "warn";
	}
	
	/**
	 * Converts this profile into a {@link RoutePlotQuestion} bean.
	 */
	public Question toQuestion() {
		RoutePlotQuestion q = new RoutePlotQuestion(getQuestion());
		q.setID(getID());
		q.setCorrectAnswer(getCorrectAnswer());
		q.setAirportD(_airportD);
		q.setAirportA(_airportA);
		List<String> rndChoices = new ArrayList<String>(getChoices());
		Collections.shuffle(rndChoices);
		for (String c : rndChoices)
			q.addChoice(c);
		
		return q;
	}
}