// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.text.*;
import java.util.Date;

import org.deltava.beans.wx.*;

import org.deltava.dao.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to download Weather data from the NOAA.
 * @author Luke
 * @version 2.6
 * @since 2.2
 */

public class GetNOAAWeather extends DAO implements CachingDAO {
	
	private static final Cache<METAR> _metarCache = new ExpiringCache<METAR>(32768, 3600);
	
	private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to use
	 */
	public GetNOAAWeather(InputStream is) {
		super(is);
	}
	
	public int getHits() {
		return _metarCache.getHits();
	}
	
	public int getRequests() {
		return _metarCache.getRequests();
	}
	
	/**
	 * Loads a weather data bean.
	 * @param t the bean type (TAF/METAR)
	 * @return the weather bean, or null if not found
	 * @throws DAOException if an I/O error occurs
	 */
	public WeatherDataBean get(WeatherDataBean.Type t) throws DAOException {
		switch (t) {
			case TAF:
				return getTAF();
			case METAR:
			default:
				return getMETAR();
		}
	}

	/**
	 * Loads METAR data from the stream.
	 * @return a METAR bean
	 * @throws DAOException if an I/O error occurs
	 */
	public METAR getMETAR() throws DAOException {
		try {
			LineNumberReader lr = getReader();
			Date dt = df.parse(lr.readLine());
			StringBuilder buf = new StringBuilder(lr.readLine());
			while (lr.ready()) {
				buf.append(lr.readLine());
				buf.append(' ');
			}
			
			// Parse the METAR
			METAR result = new METAR();
			result.setDate(dt);
			result.setData(buf.toString());
			return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Loads TAF data from the stream.
	 * @return a TAF bean
	 * @throws DAOException if an I/O error occurs
	 */
	public TAF getTAF() throws DAOException {
		try {
			LineNumberReader lr = getReader();
			Date dt = df.parse(lr.readLine());
			StringBuilder buf = new StringBuilder();
			while (lr.ready()) {
				buf.append(lr.readLine());
				buf.append("\r\n");
			}
			
			// Build the TAF
			TAF result = new TAF();
			result.setDate(dt);
			result.setData(buf.toString());
			return result;
		} catch (ParseException pe) {
			throw new DAOException(pe);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}