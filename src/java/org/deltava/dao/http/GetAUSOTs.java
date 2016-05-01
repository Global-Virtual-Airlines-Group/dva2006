// Copyright 2009, 2010, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to get Australian Track data.
 * @author Luke
 * @version 7.0
 * @since 2.7
 */

public class GetAUSOTs extends TrackDAO {

	private final String _url;
	private String _notam;

	/**
	 * Initializes the Data Access Object.
	 * @param url the URL to fetch from
	 */
	public GetAUSOTs(String url) {
		super();
		_url = url;
	}

	/**
	 * Retrieves the AUSOT information.
	 * @return a String with the formatted AUSOT data
	 * @throws DAOException if an I/O error occurs
	 */
	@Override
	public String getTrackInfo() throws DAOException {
		StringBuilder buf = new StringBuilder(1024);
		try {
			init(_url);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(getIn(), "UTF-8"))) {
				String data = br.readLine();
				while (data != null) {
					buf.append(data.toUpperCase());
					buf.append("<BR />");
					buf.append(CRLF);
					data = br.readLine();
				}
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}

		// Search for the code
		String data = buf.toString();
		int sPos = data.indexOf("<PRE>");
		int ePos = data.indexOf("</PRE>", sPos);
		if (ePos == -1)
			ePos = data.length();

		// Copy the data
		_notam = data.substring(sPos + 5, ePos);
		return _notam;
	}

	/**
	 * Returns the Waypoints for each Australian Track.
	 * @return a Map of {@link org.deltava.beans.navdata.OceanicTrack} beans, keyed by track code
	 * @throws DAOException if an I/O error occurs
	 */
	@Override
	public Map<String, Collection<String>> getWaypoints() throws DAOException {
		if (_notam == null) getTrackInfo();

		try {
			Map<String, Collection<String>> results = new TreeMap<String, Collection<String>>();
			try (BufferedReader br = new BufferedReader(new StringReader(_notam))) {
				String data = br.readLine();
				while (data != null) {
					int pos = data.indexOf("TDM TRK");
					if (pos != -1) {
						int endpos = data.indexOf(' ', pos + 9);
						String trackID = data.substring(pos + 8, endpos).trim();
						Collection<String> wps = new LinkedHashSet<String>();
						data = br.readLine();
						if (data != null)
							data = br.readLine();

						boolean noTrack = (data != null) && (data.contains("NO TRACK"));
						while ((data != null) && (!data.startsWith("RTS"))) {
							data = data.replace("<BR />", "");
							wps.addAll(StringUtils.split(data.trim(), " "));
							data = br.readLine();
						}

						// Save the track
						if (!noTrack)
							results.put(trackID, wps);
					}

					data = br.readLine();
				}
			}

			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}