package org.deltava.util.system;

import java.util.*;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;

/**
 * A singleton object containing all of the configuration data for the application.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SystemData implements Serializable {

	private static final Logger log = Logger.getLogger(SystemData.class);

	public static final String AUTHENTICATOR = "security.auth.obj";
	public static final String JDBC_POOL = "jdbc.pool";
	public static final String TASK_POOL = "tasks.pool";
	public static final String ACARS_POOL = "acars.pool";

	static final String CFG_NAME = "$CONFIGNAME$";
	static final String LOADER_NAME = "$LOADERCLASS$";

	private static SystemDataLoader _loader;
	private static Map _properties = new HashMap();;

	// This is a singleton
	private SystemData() {
		super();
	}

	/**
	 * Initializes SystemData object.
	 * @param loaderClassName the data loader class name
	 */
	public final static synchronized void init(String loaderClassName) {

		// Get the data loader class
		try {
			Class ldClass = Class.forName(loaderClassName);
			_loader = (SystemDataLoader) ldClass.newInstance();
			log.debug("Instantiated " + loaderClassName);
		} catch (Exception e) {
			_loader = new XMLSystemDataLoader();
			log.debug("Using default loader class " + _loader.getClass().getName());
		}

		try {
			_properties = _loader.load();
		} catch (IOException ie) {
			Throwable ce = ie.getCause();
			if (ce == null) {
				log.warn("Error loading System Data - " + ie.getMessage());
			} else {
				log.warn("Error loading System Data - " + ce.getMessage());
			}

			_properties = new HashMap();
		}

		// Save the loader name and return
		_properties.put(SystemData.LOADER_NAME, _loader.getClass().getName());
	}

	/**
	 * Initializes the System data with the default loader.
	 */
	public final static synchronized void init() {
		init("DEFAULT");
	}

	/**
	 * Returns a property value.
	 * @param propertyName the property name
	 * @return the property
	 */
	public static Object getObject(String propertyName) {
		return _properties.get(propertyName);
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
	 * @return the property value, or zero if not an integer
	 */
	public static int getInt(String propertyName) {
		Object obj = getObject(propertyName);
		return (obj instanceof Number) ? ((Number) obj).intValue() : 0;
	}

	/**
	 * Returns a long integer property value.
	 * @param propertyName the property name
	 * @return the property value, or zero if not a long
	 */
	public static long getLong(String propertyName) {
		Object obj = getObject(propertyName);
		return (obj instanceof Long) ? ((Long) obj).longValue() : 0;
	}

	/**
	 * Returns a floating point property value.
	 * @param propertyName the property name
	 * @return the property value, or zero if not a double
	 */
	public static double getDouble(String propertyName) {
		Object obj = getObject(propertyName);
		return (obj instanceof Double) ? ((Double) obj).doubleValue() : 0;
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
	 * @see SystemData#addAll(Map)
	 */
	public static synchronized void add(String pName, Object value) {
		log.debug("Adding value " + pName);
		_properties.put(pName, value);
	}

	/**
	 * Adds a number of properties.
	 * @param data a Map of property names/values
	 * @see SystemData#add(String, Object)
	 */
	public static synchronized void addAll(Map data) {
		for (Iterator i = data.keySet().iterator(); i.hasNext();) {
			String pName = (String) i.next();
			log.debug("Adding value " + pName);
			_properties.put(pName, data.get(pName));
		}
	}

	/**
	 * Returns an Airport object.
	 * @param airportCode the Airport code (IATA or ICAO)
	 * @return the Airport bean, or null if not found
	 * @throws NullPointerException if airportCode is null
	 * @throws IllegalStateException if the &quot;airports&quot; property has not been added
	 */
	public static Airport getAirport(String airportCode) {
		if (airportCode == null)
			return null;

		if (!_properties.containsKey("airports"))
			throw new IllegalStateException("Airports not Loaded");

		Map airports = (Map) _properties.get("airports");
		return (Airport) airports.get(airportCode.toUpperCase());
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

		Map airlines = (Map) _properties.get("airlines");
		return (Airline) airlines.get(airlineCode.trim().toUpperCase());
	}
}