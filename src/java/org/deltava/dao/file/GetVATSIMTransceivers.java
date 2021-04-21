// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;


import org.json.*;
import org.deltava.beans.schedule.GeoPosition;
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
			JSONArray tta = to.getJSONArray("transceivers");
			RadioPosition rp = new RadioPosition(to.getString("callsign"));
			for (int y = 0; y < tta.length(); y++) {
				JSONObject tlo = tta.getJSONObject(y);
				int alt = (int) Math.round(tlo.getDouble("heightMslM") / 0.3048);
				rp.addPosition(new GeoPosition(tlo.getDouble("latDeg"), tlo.getDouble("lonDeg"), alt));
			}
			
			if (!rp.getPositions().isEmpty())
				results.add(rp);
		}
		
		return results;
	}
}