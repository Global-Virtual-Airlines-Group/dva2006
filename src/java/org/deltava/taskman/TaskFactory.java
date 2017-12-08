// Copyright 2005, 2006, 2009, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.jdom2.*;
import org.jdom2.input.*;

import org.deltava.util.ConfigLoader;

/**
 * A utility class to load Scheduled Tasks from an XML configuration file.
 * @author Luke
 * @version 8.1
 * @since 1.0
 */

public class TaskFactory {
   
   private static final Logger log = Logger.getLogger(TaskFactory.class);

   // singleton
   private TaskFactory() {
	   super();
   }

   /**
    * Loads tasks from an XML configuration file.
    * @param configXML the file name
    * @return a Collection of Task objects
    * @throws IOException if an I/O error occurs
    */
   public static Collection<Task> load(String configXML) throws IOException {
      
      // Gracefully fail if no commands found
      if (configXML == null) {
          log.warn("No Scheduled Tasks loaded");
          return Collections.emptySet();
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

      // Parse through the tasks
      Collection<Task> results = new HashSet<Task>();
      for (Element e : root.getChildren("task")) {
         String id = e.getAttributeValue("id");
         String className = e.getChildTextTrim("class");
         
         // Instantiate the task and set the interval
         try {
            Class<?> c = Class.forName(className);
            Task t = (Task) c.getDeclaredConstructor().newInstance();
            t.setID(id);
            t.setEnabled(Boolean.valueOf(e.getAttributeValue("enabled")).booleanValue());
           	log.debug(id + " enabled = " + t.getEnabled());
            
            // Load the time
            Element te = e.getChild("time");
            if (te == null) {
            	log.warn("No time specified for " + c.getName());
            	t.setEnabled(false);
            } else
            	te.getChildren().forEach(tte -> t.setRunTimes(tte.getName(), tte.getTextNormalize()));

            results.add(t);
        } catch (ClassNotFoundException cnfe) {
            log.error("Cannot find class " + className);
        } catch (Exception ex) {
           log.error("Error loading " + className + " - " + ex.getMessage());
        }
      }

      log.info("Loaded " + results.size() + " tasks");
      return results;
   }
}