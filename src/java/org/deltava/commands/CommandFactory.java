// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2015, 2016, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.deltava.util.*;

/**
 * A factory class to initalize the web command map.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class CommandFactory {

	private static final Logger log = LogManager.getLogger(CommandFactory.class);

	private CommandFactory() {
		super();
	}

	/*
	 * Helper method to parse comma-delimited list of roles.
	 */
	private static Collection<String> getRoles(String roleNames) {
		return StringUtils.isEmpty(roleNames) ? Collections.singleton("*") : new TreeSet<String>(StringUtils.split(roleNames, ","));
	}

	/**
	 * Returns the initialized command objects.
	 * @param configXML the name of the command configuration file
	 * @return a Map of initialized command objects
	 * @throws IOException if an I/O error occurs
	 * @see Command#init(String, String)
	 */
	public static Map<String, Command> load(String configXML) throws IOException {
		// Gracefully fail if no commands found
		if (configXML == null) {
			log.warn("No Commands loaded");
			return Collections.emptyMap();
		}

		// Create the builder and load the file into an XML in-memory document
		Document doc = null;
		try (InputStream is = ConfigLoader.getStream(configXML)) {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(is);
		} catch (JDOMException je) {
			throw new IOException("XML Parse Error in " + configXML, je);
		}

		// Get the root element
		Element root = doc.getRootElement();
		if (root == null)
			throw new IOException("Empty XML Document");

		// Parse through the commands
		Map<String, Command> results = new LinkedHashMap<String, Command>();
		List<Element> cmds = root.getChildren("command");
		for (Element e : cmds) {
			String cmdID = e.getAttributeValue("id").trim();
			String cmdClassName = e.getChildTextTrim("class");

			// Check if the command ID is unique
			if (results.containsKey(cmdID))
				log.warn("Duplicate command ID {}", cmdID);
			else {
				Command cmd = null;
				try {
					Class<?> c = Class.forName(cmdClassName);
					cmd = (Command) c.getDeclaredConstructor().newInstance();
					log.debug("Loaded command {}", cmdID);

					// init the command
					cmd.init(cmdID, e.getChildTextTrim("name"));
					cmd.setRoles(getRoles(e.getChildText("roles")));

					// Save the command in the map
					results.put(cmdID.toLowerCase(), cmd);
					log.debug("Initialized command {}", cmdID);
				} catch (ClassNotFoundException | NoClassDefFoundError cnfe) {
					log.error("Cannot find class {} for {}", cmdClassName, cmdID);
				} catch (Exception ex) {
					log.error("Cannot start {} - {}", cmdID, ex.getClass().getName());
				}
			}
		}

		log.info("Loaded {} commands", Integer.valueOf(results.size()));
		return results;
	}
}