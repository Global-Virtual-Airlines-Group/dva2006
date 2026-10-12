// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.json.*;
import org.apache.logging.log4j.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;

import org.deltava.dao.DAOException;

import org.deltava.service.logbook.JSONFlightExport;

import org.deltava.util.*;

/**
 * A Data Access Object to use Claude AI to analyze a flight. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class GetAIAnalysis extends ClaudeDAO {
	
	private static final Logger log = LogManager.getLogger(GetAIAnalysis.class);
	
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
		go.put("lat", g.getLatitude());
		go.put("lng", g.getLongitude());
		return go;
	}
	
	/*
	 * Helper method to JSON-format a Pilot.
	 */
	private static JSONObject format(Pilot p) {
		JSONObject po = new JSONObject();
		po.put("id", p.getID());
		po.put("firstName", p.getFirstName());
		po.put("lastName", p.getLastName());
		po.put("rank", p.getRank().getName());
		po.put("equipmentProgram", p.getEquipmentType());
		po.put("joinedOn", p.getCreatedOn().toEpochMilli());
		p.getRatings().forEach(r -> po.accumulate("ratings", r));
		if (p.getLegs() > 0) {
			po.put("flights", p.getLegs());
			po.put("hours", p.getHours());
			po.put("lastFlight", p.getLastFlight().toEpochMilli());
			po.put("pilotID", p.getPilotCode());
		}
		
		JSONUtils.ensureArrayPresent(po, "ratings");
		return po;
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
		
		// TODO: This needs to be data, not code
		// Set the prompt
		setMaxTokens(6500);
		addSystemPrompt("All bank and pitch angles are in degrees. Speeds are in knots, distances in statute miles, altitudes in feet. Angle of attack on the ground or greater than 50 degrees can be ignored.\n"
				+ "Dates/times are epoch milliseconds, durations are seconds.");
		addSystemPrompt("If the Pilot is new, be more conversational and encouraging. After 150 flights, be more anlaytical.");

		addPrompt(PromptType.USER, "You are a flight data analyst. Analyze some flight data. Scoring rubric (0-100): Safety = 60 pts, Comfort = 40 pts. These may change, so return "
			+ "the maximum score in each category as a field in the JSON response.\n Deduct for: unstabilized approach, hard landing (>1.8g), overspeed/stall warnings, turbulence, high bank angles.\n"
			+ "Respond with JSON only:\n {\"analysis\": \"<1-2 paragraphs>\", \"safety_score\": <0-60>,\n\"comfort_score\": <0-40>, \"flight_score\": <0-100>,\n\"key_factors\": [\"...\"]}");
		addPrompt(PromptType.ASSISTANT, "Please provide the flight data, in JSON format.");
		addPrompt(PromptType.USER, data.toString());
		addPrompt(PromptType.ASSISTANT, "Please provide information about the Pilot, in JSON format.");
		addPrompt(PromptType.USER, format(_pkg.getPilot()).toString());
<<<<<<< HEAD
		addSystemPrompt("All bank and pitch angles are in degrees. Speeds are in knots, distances in statute miles, altitudes in feet. Angle of attack on the ground or greater than 50 degrees can be ignored.\n"
			+ "Dates/times are epoch milliseconds, durations are seconds. There is no need to mention ignored data in the summary.");
		addSystemPrompt("If the Pilot is new, be more conversational and encouraging. After 150 flights, be more anlaytical.");
=======
>>>>>>> branch 'Claude-Analysis' of https://github.com/Global-Virtual-Airlines-Group/dva2006.git
		
		// Call claude API
		try {
			TaskTimer tt = new TaskTimer();
			JSONObject ro = send();
			if (!ro.has("content"))
				throw new IllegalArgumentException("No content in response");
			
			// Check for response
			long ms = tt.stop();
			System.out.println(ro.toString(1));
			JSONArray ca = ro.getJSONArray("content");
			for (int x = 0; x < ca.length(); x++) {
				JSONObject co = ca.getJSONObject(x);
				if ("text".equals(co.optString("type"))) {
					String js = co.getString("text");
					JSONObject ao = new JSONObject(new JSONTokener(js));
					
					// Create the response object
					FlightAnalysis fa = new FlightAnalysis(_pkg.getID(), ao.getString("analysis"));
					fa.setGenerationTime((int) ms);
					fa.setMaxmimums(ao.optInt("safety_max", 60), ao.optInt("comfort_max", 40));
					fa.setScores(ao.optInt("safety_score"), ao.optInt("comfort_score"));
					fa.setTokens(getInputTokens(), getOutputTokens());
					JSONArray kfa = ao.getJSONArray("key_factors");
					for (int y = 0; y < kfa.length(); y++)
						fa.addFactor(kfa.getString(y));
					
					return fa;
				}
			}
			
			throw new IllegalArgumentException("No text in response content");
		} catch (IllegalArgumentException iae) {
			log.warn("Response Error - {}", iae.getMessage());
			return new FlightAnalysis(_pkg.getID(), String.format("Response Error - %s", iae.getMessage()));
		} catch (ClaudeException ce) {
			log.warn("Claude API Error - {}", ce.getMessage());
			return new FlightAnalysis(_pkg.getID(), String.format("Claude API Error - %s", ce.getMessage()));
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}