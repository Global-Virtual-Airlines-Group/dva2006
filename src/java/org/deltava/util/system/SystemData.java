// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.system;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;

/**
 * A singleton object containing all of the configuration data for the application. This object is internally synchronized
 * to allow thread-safe read and write access to the configuration data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class SystemData implements Serializable {

	private static final Logger log = Logger.getLogger(SystemData.class);

	public static final String AUTHENTICATOR = "security.auth.obj";
	public static final String JDBC_POOL = "jdbc.pool";
	public static final String TASK_POOL = "tasks.pool";
	public static final String ACARS_POOL = "acars.pool";
	public static final String ACARS_DAEMON = "acars.daemon";
	public static final String SMTP_DAEMON = "smtp.daemonthread";

	static final String CFG_NAME = "$CONFIGNAME$";
	static final String LOADER_NAME = "$LOADERCLASS$";

	private static SystemDataLoader _loader;
	private static final Map<String, Object> _properties = new HashMap<String, Object>();
	
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock r = rwl.readLock();
    private static final Lock w = rwl.writeLock();

	// This is a singleton
	private SystemData() {
		super();
	}

	/**
	 * Initializes SystemData object.
	 * @param loaderClassName the data loader class name
	 * @param clearData TRUE if existing data should be cleared, otherwise FALSE
	 */
	public static void init(String loaderClassName, boolean clearData) {

		// Get the data loader class
		try {
			Class ldClass = Class.forName(loaderClassName);
			_loader = (SystemDataLoader) ldClass.newInstance();
			log.debug("Instantiated " + loaderClassName);
		} catch (Exception e) {
			_loader = new XMLSystemDataLoader();
			log.debug("Using default loader class " + _loader.getClass().getName());
		}
		
		// Reset the properties
		w.lock();
		if (clearData)
			_properties.clear();

		try {
			_properties.putAll(_loader.load());
		} catch (IOException ie) {
			Throwable ce = ie.getCause();
			if (ce == null)
				log.error("Error loading System Data - " + ie.getMessage());
			else
				log.error("Error loading System Data - " + ce.getMessage());
		} finally {
			_properties.put(SystemData.LOADER_NAME, _loader.getClass().getName());
			w.unlock();
		}
	}

	/**
	 * Initializes the System data with the default loader.
	 */
	public static void init() {
		init("DEFAULT", true);
	}

	/**
	 * Returns a property value.
	 * @param propertyName the property name
	 * @return the property
	 */
	public static Object getObject(String propertyName) {
		r.lock();
		try {
			return _properties.get(propertyName);
		} finally {
			r.unlock();
		}
	}

	/**
	 * Returns a boolean property value.
	 * @param propertyName the property name
	 * @return TRUE if the object is a boolean value and TRUE, otherwise FALSE
	 */
	public static boolean getBoolean(String propertyName) {
		Object obj = getObject(propertyName);
		return (obj instanceof Boolean) ? ((Boolean) obj).booleanValue() : false;
	}

	/**
	 * Returns an integer property value.
	 * @param propertyName the property name
	 * @param defValue the default value
	 * @return the property value, or the default if not an integer
	 */
	public static int getInt(String propertyName, int defValue) {
		Object obj = getObject(propertyName);
		return (obj instanceof Number) ? ((Number) obj).intValue() : defValue;
	}

	/**
	 * Returns an integer property value.
	 * @param propertyName the property name
	 * @return the property value, or 0 if not an integer
	 */
	public static int getInt(String propertyName) {
		return getInt(propertyName, 0);
	}

	/**
	 * Returns a long integer property value.
	 * @param propertyName the property name
	 * @param defValue the default value
	 * @return the property value, or defValue if not a long
	 */
	public static long getLong(String propertyName, long defValue) {
		Object obj = getObject(propertyName);
		return (obj instanceof Long) ? ((Long) obj).longValue() : defValue;
	}

	/**
	 * Returns a floating point property value.
	 * @param propertyName the property name
	 * @param defValue the default value
	 * @return the property value, or defValue if not a double
	 */
	public static double getDouble(String propertyName, double defValue) {
		Object obj = getObject(propertyName);
		return (obj instanceof Double) ? ((Double) obj).doubleValue() : defValue;
	}

	/**
	 * Returns a property value.
	 * @param propertyName the property name
	 * @return the property value, or null if not found
	 */
	public static String get(String propertyName) {
		Object obj = getObject(propertyName);
		return (obj == null) ? null : obj.toString();
	}

	/**
	 * Adds a property.
	 * @param pName the property name
	 * @param value the property value
	 */
	public static void add(String pName, Object value) {
		log.debug("Adding value " + pName);
		w.lock();
		try {
			_properties.put(pName, value);
		} finally {
			w.unlock();
		}
	}

	/**
	 * Returns an Airport object.
	 * @param airportCode the Airport code (IATA or ICAO)
	 * @return the Airport bean, or null if not found
	 * @throws IllegalStateException if the &quot;airports&quot; property has not been added
	 * @see SystemData#getAirports()
	 */
	public static Airport getAirport(String airportCode) {
		if (airportCode == null)
			return null;

		if (!_properties.containsKey("airports"))
			throw new IllegalStateException("Airports not Loaded");

		Map airports = (Map) getObject("airports");
		return (Airport) airports.get(airportCode.toUpperCase());
	}
	
	/**
	 * Returns all Airports.
	 * @return a Map of Airports, indexed by ICAO/IATA code.
	 * @see SystemData#getAirport(String)
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Airport> getAirports() {
		return (Map) getObject("airports");
	}
	
	/**
	 * Returns an Airline object.
	 * @param airlineCode the Airline code
	 * @return the Airline bean, or null if not found
	 * @throws NullPointerException if airlineCode is null
	 * @throws IllegalStateException if the &quot;airlines&quot; property has not been added
	 */
	public static Airline getAirline(String airlineCode) {
		if (airlineCode == null)
			return null;

		if (!_properties.containsKey("airlines"))
			throw new IllegalStateException("Airlines not Loaded");

		// Search based on primary code
		Map airlines = (Map) getObject("airlines");
		Airline a = (Airline) airlines.get(airlineCode.trim().toUpperCase());
		if (a != null)
			return a;
		
		// Search based on secondary codes
		for (Iterator i = airlines.values().iterator(); i.hasNext(); ) {
			a = (Airline) i.next();
			if (a.getCodes().contains(airlineCode.toUpperCase()))
				return a;
		}
		
		return null;
	}
	
	/**
	 * Returns all airlines for the current web application.
	 * @return a Collection of Airline beans
	 */
	public static Map<String, Airline> getAirlines() {
		String code = (String) getObject("airline.code");
		Map airlines = (Map) getObject("airlines");
		Map<String, Airline> results = new LinkedHashMap<String, Airline>();
		for (Iterator i = airlines.values().iterator(); i.hasNext(); ) {
			Airline a = (Airline) i.next();
			if (a.getApplications().contains(code))
				results.put(a.getCode(), a);
		}
		
		return results;
	}
	
	/**
	 * Returns an Airline Information object, which is data about other virtual airlines on this server.
	 * @param airlineCode the airline code
	 * @return the AirlineInformation bean, or null if not found
	 * @throws IllegalStateException if the &quot;apps&quot; property has not been added
	 */
	public static AirlineInformation getApp(String airlineCode) {
	   if (airlineCode == null)
			return null;
	   
	   if (!_properties.containsKey("apps"))
			throw new IllegalStateException("Applications not Loaded");
	   
	   Map apps = (Map) getObject("apps");
	   return (AirlineInformation) apps.get(airlineCode.trim().toUpperCase());
	}
}