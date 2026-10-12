// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.json.*;

import org.deltava.beans.ai.FlightAnalysis;
import org.deltava.beans.flight.ScorePackage;
import org.deltava.beans.navdata.*;

import org.deltava.dao.DAOException;

import org.deltava.service.logbook.JSONFlightExport;

import org.deltava.util.JSONUtils;

/**
 * A Data Access Object to use Claude AI to analyze a flight. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class GetAIAnalysis extends ClaudeDAO {
	
	private final ScorePackage _pkg;

	/**
	 * Creates the DAO.
	 * @param pkg the ScorePackage to analyze 
	 */
	public GetAIAnalysis(ScorePackage pkg) {
		super();
		_pkg = pkg;
	}
	
	/*
	 * Helper method to JSON-format a Gate.
	 */
	private static JSONObject format(Gate g) {
		if (g == null) return null;
		JSONObject go = new JSONObject();
		go.put("airport", g.getCode());
		go.put("name", g.getName());
		go.put("zone", g.getZone().getDescription());
		go.put("location", JSONUtils.format(g));
		return go;
	}

	/**
	 * Anlayzes the Flight.
	 * @return a FlightAnalysis bean
	 * @throws DAOException if an error occurs
	 */
	public FlightAnalysis execute() throws DAOException {
		
		// Convert the Package to JSON
		JSONObject data = new JSONObject();
		data.put("flight", JSONFlightExport.format(_pkg.getAircraft(), _pkg.getFlightReport()));
		data.putOpt("departure_runway", JSONUtils.format(_pkg.getRunwayD()));
		data.putOpt("arrival_runway", JSONUtils.format(_pkg.getRunwayA()));
		data.putOpt("departure_gate", format(_pkg.getGateD()));
		data.putOpt("arrival_gate", format(_pkg.getGateA()));
		_pkg.getData().stream().map(afr -> JSONFlightExport.format(_pkg.getFlightReport().getFDR(), afr)).forEach(po -> data.accumulate("positions", po));
		JSONUtils.ensureArrayPresent(data, "positions");
		
		// Format the JSON payload
		StringBuilder buf = new StringBuilder("<flightdata>");
		buf.append(data.toString());
		buf.append("</flightdata>");
		
		// TODO: This needs to be data, not code
		// Set the prompt
		addMessage(MessageType.USER, "You are a flight data analyst. Analyze some flight data. Scoring rubric (0-100): Safety = 60 pts, Comfort = 40 pts. These may change, so return "
			+ "the maximum score in each category as part of the response.\n Deduct for: unstabilized approach, hard landing (>1.8g), overspeed/stall warnings, turbulence, high bank angles.\n"
			+ "Respond with JSON only:\n {\"analysis\": \"<1-2 paragraphs>\", \"safety_score\": <0-60>,\n\"comfort_score\": <0-40>, \"flight_score\": <0-100>,\n\"key_factors\": [\"...\"]}");
		addMessage(MessageType.ASSISTANT, "Please provide the flight data, in JSON format.");
		addMessage(MessageType.USER, buf.toString());
		
		setSystemPrompt("All bank and pitch angles are in degrees. Speeds are in knots, distances in statute miles, altitudes in feet. Angles of attack greater than 50 degrees can be ignored.");
		
		// Call claude
		try {
			JSONObject ro = send();
			if (!ro.has("content"))
				throw new IllegalArgumentException("No content in response");
			
			// Check for response
			JSONArray ca = ro.getJSONArray("content");
			for (int x = 0; x < ca.length(); x++) {
				JSONObject co = ca.getJSONObject(x);
				if ("text".equals(co.optString("type"))) {
					String js = co.getString("text");
					JSONObject ao = new JSONObject(new JSONTokener(js));
					
					// Create the response object
					FlightAnalysis fa = new FlightAnalysis(_pkg.getID(), ao.getString("analysis"));
					fa.setScores(ao.optInt("safety_score"), ao.optInt("comfort_score"));
					JSONArray kfa = ao.getJSONArray("key_factors");
					for (int y = 0; y < kfa.length(); y++)
						fa.addFactor(kfa.getString(y));
					
					return fa;
				}
			}
			
			throw new IllegalArgumentException("No text in response content");
		} catch (IllegalArgumentException iae) {
			return new FlightAnalysis(_pkg.getID(), String.format("Response Error - %s", iae.getMessage()));
		} catch (ClaudeException ce) {
			return new FlightAnalysis(_pkg.getID(), String.format("Claude API Error - %s", ce.getMessage()));
		}
	}
}