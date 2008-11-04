// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import java.net.*;
import java.io.InputStream;

import org.deltava.beans.wx.WeatherDataBean;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.GetNOAAWeather;
import org.deltava.dao.wsdl.GetFAWeather;

import org.deltava.service.WebService;

import org.deltava.util.cache.*;
import org.deltava.util.ftp.*;
import org.deltava.util.system.SystemData;

/**
 * An abstract class for common weather Web Service code. 
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

abstract class WeatherDataService extends WebService {
	
	protected static final Cache<WeatherDataBean> _cache = new ExpiringCache<WeatherDataBean>(256, 1800);

	/**
	 * Returns an NOAA Weather bean.
	 * @param type the weather type (TAF / METAR)  
	 * @return a WeatherDataBean
	 * @throws DAOException if an error occurs
	 */
	protected WeatherDataBean getNOAAData(String type, String code) throws DAOException {
		try {
			URL url = new URL(SystemData.get("weather.url." + type.toLowerCase()));
			if (!"ftp".equalsIgnoreCase(url.getProtocol()))
				throw new DAOException("FTP expected - " + url.toExternalForm());
			
			// Connect to the FTP site and change directories
			FTPConnection ftpc = new FTPConnection(url.getHost());
			ftpc.connect("anonymous", "golgotha@" + InetAddress.getLocalHost().getHostName());
			ftpc.getClient().chdir(url.getPath());
			
			// Get the data
			GetNOAAWeather dao = null;
			try {
				InputStream is = ftpc.get(code.toUpperCase() + ".TXT", false);
				dao = new GetNOAAWeather(is);
			} catch (FTPClientException fe) {
				WeatherDataBean wx = WeatherDataBean.create(type);
				wx.setData("Weather not available - " + fe.getMessage());
				return wx;
			}
			
			return dao.get(type);
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