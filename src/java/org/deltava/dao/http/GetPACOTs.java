// Copyright 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to loac Pacific Track data.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetPACOTs extends DAO implements TrackDAO {
	
	private static final String CRLF = System.getProperty("line.separator");
	
	private String _url;
	private String _notam;

	/**
	 * Initializes the Data Access Object.
	 * @param url the URL to fetch from
	 */
	public GetPACOTs(String url) {
		super();
		_url = url;
	}

	/**
	 * Retrieves the PACOT information.
	 * @return a String with the formatted PACOT data
	 * @throws DAOException if an I/O error occurs
	 */
	public String getTrackInfo() throws DAOException {
		StringBuilder buf = new StringBuilder();
		try {
			LineNumberReader br = new LineNumberReader(new InputStreamReader(getStream(_url)));
			String data = br.readLine();
			while (data != null) {
				buf.append(data);
				buf.append("<BR />");
				buf.append(CRLF);
				data = br.readLine();
			}
			
			br.close();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		// Split out the data
		String notam = buf.toString().toUpperCase(); int pos = notam.indexOf("<INPUT TYPE=\"CHECKBOX\"");
		buf.setLength(0);
		while (pos != -1) {
			int startPos = notam.indexOf("<PRE>", pos);
			int endPos = notam.indexOf("</PRE>", startPos);
			if ((startPos != -1) && (endPos != -1)) {
				String trackInfo = notam.substring(startPos + 5, endPos);
				trackInfo = trackInfo.replace("<B>", "");
				trackInfo = trackInfo.replace("</B>", "");
				buf.append(trackInfo);
				buf.append("<br /><hr />");
				buf.append(CRLF);
			}
			
			// Find next occurrance
			pos = notam.indexOf("<INPUT TYPE=\"CHECKBOX\"", endPos);
		}
		
		// Return the data
		_notam = buf.toString();
		return _notam;
	}
	
	/**
	 * Returns the Waypoints for each Pacific Track.
	 * @return a Map of waypoint codes, keyed by track code
	 * @throws DAOException if an I/O error occurs
	 */
	public Map<String, Collection<String>> getWaypoints() throws DAOException {
		if (_notam == null)
			getTrackInfo();

		Map<String, Collection<String>> info = new LinkedHashMap<String, Collection<String>>();
		try {
			LineNumberReader br = new LineNumberReader(new StringReader(_notam));
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
					
					while ((data != null) && (!data.startsWith("RTS"))) {
						data = data.replace("<BR />", "");
						wps.addAll(StringUtils.split(data.trim(), " "));
						data = br.readLine();
					}
					
					// Save the track
					info.put(trackID, wps);
				} else if (data.startsWith("TRACK ") && data.endsWith(".<BR />")) {
					int tPos = data.indexOf(' ');
					int tePos = data.indexOf('.', tPos);
					if (tePos != -1) {
						String trackID = data.substring(tPos + 1, data.length() - 7);
						Collection<String> wps = new LinkedHashSet<String>();
						data = br.readLine();
						while ((data != null) && data.startsWith(" ")) {
							data = data.replace("<BR />", "");
							if (data.contains(":")) {
								int wPos = data.indexOf(':') + 1; 
								wps.addAll(StringUtils.split(data.substring(wPos).trim(), " "));
							} else
								wps.addAll(StringUtils.split(data.trim(), " "));
							
							data = br.readLine();
						}
						
						// Save the track
						info.put(trackID, wps);
					}
				}
				
				data = br.readLine();
			}
			
			br.close();
			return info;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}