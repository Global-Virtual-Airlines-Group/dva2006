// Copyright 2005, 2008, 2009, 2012, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.deltava.util.ConfigLoader;

/**
 * A Factory to load Web Service configuration data.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class ServiceFactory {

	private static final Logger log = LogManager.getLogger(ServiceFactory.class);

	// Private constructor
	private ServiceFactory() {
		super();
	}

	public static Map<String, WebService> load(String configXML) throws IOException {

		// Gracefully fail if no commands found
		Map<String, WebService> results = new HashMap<String, WebService>();
		if (configXML == null) {
			log.error("No Web Services loaded");
			return results;
		}

		// Create the builder and load the file into an XML in-memory document
		Document doc = null;
		try (InputStream in = ConfigLoader.getStream(configXML)) {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(in);
		} catch (JDOMException je) {
			throw new IOException("XML Parse Error in " + configXML, je);
		}

		// Get the root element
		Element root = doc.getRootElement();
		if (root == null)
			throw new IOException("Empty XML Document");

		// Parse through the services
		for (Element e : root.getChildren("service")) {
			String svcID = e.getAttributeValue("id");
			String svcClassName = e.getAttributeValue("class");

			try {
				Class<?> c = Class.forName(svcClassName);
				WebService ws = (WebService) c.getDeclaredConstructor().newInstance();
				results.put(svcID.toLowerCase(), ws);
			} catch (ClassNotFoundException cnfe) {
				log.error("Cannot find class {} for {}", svcClassName, svcID);
			} catch (Exception ex) {
				log.error("Cannot load {} -{}", svcClassName, ex.getMessage());
			}
		}

		log.info("Loaded {} services", Integer.valueOf(results.size()));
		return results;
	}
}