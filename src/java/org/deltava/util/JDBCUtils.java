// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.lang.reflect.Field;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * A utility class for JDBC stuff.
 * @author Luke
 * @version 2.8
 * @since 2.8
 */

public class JDBCUtils {
	
	private static final Logger log = Logger.getLogger(JDBCUtils.class);
	private static final String MYSQL_CON = "com.mysql.jdbc.ConnectionImpl";
	
	// Private constructor
	private JDBCUtils() {
		super();
	}

	/**
	 * Deregisters all JDBC drivers loaded by the current class loader. Unless this is done, the
	 * class loader will never be garbage collected and bad things will happen.
	 */
	public static synchronized void deregisterDrivers() {
		boolean driversFound = false;
		for (Enumeration<Driver> en = DriverManager.getDrivers(); en.hasMoreElements();) {
			Driver driver = en.nextElement();
			if (JDBCUtils.class.getClassLoader() == driver.getClass().getClassLoader()) {
				try {
					DriverManager.deregisterDriver(driver);
					driversFound = true;
					log.info("Deregistered JDBC driver " + driver.getClass().getName());
				} catch (Exception ex) {
					log.error("Error dregistering " + driver.getClass(), ex);
				}
			}
		}
		
		// Warn if no drivers found 
		if (!driversFound)
			log.warn("No JDBC drivers deregistered");
	}
	
	/**
	 * A nasty utility hack to clean up the mySQL statement cancelation timer
	 */
	public static synchronized void cleanMySQLTimer() {
		try {
			Class<?> c = Class.forName(MYSQL_CON);
			if (c.getClassLoader() == JDBCUtils.class.getClassLoader()) {
				Field f = c.getDeclaredField("cancelTimer");
				f.setAccessible(true);	
				Timer timer = (Timer) f.get(null);
				timer.cancel();
				log.info("Canceled mySQL cancelation timer");
			} else
				log.warn(MYSQL_CON + " not loaded by current ClassLoader");
		} catch (IllegalAccessException iae) {
			log.warn(MYSQL_CON + ".cancelTimer inaccessible");
		} catch (NoSuchFieldException nfe) {
			log.warn(MYSQL_CON + ".cancelTimer not found");
		} catch (ClassNotFoundException cnfe) {
			log.info("Not using MySQL JDBC driver");
		}
	}
}