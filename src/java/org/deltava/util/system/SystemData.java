// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2017, 2018, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.system;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;
import org.deltava.comparators.AirlineComparator;
import org.deltava.util.StringUtils;

import org.gvagroup.pool.*;

/**
 * A singleton object containing all of the configuration data for the application. This object is internally synchronized
 * to allow thread-safe read and write access to the configuration data.
 * @author Luke
 * @version 11.3
 * @since 1.0
 */

public final class SystemData implements Serializable {

	private static final Logger log = LogManager.getLogger(SystemData.class);

	public static final String AUTHENTICATOR = "security.auth.obj";
	
	public static final String TASK_POOL = "tasks.pool";
	public static final String ECON_DATA = "econ.info";
	
	public static final String JDBC_POOL = "jdbc.pool";
	public static final String JEDIS_POOL ="jedis.pool";

	public static final String CFG_NAME = "$CONFIGNAME$";
	public static final String LOADER_NAME = "$LOADERCLASS$";

	private static final ConcurrentMap<String, Object> _properties = new ConcurrentHashMap<String, Object>();
	
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
		SystemDataLoader loader = null;
		try {
			Class<?> ldClass = Class.forName(loaderClassName);
			loader = (SystemDataLoader) ldClass.getDeclaredConstructor().newInstance();
			log.debug("Instantiated {}", loaderClassName);
		} catch (Exception e) {
			loader = new XMLSystemDataLoader();
			log.info("Using default loader {}", loader.getClass().getSimpleName());
		}
		
		// Reset the properties
		if (clearData) _properties.clear();
		try {
			_properties.putAll(loader.load());
		} catch (IOException ie) {
			log.atError().withThrowable(ie).log("Error loading System Data - {}", ie.getMessage());
		} finally {
			_properties.put(SystemData.LOADER_NAME, loader.getClass().getName());
		}
	}

	/**
	 * Initializes the System data with the default loader.
	 * @see SystemData#init(String, boolean)
	 */
	public static void init() {
		init("DEFAULT", true);
	}
	
	/**
	 * Returns whether a particular property exists.
	 * @param propertyName the property name
	 * @return TRUE if the property exists, otherwise FALSE
	 */
	public static boolean has(String propertyName) {
		return _properties.containsKey(propertyName);
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
		return (obj instanceof Boolean b) ? b.booleanValue() : false;
	}

	/**
	 * Returns an integer property value.
	 * @param propertyName the property name
	 * @param defValue the default value
	 * @return the property value, or the default if not an integer
	 */
	public static int getInt(String propertyName, int defValue) {
		Object obj = getObject(propertyName);
		return (obj instanceof Number n) ? n.intValue() : defValue;
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
		return (obj instanceof Long l) ? l.longValue() : defValue;
	}

	/**
	 * Returns a floating point property value.
	 * @param propertyName the property name
	 * @param defValue the default value
	 * @return the property value, or defValue if not a double
	 */
	public static double getDouble(String propertyName, double defValue) {
		Object obj = getObject(propertyName);
		return (obj instanceof Double d) ? d.doubleValue() : defValue;
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
		log.debug("Adding value {}", pName);
		_properties.put(pName, value);
	}

	/**
	 * Returns an Airport object.
	 * @param airportCode the Airport code (IATA or ICAO)
	 * @return the Airport bean, or null if not found
	 * @throws IllegalStateException if the &quot;airports&quot; property has not been added
	 * @see SystemData#getAirports()
	 */
	public static Airport getAirport(String airportCode) {
		if (airportCode == null) return null;
		Map<?, ?> airports = (Map<?, ?>) getObject("airports");
		if (airports == null)
			throw new IllegalStateException("Airports not Loaded");
		
		return (Airport) airports.get(airportCode.toUpperCase());
	}
	
	/**
	 * Returns all Airports.
	 * @return a Map of Airports, indexed by ICAO/IATA code.
	 * @see SystemData#getAirport(String)
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Airport> getAirports() {
		return (Map<String, Airport>) getObject("airports");
	}
	
	/**
	 * Returns an Airline object.
	 * @param airlineCode the Airline code
	 * @return the Airline bean, or null if not found
	 * @throws IllegalStateException if the &quot;airlines&quot; property has not been added
	 */
	public static Airline getAirline(String airlineCode) {
		if (airlineCode == null)
			return null;
		
		Map<?, ?> airlines = (Map<?, ?>) getObject("airlines");
		if (airlines == null)
			throw new IllegalStateException("Airlines not Loaded");

		// Search based on primary code
		String c = airlineCode.trim().toUpperCase();
		Airline a = (Airline) airlines.get(c);
		if (a != null)
			return a;
		
		// Search based on secondary codes
		return airlines.values().stream().map(Airline.class::cast).filter(al -> (al.getCodes().contains(c) || c.equals(al.getICAO()))).findFirst().orElse(null);
	}
	
	/**
	 * Returns all airlines for the current web application.
	 * @return a Collection of Airline beans
	 */
	public static Collection<Airline> getAirlines() {
		String code = (String) getObject("airline.code");
		Map<?, ?> airlines = (Map<?, ?>) getObject("airlines");
		Collection<Airline> results = new TreeSet<Airline>(new AirlineComparator(AirlineComparator.NAME));
		airlines.values().stream().map(Airline.class::cast).filter(a -> a.getApplications().contains(code)).forEach(results::add);
		return results;
	}
	
	/**
	 * Returns an Airline Information object, which is data about other virtual airlines on this server.
	 * @param airlineCode the airline code, or null if the current airline
	 * @return the AirlineInformation bean, or null if not found
	 * @throws IllegalStateException if the &quot;apps&quot; property has not been added
	 */
	public static AirlineInformation getApp(String airlineCode) {
		String code = StringUtils.isEmpty(airlineCode) ? get("airline.code") : airlineCode;
		Map<?, ?> apps = (Map<?, ?>) getObject("apps");
		if (apps == null)
			throw new IllegalStateException("Applications not Loaded");
	   
	   return (AirlineInformation) apps.get(code.trim().toUpperCase());
	}
	
	/**
	 * Returns information about all virtual airlines on this server. The current virtual airline will be the first member of this collection.
	 * @return a Collection of AirlineInformation beans
	 */
	public static Collection<AirlineInformation> getApps() {
		Map<?, ?> apps = (Map<?, ?>) getObject("apps");
		if (apps == null)
			throw new IllegalStateException("Applications not Loaded");
		
		Collection<AirlineInformation> results = new LinkedHashSet<AirlineInformation>();
		AirlineInformation thisAirline = getApp(get("airline.code"));
		if (thisAirline != null)
			results.add(thisAirline);
		
		for (Iterator<?> i = apps.values().iterator(); i.hasNext(); )
			results.add((AirlineInformation) i.next());
		
		return results;
	}
	
	public static ConnectionPool<java.sql.Connection> getJDBCPool() {
		return (JDBCPool) getObject(JDBC_POOL);
	}
	
	public static JedisPool getJedisPool() {
		return (JedisPool) getObject(JEDIS_POOL);
	}
}