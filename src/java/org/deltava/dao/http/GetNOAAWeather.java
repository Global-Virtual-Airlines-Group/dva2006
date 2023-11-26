// Copyright 2008, 2009, 2011, 2016, 2019, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.Instant;

import org.apache.logging.log4j.*;

import org.deltava.beans.navdata.AirportLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to download Weather data from the NOAA.
 * @author Luke
 * @version 11.1
 * @since 2.2
 */

public class GetNOAAWeather extends DAO {

	private static final Logger log = LogManager.getLogger(GetNOAAWeather.class);

	private static String getURL(String base, int hour) throws DAOException {
		try {
			URI baseURL = new URI(base);
			if (!"https".equalsIgnoreCase(baseURL.getScheme()))
				throw new DAOException(String.format("HTTPS expected - %s", baseURL));
			
			URI url = new URI(base + "/" + StringUtils.format(hour, "00") + "Z.TXT");
			return url.toString();
		} catch (URISyntaxException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves a complete METAR cycle.
	 * @param hour the hour in military time
	 * @return a Map of METAR objects, keyed by airport code
	 * @throws DAOException if an I/O error occurs
	 */
	public Map<String, METAR> getMETARCycle(int hour) throws DAOException {
		try {
			init(getURL(SystemData.get("weather.url.metarCycle"), hour));
			Map<String, METAR> results = new TreeMap<String, METAR>();
			try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(getIn(), "UTF-8"), 65536)) {
				while (lr.ready()) {
					String date = lr.readLine();
					if (date.indexOf(' ') < date.lastIndexOf(' '))
						date = date.substring(0, date.indexOf(' ', date.indexOf(' ') + 1));

					StringBuilder buf = new StringBuilder();
					String data = lr.readLine();
					while (!StringUtils.isEmpty(data)) {
						buf.append(data);
						buf.append(' ');
						data = lr.readLine();
					}

					try {
						Instant dt = StringUtils.parseInstant(date, "yyyy/MM/dd HH:mm");
						if (!StringUtils.isEmpty(buf)) {
							METAR m = MetarParser.parse(buf.toString());
							m.setDate(dt);
							m.setData(XMLUtils.stripInvalidUnicode(buf.toString()));
							results.put(m.getCode(), m);
						}
					} catch (Exception e) {
						log.warn("Error parsing {} at Line {} - {}", buf, Integer.valueOf(lr.getLineNumber()), e.getMessage());
					}
				}
			}

			return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}

	/**
	 * Retrieves a complete TAF cycle.
	 * @param hour the hour in military time
	 * @return a Map of TAF objects, keyed by airport code
	 * @throws DAOException if an I/O error occurs
	 */
	public Map<String, TAF> getTAFCycle(int hour) throws DAOException {
		try {
			init(getURL(SystemData.get("weather.url.tafCycle"), hour));
			Map<String, TAF> results = new TreeMap<String, TAF>();
			try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(getIn(), "UTF-8"), 32768)) {
				while (lr.ready()) {
					String date = lr.readLine();
					while (lr.ready() && (!StringUtils.isEmpty(date)) && (!Character.isDigit(date.charAt(0))))
						date = lr.readLine();

					// Check for amendment
					boolean isAmended = date.contains("Ammendment");
					if (date.indexOf(' ') < date.lastIndexOf(' '))
						date = date.substring(0, date.indexOf(' ', date.indexOf(' ') + 1));

					StringBuilder buf = new StringBuilder();
					String data = lr.readLine();
					while (!StringUtils.isEmpty(data)) {
						buf.append(data);
						buf.append((data.length() > 5) ? "\r\n" : " ");
						data = lr.readLine();
					}

					// Build the TAF
					try {
						Instant dt = StringUtils.parseInstant(date, "yyyy/MM/dd HH:mm");
						TAF t = new TAF();
						t.setAmended(isAmended);
						t.setDate(dt);
						t.setData(buf.toString());

						// Get the code
						String code = null;
						List<String> parts = StringUtils.split(t.getData(), " ");
						for (Iterator<String> i = parts.iterator(); i.hasNext() && (code == null);) {
							String part = i.next();
							if (part.length() == 4)
								code = part;
						}

						// Get the code
						if (code != null) {
							AirportLocation loc = new AirportLocation(0, 0);
							loc.setCode(code);
							t.setAirport(loc);
							results.put(code, t);
						}
					} catch (Exception e) {
						log.warn("Error parsing {} at Line {} - {}", buf, Integer.valueOf(lr.getLineNumber()), e.getMessage());
					}
				}
			}

			return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}