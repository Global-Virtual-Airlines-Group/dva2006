// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

import java.util.Date;

import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.util.cache.*;

/**
 * Loads weather data from FlightAware via SOAP.
 * @author Luke
 * @version 2.6
 * @since 2.2
 */

public class GetFAWeather extends FlightAwareDAO implements CachingDAO {
	
	private static final ExpiringCache<WeatherDataBean> _wxCache = new ExpiringCache<WeatherDataBean>(256, 1800);

	public int getHits() {
		return _wxCache.getHits();
	}
	
	public int getRequests() {
		return _wxCache.getRequests();
	}
	
	/**
	 * Loads a weather data bean.
	 * @param t the bean type (TAF/METAR)
	 * @param code the airport ICAO code
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
	 * Gets the METAR for a particular airport.
	 * @param code the airport ICAO code
	 * @return the METAR text, or null if not found
	 * @throws DAOException if an I/O error occurs
	 */
	public METAR getMETAR(String code) throws DAOException {
		if (code == null)
			return null;
		
		// Check the cache
		code = code.toUpperCase();
		String key = "METAR$" + code;
		if (_wxCache.contains(key))
			return (METAR) _wxCache.get(key);

		try {
			METAR result = new METAR();
			result.setData(getStub().METAR(code));
            result.setDate(new Date());
            _wxCache.add(result);
            return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Gets the TAF for a particular airport.
	 * @param code the airport ICAO code
	 * @return the TAF text, or null if not found
	 * @throws DAOException if an I/O error occurs
	 */
	public TAF getTAF(String code) throws DAOException {
		if (code == null)
			return null;
		
		// Check the cache
		code = code.toUpperCase();
		String key = "TAF$" + code;
		if (_wxCache.contains(key))
			return (TAF) _wxCache.get(key);

		try {
			TAF result = new TAF();
			result.setData(getStub().TAF(code));
			result.setDate(new Date());
			_wxCache.add(result);
			return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}