// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import org.deltava.beans.wx.WeatherDataBean;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.GetNOAAWeather;
import org.deltava.dao.wsdl.GetFAWeather;

import org.deltava.service.WebService;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * An abstract class for common weather Web Service code. 
 * @author Luke
 * @version 2.6
 * @since 2.3
 */

abstract class WeatherDataService extends WebService {
	
	protected static final Cache<WeatherDataBean> _cache = new ExpiringCache<WeatherDataBean>(256, 1800);

	/**
	 * Returns an NOAA Weather bean.
	 * @param t the weather type (TAF / METAR) 
	 * @param code the 
	 * @return a WeatherDataBean
	 * @throws DAOException if an error occurs
	 */
	protected WeatherDataBean getNOAAData(WeatherDataBean.Type t, String code) throws DAOException {
		try {
			GetNOAAWeather dao = new GetNOAAWeather();
			WeatherDataBean wx = dao.get(t, code);
			if (wx == null) {
				wx = WeatherDataBean.create(t);
				wx.setData("Weather not available");
			}
			
			return wx;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Returns an initialized FlightAware Weather Data Access Object.
	 * @return a FlightAware weather DAO
	 */
	protected GetFAWeather getFAData() {
		GetFAWeather dao = new GetFAWeather();
		dao.setUser(SystemData.get("schedule.flightaware.download.user"));
		dao.setPassword(SystemData.get("schedule.flightaware.download.pwd"));
		return dao;
	}
}