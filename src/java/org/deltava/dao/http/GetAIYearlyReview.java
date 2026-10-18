// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;

import org.apache.logging.log4j.*;
import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.YearlySummary;
import org.deltava.beans.testing.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;

/**
 * A Data Access Object to retrieve an AI-generated yearly summary. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class GetAIYearlyReview extends ClaudeDAO {
	
	private static final Logger log = LogManager.getLogger(GetAIYearlyReview.class);
	
	// JSON Array legend
	private static final Collection<String> LEGEND = Arrays.asList("date", "departure_airport", "arrival_airport", "equipment_type", "airline");
	
	private final Pilot _p;
	private final YearlySummary _cy;
	private final Collection<Test> _exams = new ArrayList<Test>();

	/**
	 * Creates the Data Access Object.
	 * @param p the Pilot
	 * @param cy a YearlySummary
	 */
	public GetAIYearlyReview(Pilot p, YearlySummary cy) {
		super();
		_p = p;
		_cy = cy;
	}
	
	public void addExams(Collection<Test> exams) {
		_exams.addAll(exams);
	}
	
	/*
	 * Helper method to format Flights as a JSON array.
	 */
	private static JSONArray format(FlightReport fr) {
		JSONArray fa = new JSONArray();
		fa.put(StringUtils.format(fr.getDate(), "MM/dd/yyyy"));
		fa.put(fr.getAirportD().getICAO());
		fa.put(fr.getAirportA().getICAO());
		fa.put(fr.getEquipmentType());
		fa.put(fr.getAirline().getCode());
		return fa;
	}
	
	/*
	 * Helper method to format an Exam/Check Ride into a JSON object.
	 */
	private static JSONObject format(Test t) {
		JSONObject jo = new JSONObject();
		jo.put("type", t.getClass().getSimpleName());
		jo.put("date", StringUtils.format(t.getDate(), "MM/dd/yyyy"));
		jo.put("name", t.getName());
		jo.put("result", t.getPassFail() ? "PASS" : "FAIL");
		jo.put("stage", t.getStage());
		if (t instanceof Examination) {
			jo.put("questions", t.getSize());
			jo.put("score", t.getScore());
		} else if (t instanceof CheckRide cr) {
			jo.put("comments", cr.getComments());
			jo.put("checkride_type", cr.getType().getName());
		}
		
		return jo;
	}

	/**
	 * 
	 * @return an AISummary
	 * @throws DAOException if an error occurs
	 */
	public AISummary execute() throws DAOException {
		
		// Convert the data to JSON
		TaskTimer tt = new TaskTimer();
		JSONObject data = new JSONObject();
		data.put("year", _cy.getYear());
		data.put("flight_count", _cy.size());
		data.put("total_distance", _cy.getDistance());
		_cy.getNewAirports().stream().forEach(a -> data.accumulate("new_airports", a.getICAO()));
		_cy.getNewEquipment().stream().forEach(eq -> data.accumulate("new_aircraft", eq));
		
		// Format flight data
		JSONArray la = new JSONArray();
		LEGEND.forEach(la::put);
		data.accumulate("flights", la);
		_cy.getFlights().stream().map(GetAIYearlyReview::format).forEach(fa -> data.accumulate("flights", fa));
		JSONUtils.ensureArrayPresent(data, "flights", "new_airports", "new_aircraft");
		
		// Format airports
		JSONArray aa = new JSONArray();
		_cy.getAirports().stream().map(JSONUtils::format).forEach(aa::put);
		
		// Format Examiantions / Check Rides
		JSONArray ea = new JSONArray();
		_exams.stream().map(GetAIYearlyReview::format).forEach(ea::put);
		
		// Set the prompt
		setMaxTokens(7500);
		addPrompt(PromptType.SYSTEM, "All airport codes are in ICAO format. All distances are Statute Miles.");
		addPrompt(PromptType.SYSTEM, "Flight data is an array of JSON arrays. The first record in this array is the field names and is not valid data.");
		addPrompt(PromptType.USER, "Please provide a 2 to 3 paragraph summary of this list of flights and testing results for a conversational \"Year in Review\" product. Pay close attention to new Airports, "
			+ "Aircraft Types, Ratings, Check Rides and Examinations passed.\nFeel free to add conversational items about the airports visited or aircraft used.");
		addPrompt(PromptType.ASSISTANT, "Please provide the year's flight data, in JSON format.");
		addPrompt(PromptType.USER, data.toString());
		addPrompt(PromptType.ASSISTANT, "Please provide information about the Airports, in JSON format.");
		addPrompt(PromptType.USER, aa.toString());
		addPrompt(PromptType.ASSISTANT, "Please provide information about any passed Check Rides or Examinations, in JSON format.");
		addPrompt(PromptType.USER, ea.toString());
		addPrompt(PromptType.ASSISTANT, "Please provide information about the Pilot, in JSON format.");
		addPrompt(PromptType.USER, format(_p).toString()); 
	
		// Call claude API
		try {
			JSONObject ro = send();
			
			// Create response
			String txt = getResponseText(ro);
			AISummary s = new AISummary(_p.getID(), txt);
			s.setGenerationTime(tt.stop());
			s.setTokens(getInputTokens(), getOutputTokens());			
			return s;
		} catch (IllegalArgumentException iae) {
			log.warn("Response Error - {}", iae.getMessage());
			return new AISummary(_p.getID(), iae.getMessage());
		} catch (ClaudeException ce) {
			log.warn("Claude API Error - {}", ce.getMessage());
			return new AISummary(_p.getID(), String.format("Claude API Error - %s", ce.getMessage())) {{ setGenerationTime(tt.stop()); setTokens(getInputTokens(), getOutputTokens()); }};
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}