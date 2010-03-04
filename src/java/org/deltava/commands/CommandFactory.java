// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import org.deltava.util.*;

/**
 * A factory class to initalize the web command map.
 * @author Luke
 * @version 3.0
 * @since 1.0
 */

public class CommandFactory {

	private static final Logger log = Logger.getLogger(CommandFactory.class);

	private CommandFactory() {
		super();
	}

	/**
	 * Helper method to parse comma-delimited list of roles.
	 */
	private static Collection<String> getRoles(String roleNames) {
		if (StringUtils.isEmpty(roleNames))
			return Collections.singleton("*");

		// Build the roles
		Collection<String> results = new TreeSet<String>();
		results.addAll(StringUtils.split(roleNames, ","));
		return results;
	}

	/**
	 * Returns the initialized command objects.
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

		// Get the file
		InputStream is = ConfigLoader.getStream(configXML);

		// Create the builder and load the file into an XML in-memory document
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(is);
			is.close();
		} catch (JDOMException je) {
			throw new IOException("XML Parse Error in " + configXML, je);
		}

		// Get the root element
		Element root = doc.getRootElement();
		if (root == null)
			throw new IOException("Empty XML Document");

		// Parse through the commands
		Map<String, Command> results = new LinkedHashMap<String, Command>();
		List<?> cmds = root.getChildren("command");
		for (Iterator<?> i = cmds.iterator(); i.hasNext();) {
			Element e = (Element) i.next();
			String cmdID = e.getAttributeValue("id").trim();
			String cmdClassName = e.getChildTextTrim("class");

			// Check if the command ID is unique
			if (results.containsKey(cmdID))
				log.warn("Duplicate command ID " + cmdID);
			else {
				Command cmd = null;
				try {
					Class<?> c = Class.forName(cmdClassName);
					cmd = (Command) c.newInstance();
					if (log.isDebugEnabled())
						log.debug("Loaded command " + cmdID);

					// init the command
					cmd.init(cmdID, e.getChildTextTrim("name"));
					cmd.setRoles(getRoles(e.getChildText("roles")));

					// Save the command in the map
					results.put(cmdID.toLowerCase(), cmd);
					if (log.isDebugEnabled())
						log.debug("Initialized command " + cmdID);
				} catch (CommandException ce) {
					log.error("Error initializing " + cmdID + " - " + ce.getMessage());
				} catch (ClassNotFoundException cnfe) {
					log.error("Cannot find class " + cmdClassName + " for " + cmdID);
				} catch (NoClassDefFoundError ncde) {
					log.error("Cannot find class " + cmdClassName + " for " + cmdID);
				} catch (Exception ex) {
					log.error("Cannot start " + cmdID + " - " + ex.getClass().getName());
				}
			}
		}

		// Return the commands
		log.info("Loaded " + results.size() + " commands");
		return results;
	}
}