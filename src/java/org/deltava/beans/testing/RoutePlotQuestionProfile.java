// Copyright 2008, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.*;

/**
 * A bean to store route plotting question data.
 * @author Luke
 * @version 7.0
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

	@Override
	public Airport getAirportA() {
		return _airportA;
	}

	@Override
	public Airport getAirportD() {
		return _airportD;
	}

	@Override
	public GeoLocation getMidPoint() {
		return new GeoPosition(_airportD).midPoint(_airportA);
	}

	@Override
	public int getDistance() {
		return new GeoPosition(_airportD).distanceTo(_airportA);
	}

	@Override
	public void setAirportA(Airport a) {
		_airportA = a;
	}

	@Override
	public void setAirportD(Airport a) {
		_airportD = a;
	}

	@Override
	public String getRowClassName() {
		return getActive() ? "opt1" : "warn";
	}

	@Override
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