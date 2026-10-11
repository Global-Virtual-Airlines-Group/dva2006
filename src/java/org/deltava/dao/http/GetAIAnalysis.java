// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.json.JSONObject;

import org.deltava.beans.acars.RunwayDistance;
import org.deltava.beans.flight.ScorePackage;
import org.deltava.beans.navdata.Runway;

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
	 * Helper method to JSON-format a runway.
	 */
	private static JSONObject format(Runway r) {
		if (r == null) return null;
		JSONObject ro = new JSONObject();
		ro.put("airport", r.getCode());
		ro.put("name", r.getName());
		ro.put("surface", r.getSurface().name());
		ro.put("length", r.getLength());
		ro.put("width", r.getWidth());
		if (r instanceof RunwayDistance rd)
			ro.put("distance", rd.getDistance());
		
		return ro;
	}

	/**
	 * Anlayzes the Flight.
	 * @return
	 * @throws DAOException if an error occurs
	 */
	public String execute() throws DAOException {
		
		// Convert the Package to JSON
		JSONObject data = new JSONObject();
		data.put("flight", JSONFlightExport.format(_pkg.getAircraft(), _pkg.getFlightReport()));
		data.putOpt("departure_runway", format(_pkg.getRunwayD()));
		data.putOpt("arrival_runway", format(_pkg.getRunwayA()));
		_pkg.getData().stream().map(afr -> JSONFlightExport.format(_pkg.getFlightReport().getFDR(), afr)).forEach(po -> data.accumulate("positions", po));
		JSONUtils.ensureArrayPresent(data, "positions");
		
		// TODO: This needs to be data, not code
		// Set the prompt
		addMessage("\"You are a flight data analyst. Analyze the flight data included in this request. Scoring rubric (0-100): Safety = 60 pts, Comfort = 40 pts.\n"
			+ "Deduct for: unstabilized approach, hard landing (>1.8g), overspeed/stall warnings, turbulence, high bank angles.\n"
			+ "Respond with JSON only:\n {{\"analysis\": \"<1-2 paragraphs>\", \"safety_score\": <0-60>,\n\"comfort_score\": <0-40>, \"flight_score\": <0-100>,\n\"key_factors\": [\"...\"]}}");
		addMessage(data.toString());
		
		// Call claude
		JSONObject ro = send();
		return ro.toString(); 
	}
}