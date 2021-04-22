// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.json.*;

import org.deltava.beans.servinfo.RadioPosition;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to load VATSIM radio position data.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class GetVATSIMTransceivers extends DAO {
	
	/**
	 * Creates the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetVATSIMTransceivers(InputStream is) {
		super(is);
	}

	/**
	 * Load the radio positions.
	 * @return a Collection of RadioPosition beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<RadioPosition> load() throws DAOException {
		
		JSONArray ja = null;
		try (InputStream is = getStream()) {
			ja = new JSONArray(new JSONTokener(is));
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		Collection<RadioPosition> results = new ArrayList<RadioPosition>();
		for (int x = 0; x < ja.length(); x++) {
			JSONObject to = ja.getJSONObject(x);
			String cs = to.getString("callsign");
			JSONArray tta = to.getJSONArray("transceivers");
			for (int y = 0; y < tta.length(); y++) {
				JSONObject tlo = tta.getJSONObject(y);
				long freq = tlo.getLong("frequency") / 10000;
				StringBuilder fb = new StringBuilder();
				fb.append(freq / 100).append('.').append(freq % 100);
				
				RadioPosition rp = new RadioPosition(cs, tlo.optInt("id", 0), fb.toString());	
				int alt = (int) Math.round(tlo.getDouble("heightMslM") / 0.3048);
				rp.setPosition(tlo.getDouble("latDeg"), tlo.getDouble("lonDeg"), alt);
				results.add(rp);
			}
		}
		
		return results;
	}
}