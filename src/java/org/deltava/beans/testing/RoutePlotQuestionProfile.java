// Copyright 2008, 2009, 2016, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.deltava.beans.schedule.*;

/**
 * A bean to store route plotting question data.
 * @author Luke
 * @version 12.0
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