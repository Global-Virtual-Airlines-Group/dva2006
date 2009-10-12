// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.Date;

import org.deltava.beans.navdata.AirportLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;

import org.deltava.util.cache.*;
import org.deltava.util.ftp.FTPConnection;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to download Weather data from the NOAA.
 * @author Luke
 * @version 2.6
 * @since 2.2
 */

public class GetNOAAWeather extends DAO implements CachingDAO {
	
	private static final Cache<WeatherDataBean> _cache = new ExpiringCache<WeatherDataBean>(8192, 3600);
	
	private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	/**
	 * Initializes the Data Access Object.
	 */
	public GetNOAAWeather() {
		super(null);
	}
	
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
	}
	
	/**
	 * Loads a weather data bean.
	 * @param t the bean type (TAF/METAR)
	 * @param code the ICAO code
	 * @return the weather bean, or null if not found
	 * @throws DAOException if an I/O error occurs
	 */
	public WeatherDataBean get(WeatherDataBean.Type t, String code) throws DAOException {
		switch (t) {
			case TAF:
				return getTAF(code);
			case METAR:
			default:
				return getMETAR(code);
		}
	}

	/**
	 * Loads METAR data from the stream.
	 * @param code the ICAO code
	 * @return a METAR bean
	 * @throws DAOException if an I/O error occurs
	 */
	public METAR getMETAR(String code) throws DAOException {
		if (code == null)
			return null;
		
		// Check the cache
		METAR result = (METAR) _cache.get("METAR$" + code);
		if (result != null)
			return result;
		
		try {
			URL url = new URL(SystemData.get("weather.url.metar"));
			if (!"ftp".equalsIgnoreCase(url.getProtocol()))
				throw new DAOException("FTP expected - " + url.toExternalForm());
			
			// Get the file name
			String fName = code.toUpperCase() + ".TXT";

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
			result = MetarParser.parse(buf.toString());
			result.setDate(dt);
			result.setData(buf.toString());
			_cache.add(result);
			return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Loads TAF data from the stream.
	 * @param code the ICAO code
	 * @return a TAF bean
	 * @throws DAOException if an I/O error occurs
	 */
	public TAF getTAF(final String code) throws DAOException {
		if (code == null)
			return null;
		
		// Check the cache
		TAF result = (TAF) _cache.get("TAF$" + code);
		if (result != null)
			return result;
		
		try {
			URL url = new URL(SystemData.get("weather.url.taf"));
			if (!"ftp".equalsIgnoreCase(url.getProtocol()))
				throw new DAOException("FTP expected - " + url.toExternalForm());
			
			// Get the file name
			String fName = code.toUpperCase() + ".TXT";
			
			// Connect to the FTP site and change directories
			FTPConnection ftpc = new FTPConnection(url.getHost());
			ftpc.connect("anonymous", "golgotha@" + InetAddress.getLocalHost().getHostName());
			ftpc.getClient().chdir(url.getPath());
			if (!ftpc.hasFile(url.getPath(), fName)) {
				ftpc.close();
				return null;
			}

			// Get the file
			InputStream is = ftpc.get(code + ".TXT", false);
			LineNumberReader lr = new LineNumberReader(new InputStreamReader(is), 16384);
			Date dt = df.parse(lr.readLine());
			StringBuilder buf = new StringBuilder();
			while (lr.ready()) {
				buf.append(lr.readLine());
				buf.append("\r\n");
			}
			
			// Close the file
			ftpc.close();
			
			// Build the TAF
			result = new TAF();
			result.setDate(dt);
			result.setData(buf.toString());
			result.setAirport(new AirportLocation(0, 0) {{ setCode(code); }});
			_cache.add(result);
			return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}