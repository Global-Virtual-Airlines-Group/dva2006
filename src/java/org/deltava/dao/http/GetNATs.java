// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to get North Atlantic Track data.
 * @author Luke
 * @version 5.0
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
	public String getTrackInfo() throws DAOException {
		try {
			init(_url);
			StringBuilder buf = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(getIn()))) {
				boolean isWriting = false;
				String data = br.readLine();
				while (data != null) {
					data = data.trim();

					// Check for start/end of NAT segment
					if (!isWriting && data.contains("NAT-")) {
						buf.append(data);
						buf.append("<br />");
						buf.append(CRLF);
						isWriting = true;
					} else if (isWriting && (data.startsWith("END OF PART"))) {
						buf.append(data);
						buf.append("<br /><hr />");
						buf.append(CRLF);
						isWriting = false;
					} else if (isWriting) {
						buf.append(data);
						buf.append("<br />");
						buf.append(CRLF);
					}

					data = br.readLine();
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