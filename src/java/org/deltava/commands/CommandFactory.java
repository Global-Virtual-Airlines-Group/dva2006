package org.deltava.commands;

import java.io.*;
import java.util.*;

import javax.servlet.*;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import org.deltava.util.ConfigLoader;

/**
 * A factory class to initalize a web command map.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class CommandFactory {

    private static final Logger log = Logger.getLogger(CommandFactory.class);
    
    private CommandFactory() {
    }
    
    /**
     * Helper method to parse comma-delimited list of roles.
     */
    private static List getRoles(String roleNames) {
        if (roleNames == null) {
            List results = new ArrayList();
            results.add("*");
            return results;
        }
        
        // Loop through the roles
        Set results = new TreeSet();
        StringTokenizer tkns = new StringTokenizer(roleNames, ",");
        while (tkns.hasMoreTokens())
            results.add(tkns.nextToken().trim());
        
        // Return the roles
        return new ArrayList(results);
    }
    
    /**
     * Returns the initialized command objects.
     * @return a Map of initialized command objects
     * @throws IOException if an I/O error occurs
     * @see Command#init(String, String)
     */
    public static Map load(String configXML, ServletContext sc) throws IOException {
        // Gracefully fail if no commands found
        if (configXML == null) {
            log.warn("No Commands loaded");
            return Collections.EMPTY_MAP;
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
            IOException ie = new IOException("XML Parse Error in " + configXML);
            ie.initCause(je);
            throw ie;
        }
        
        // Get the root element
        Element root = doc.getRootElement();
        if (root == null)
            throw new IOException("Empty XML Document");
        
        // Parse through the commands
        Map results = new HashMap();
        List cmds = root.getChildren("command");
        for (Iterator i = cmds.iterator(); i.hasNext(); ) {
            Element e = (Element) i.next();
            String cmdID = e.getAttributeValue("id");
            String cmdClassName = e.getChildTextTrim("class");
            
            Command cmd = null;
            try {
                Class c = Class.forName(cmdClassName);
                cmd = (Command) c.newInstance();
                log.debug("Loaded command " + cmdID);
                
                // init the command
                cmd.setContext(sc);
                cmd.init(cmdID, e.getChildTextTrim("name"));
                cmd.setRoles(getRoles(e.getChildText("roles")));
                
                log.debug("Initialized command " + cmdID);
            } catch (CommandException ce) {
                log.error("Error initializing " + cmdID + " - " + ce.getMessage());
            } catch (ClassNotFoundException cnfe) {
                log.error("Cannot find class " + cmdClassName + " for " + cmdID);
            } catch (Exception ex) {
                log.error("Cannot start " + cmdID + " - "  + ex.getClass().getName());
            }
            
            // Save the command in the map
            results.put(cmdID.toLowerCase(), cmd);
        }
        
        // Return the commands
        log.info("Loaded " + results.size() + " commands");
        return results;
    }
}