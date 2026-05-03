// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2015, 2022, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;

import org.json.*;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to get North Atlantic Track data.
 * @author Luke
 * @version 12.4
 * @since 1.0
 */

public class GetNATs extends TrackDAO {
	
	private final String _url;
	private String _notam;

	/**
	 * Initializes the Data Access Object.
	 * @param url the URL to fetch from
	 */
	public GetNATs(String url) {
		super();
		_url = url;
	}

	/**
	 * Retrieves the NAT information.
	 * @return a String with the formatted NAT data
	 * @throws DAOException if an I/O error occurs
	 */
	@Override
	public String getTrackInfo() throws DAOException {
		try {
			setCompression(Compression.GZIP, Compression.DEFLATE);
			init(_url);
			StringBuilder buf = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(getIn(), "utf-8"))) {
				JSONArray ja = new JSONArray(new JSONTokener(br));
				for (int x = 0; x < ja.length(); x++) {
					JSONObject jo = ja.getJSONObject(x);
					String data = jo.optString("condition_message");
					if (StringUtils.isEmpty(data)) continue;
					
					// Split the string
					int sPos = Math.max(0, data.indexOf("NAT-"));
					int ePos = data.indexOf("END OF PART");
					if (ePos > -1)
						ePos = data.indexOf('\n', ePos);
					
					buf.append(data.subSequence(sPos, (ePos == -1) ? data.length() : ePos));
				}
			}

			_notam = buf.toString();
			return _notam;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}

	/**
	 * Returns the Waypoints for each North Atlantic Track.
	 * @return a Map of {@link org.deltava.beans.navdata.OceanicTrack} beans, keyed by track code
	 * @throws DAOException if an I/O error occurs
	 */
	@Override
	public Map<String, Collection<String>> getWaypoints() throws DAOException {
		if (_notam == null)
			getTrackInfo();

		// Parse the NOTAM data
		try {
			Map<String, Collection<String>> results = new TreeMap<String, Collection<String>>();
			try (BufferedReader br = new BufferedReader(new StringReader(_notam))) {
				while (br.ready()) {
					String data = br.readLine();
					br.mark(512);

					// Check if the track code is the first character
					if ((data != null) && (data.length() > 2) && (data.charAt(1) == ' ')) {
						String el = br.ready() ? br.readLine() : null;
						String wl = br.ready() ? br.readLine() : null;

						// Validate the next two lines - if they're good then parse the track
						if ((el != null) && (el.startsWith("EAST LVLS") && (wl != null) && (wl.startsWith("WEST LVLS")))) {
							String code = data.substring(0, 1);
							int end = data.indexOf('<');
							if (end == -1)
								end = data.length();

							Collection<String> wps = new LinkedHashSet<String>(StringUtils.split(data.substring(2, end), " "));
							results.put(code, wps);
						}
					} else if (data == null)
						break;

					br.reset();
				}
			}

			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}