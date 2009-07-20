// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.input.*;

import org.deltava.util.ConfigLoader;

/**
 * A utility class to load Scheduled Tasks from an XML configuration file.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class TaskFactory {
   
   private static final Logger log = Logger.getLogger(TaskFactory.class);

   // singleton
   private TaskFactory() {
	   super();
   }

   public static Collection<Task> load(String configXML) throws IOException {
      
      // Gracefully fail if no commands found
      if (configXML == null) {
          log.warn("No ScheduledTasks loaded");
          return new HashSet<Task>();
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

      // Parse through the tasks
      Collection<Task> results = new HashSet<Task>();
      for (Iterator<?> i = root.getChildren("task").iterator(); i.hasNext(); ) {
         Element e = (Element) i.next();
         String id = e.getAttributeValue("id");
         String className = e.getChildTextTrim("class");
         
         // Instantiate the task and set the interval
         try {
            Class<?> c = Class.forName(className);
            Task t = (Task) c.newInstance();
            log.debug("Validated task " + c.getName());
            t.setID(id);
            t.setEnabled(Boolean.valueOf(e.getAttributeValue("enabled")).booleanValue());
            log.debug(id + " enabled = " + t.getEnabled());
            
            // Load the time
            Element te = e.getChild("time");
            if (te != null) {
            	for (Iterator<?> ti = te.getChildren().iterator(); ti.hasNext(); ) {
            		Element tte = (Element) ti.next();
            		t.setRunTimes(tte.getName(), tte.getTextNormalize());
            	}
            } else { 
            	log.warn("No time specified for " + c.getName());
            	t.setEnabled(false);
            }
            
            // Add to results
            results.add(t);
        } catch (ClassNotFoundException cnfe) {
            log.error("Cannot find class " + className);
        } catch (Exception ex) {
           log.error("Error loading " + className + " - " + ex.getMessage());
        }
      }

      // Return results
      log.info("Loaded " + results.size() + " tasks");
      return results;
   }
}