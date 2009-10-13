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
	
	private static final Cache<METAR> _wxCache = new ExpiringCache<METAR>(512, 1800);
	private static final Cache<TAF> _fCache = new ExpiringCache<TAF>(512, 1800);
	
	private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	/**
	 * Initializes the Data Access Object.
	 */
	public GetNOAAWeather() {
		super(null);
	}
	
	public CacheInfo getCacheInfo() {
		CacheInfo info = new CacheInfo(_wxCache);
		info.add(_fCache);
		return info;
	}
	
	/**
	 * Loads a weather data bean.
	 * @param t the bean type (TAF/METAR)
	 * @param loc the AirportLocation
	 * @return the weather bean, or null if not found
	 * @throws DAOException if an I/O error occurs
	 */
	public WeatherDataBean get(WeatherDataBean.Type t, AirportLocation loc) throws DAOException {
		switch (t) {
			case TAF:
				return getTAF(loc);
			case METAR:
			default:
				return getMETAR(loc);
		}
	}

	/**
	 * Loads METAR data from the stream.
	 * @param loc the AirportLocation
	 * @return a METAR bean
	 * @throws DAOException if an I/O error occurs
	 */
	public METAR getMETAR(AirportLocation loc) throws DAOException {
		if (loc == null)
			return null;
		
		// Check the cache
		METAR result = _wxCache.get(loc.getCode());
		if (result != null)
			return result;
		
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
			result = MetarParser.parse(buf.toString());
			result.setDate(dt);
			result.setData(buf.toString());
			result.setAirport(loc);
			_wxCache.add(result);
			return result;
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
	public TAF getTAF(AirportLocation loc) throws DAOException {
		if (loc == null)
			return null;
		
		// Check the cache
		TAF result = _fCache.get(loc.getCode());
		if (result != null)
			return result;
		
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
			result.setAirport(loc);
			_fCache.add(result);
			return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}