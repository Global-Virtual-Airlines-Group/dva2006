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
 * @version 2.3
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
	 * @return the weather bean, or null if not found
	 * @throws DAOException if an I/O error occurs
	 */
	public WeatherDataBean get(String type) throws DAOException {
		boolean isMETAR = "METAR".equalsIgnoreCase(type);
		return isMETAR ? getMETAR() : getTAF();
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
			StringBuilder buf = new StringBuilder();
			while (lr.ready()) {
				buf.append(lr.readLine());
				buf.append("\r\n");
			}
			
			// Build the METAR
			METAR result = new METAR();
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