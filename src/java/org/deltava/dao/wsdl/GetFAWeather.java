// Copyright 2008, 2009, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

import java.time.Instant;

import org.deltava.beans.navdata.AirportLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.util.cache.*;

import com.flightaware.flightxml.soap.FlightXML2.*;

/**
 * Loads weather data from FlightAware via SOAP.
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class GetFAWeather extends FlightAwareDAO {
	
	private static final Cache<METAR> _wxCache = CacheManager.get(METAR.class, "FlightAwareMETAR");
	private static final Cache<TAF> _fCache = CacheManager.get(TAF.class, "FlightAwareTAF");

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
			MetarResults rsp = getStub().metar(new MetarRequest(loc.getCode()));
			String data = rsp.getMetarResult();
			result = MetarParser.parse(data);
			result.setData(data);
            result.setDate(Instant.now());
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
			TafResults rsp = getStub().taf(new TafRequest(loc.getCode()));
			result = new TAF();
			result.setData(rsp.getTafResult());
			result.setDate(Instant.now());
			_fCache.add(result);
			return result;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}