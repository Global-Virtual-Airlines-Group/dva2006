// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

import java.util.Date;

import org.deltava.beans.navdata.AirportLocation;
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
	
	private static final ExpiringCache<METAR> _wxCache = new ExpiringCache<METAR>(256, 1800);
	private static final ExpiringCache<TAF> _fCache = new ExpiringCache<TAF>(256, 1800);

	public CacheInfo getCacheInfo() {
		return new CacheInfo(_wxCache);
	}

	/**
	 * Loads a weather data bean.
	 * @param t the bean type (TAF/METAR)
	 * @param loc the AirportLocation bean
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
	 * Gets the METAR for a particular airport.
	 * @param loc the AirportLocation bean
	 * @return the METAR text, or null if not found
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
			String data = getStub().METAR(loc.getCode());
			result = MetarParser.parse(data);
			result.setData(data);
            result.setDate(new Date());
            result.setAirport(loc);
            _wxCache.add(result);
            return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Gets the TAF for a particular airport.
	 * @param loc the AirportLocation bean
	 * @return the TAF text, or null if not found
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
			result = new TAF();
			result.setData(getStub().TAF(loc.getCode()));
			result.setDate(new Date());
			_fCache.add(result);
			return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}