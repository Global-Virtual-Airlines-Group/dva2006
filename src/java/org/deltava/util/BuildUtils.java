// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.Properties;

import org.apache.logging.log4j.*;

/**
 * A utility class to load program build data.
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

public class BuildUtils {
	
	private static final Logger log = LogManager.getLogger(BuildUtils.class);

	// static class
	private BuildUtils() {
		super();
	}

	/**
	 * Loads program build data.
	 * @param name the properties file name
	 * @return a Properties object
	 */
	public static Properties getBuildInfo(String name) {
		
		Properties p = new Properties();
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
			if (is == null) throw new FileNotFoundException("Cannot load " + name);
			p.load(is);
		} catch (IOException ie) {
			log.atError().withThrowable(ie).log(ie.getMessage());
		}
		
		return p;
	}
}