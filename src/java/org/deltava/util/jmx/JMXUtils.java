// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.util.*;

import javax.management.*;
import java.lang.management.ManagementFactory;

import org.apache.logging.log4j.*;

/**
 * A utility class for registering JMX Objects.
 * @author Luke
 * @version 11.1
 * @since 10.2
 */

public class JMXUtils {
	
	private static final Logger log = LogManager.getLogger(JMXUtils.class);
	private static final Collection<String> _objNames = new HashSet<String>();
	
	// static class
	private JMXUtils() {
		super();
	}
	
	/**
	 * Registers a JMX object with the MBean server.
	 * @param name the JMX object name
	 * @param o the JMX object
	 */
	public static synchronized void register(String name, Object o) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName jmsName = new ObjectName(name);
			mbs.registerMBean(o, jmsName);
			_objNames.add(name);
			log.info("Registered JMX bean {}", jmsName.getCanonicalName());
		} catch (JMException jme) {
			log.atError().withThrowable(jme).log("Error registering {} - {}", name, jme.getMessage());
		}
	}
	
	/**
	 * Removes a JMX object from the the MBean server.
	 * @param name the JMX object name
	 */
	public static synchronized void remove(String name) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName jmsName = new ObjectName(name);
			mbs.unregisterMBean(jmsName);
			_objNames.remove(name);
			log.info("Unregistered JMX bean {}", jmsName.getCanonicalName());
		} catch (JMException jme) {
			log.atWarn().withThrowable(jme).log("Error unregistering {} - {}", name, jme.getMessage());
		}
	}
	
	/**
	 * Returns all registered JMX names.
	 * @return a List of names
	 */
	public static Collection<String> getNames() {
		return new ArrayList<String>(_objNames);
	}

	/**
	 * Removes all registered JMX object from the MBean server. 
	 */
	public static synchronized void clear() {
		Collection<String> names = new ArrayList<String>(_objNames); // clone since we modify the collection in the loop
		names.forEach(JMXUtils::remove);
	}
}