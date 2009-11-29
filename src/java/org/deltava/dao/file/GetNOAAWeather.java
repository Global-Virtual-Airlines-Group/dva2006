// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;

import org.deltava.beans.navdata.AirportLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.ftp.FTPConnection;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to download Weather data from the NOAA.
 * @author Luke
 * @version 2.7
 * @since 2.2
 */

public class GetNOAAWeather extends DAO {
	
	private static final Logger log = Logger.getLogger(GetNOAAWeather.class);
	
	private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	/**
	 * Initializes the Data Access Object.
	 */
	public GetNOAAWeather() {
		super(null);
	}
	
	/**
	 * Retrieves a complete METAR cycle.
	 * @param hour the hour in military time
	 * @return a Map of METAR objects, keyed by airport code
	 * @throws DAOException if an I/O error occurs
	 */
	public Map<String, METAR> getMETARCycle(int hour) throws DAOException {
		try {
			URL url = new URL(SystemData.get("weather.url.metarCycle"));
			if (!"ftp".equalsIgnoreCase(url.getProtocol()))
				throw new DAOException("FTP expected - " + url.toExternalForm());
			
			// Get the file name
			String fName = StringUtils.format(hour, "00") + "Z.TXT";
			
			// Connect to the FTP site and change directories
			FTPConnection ftpc = new FTPConnection(url.getHost());
			ftpc.connect("anonymous", "golgotha@" + InetAddress.getLocalHost().getHostName());
			ftpc.getClient().chdir(url.getPath());
			if (!ftpc.hasFile(url.getPath(), fName)) {
				ftpc.close();
				return Collections.emptyMap();
			}
			
			// Get the file
			Map<String, METAR> results = new TreeMap<String, METAR>();
			InputStream is = ftpc.get(fName, false);
			ftpc.close();
			LineNumberReader lr = new LineNumberReader(new InputStreamReader(is), 32768);
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
					Date dt = df.parse(date);
					if (!StringUtils.isEmpty(buf)) {
						METAR m = MetarParser.parse(buf.toString());
						m.setDate(dt);
						m.setData(XMLUtils.stripInvalidUnicode(buf.toString()));
						results.put(m.getCode(), m);
					}
				} catch (Exception e) {
					log.warn("Error parsing " + buf.toString() + " at Line " + lr.getLineNumber() + " - " + e.getMessage());
				}
			}
			
			// Close the file
			is.close();
			return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}

	/**
	 * Loads METAR data from the stream.
	 * @param loc the AirportLocation
	 * @return a METAR bean
	 * @throws DAOException if an I/O error occurs
	 */
	@Deprecated
	public METAR getMETAR(AirportLocation loc) throws DAOException {
		if (loc == null)
			return null;
		
		try {
			URL url = new URL(SystemData.get("weather.url.metar"));
			if (!"ftp".equalsIgnoreCase(url.getProtocol()))
				throw new DAOException("FTP expected - " + url.toExternalForm());
			
			// Get the file name
			String fName = loc.getCode() + ".TXT";

			// Connect to the FTP site and change directories
			FTPConnection ftpc = new FTPConnection(url.getHost());
			ftpc.connect("anonymous", "golgotha@" + InetAddress.getLocalHost().getHostName());
			ftpc.getClient().chdir(url.getPath());
			if (!ftpc.hasFile(url.getPath(), fName)) {
				ftpc.close();
				return null;
			}
			
			// Get the file
			InputStream is = ftpc.get(fName, false);
			LineNumberReader lr = new LineNumberReader(new InputStreamReader(is), 16384);
			Date dt = df.parse(lr.readLine());
			StringBuilder buf = new StringBuilder(lr.readLine());
			while (lr.ready()) {
				buf.append(lr.readLine());
				buf.append(' ');
			}
			
			// Close the file
			ftpc.close();
			
			// Parse the METAR
			METAR result = MetarParser.parse(buf.toString());
			result.setDate(dt);
			result.setData(XMLUtils.stripInvalidUnicode(buf.toString()));
			result.setAirport(loc);
			return result;
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
			URL url = new URL(SystemData.get("weather.url.tafCycle"));
			if (!"ftp".equalsIgnoreCase(url.getProtocol()))
				throw new DAOException("FTP expected - " + url.toExternalForm());
			
			// Get the file name
			String fName = StringUtils.format(hour, "00") + "Z.TXT";
			
			// Connect to the FTP site and change directories
			FTPConnection ftpc = new FTPConnection(url.getHost());
			ftpc.connect("anonymous", "golgotha@" + InetAddress.getLocalHost().getHostName());
			ftpc.getClient().chdir(url.getPath());
			if (!ftpc.hasFile(url.getPath(), fName)) {
				ftpc.close();
				return Collections.emptyMap();
			}
			
			// Get all the files
			Map<String, TAF> results = new TreeMap<String, TAF>();
			InputStream is = ftpc.get(fName, false);
			ftpc.close();
			LineNumberReader lr = new LineNumberReader(new InputStreamReader(is), 32768);
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
					Date dt = df.parse(date);
					TAF t = new TAF();
					t.setAmended(isAmended);
					t.setDate(dt);
					t.setData(XMLUtils.stripInvalidUnicode(buf.toString()));
					
					// Get the code
					String code = null;
					List<String> parts = StringUtils.split(t.getData(), " ");
					for (Iterator<String> i = parts.iterator(); i.hasNext() && (code == null); ) {
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
					log.warn("Error parsing " + buf.toString() + " at Line " + lr.getLineNumber() + " - " + e.getMessage());
				}
			}
				
			// Disconnect
			is.close();
			return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Loads TAF data from the stream.
	 * @param loc the AirportLocation bean
	 * @return a TAF bean
	 * @throws DAOException if an I/O error occurs
	 */
	@Deprecated
	public TAF getTAF(AirportLocation loc) throws DAOException {
		if (loc == null)
			return null;
		
		try {
			URL url = new URL(SystemData.get("weather.url.taf"));
			if (!"ftp".equalsIgnoreCase(url.getProtocol()))
				throw new DAOException("FTP expected - " + url.toExternalForm());
			
			// Get the file name
			String fName = loc.getCode() + ".TXT";
			
			// Connect to the FTP site and change directories
			FTPConnection ftpc = new FTPConnection(url.getHost());
			ftpc.connect("anonymous", "golgotha@" + InetAddress.getLocalHost().getHostName());
			ftpc.getClient().chdir(url.getPath());
			if (!ftpc.hasFile(url.getPath(), fName)) {
				ftpc.close();
				return null;
			}

			// Get the file
			InputStream is = ftpc.get(fName, false);
			LineNumberReader lr = new LineNumberReader(new InputStreamReader(is), 10240);
			Date dt = df.parse(lr.readLine());
			StringBuilder buf = new StringBuilder();
			while (lr.ready()) {
				buf.append(lr.readLine());
				buf.append("\r\n");
			}
			
			// Close the file
			is.close();
			
			// Build the TAF
			TAF result = new TAF();
			result.setDate(dt);
			result.setData(XMLUtils.stripInvalidUnicode(buf.toString()));
			result.setAirport(loc);
			return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}