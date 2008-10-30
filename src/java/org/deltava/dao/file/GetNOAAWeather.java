// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.text.*;
import java.util.Date;

import org.deltava.beans.wx.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to download Weather data from the NOAA.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class GetNOAAWeather extends DAO {
	
	private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to use
	 */
	public GetNOAAWeather(InputStream is) {
		super(is);
	}
	
	/**
	 * Loads a weather data bean.
	 * @param type the bean type (TAF/METAR)
	 * @param code the airport ICAO code
	 * @return the weather bean, or null if not found
	 * @throws DAOException if an I/O error occurs
	 */
	public WeatherDataBean get(String type, String code) throws DAOException {
		boolean isMETAR = "METAR".equalsIgnoreCase(type);
		return isMETAR ? getMETAR(code) : getTAF(code);
	}

	/**
	 * Loads METAR data from the stream.
	 * @param code the airport code
	 * @return a METAR bean
	 * @throws DAOException if an I/O error occurs
	 */
	public METAR getMETAR(String code) throws DAOException {
		try {
			LineNumberReader lr = getReader();
			Date dt = df.parse(lr.readLine());
			StringBuilder buf = new StringBuilder();
			while (lr.ready()) {
				buf.append(lr.readLine());
				buf.append("\r\n");
			}
			
			// Build the METAR
			METAR result = new METAR(code);
			result.setDate(dt);
			result.setData(buf.toString());
			return result;
		} catch (ParseException pe) {
			throw new DAOException(pe);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	/**
	 * Loads TAF data from the stream.
	 * @param code the airport code
	 * @return a TAF bean
	 * @throws DAOException if an I/O error occurs
	 */
	public TAF getTAF(String code) throws DAOException {
		try {
			LineNumberReader lr = getReader();
			Date dt = df.parse(lr.readLine());
			StringBuilder buf = new StringBuilder();
			while (lr.ready()) {
				buf.append(lr.readLine());
				buf.append("\r\n");
			}
			
			// Build the TAF
			TAF result = new TAF(code);
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