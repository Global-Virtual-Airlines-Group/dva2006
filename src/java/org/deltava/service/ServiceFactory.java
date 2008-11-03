// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import org.deltava.util.ConfigLoader;

/**
 * A Factory to load Web Service configuration data.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class ServiceFactory {
   
   private static final Logger log = Logger.getLogger(ServiceFactory.class);

   // Private constructor
   private ServiceFactory() {
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
      try {
          SAXBuilder builder = new SAXBuilder();
          doc = builder.build(ConfigLoader.getStream(configXML));
      } catch (JDOMException je) {
          throw new IOException("XML Parse Error in " + configXML, je);
      }
      
      // Get the root element
      Element root = doc.getRootElement();
      if (root == null)
          throw new IOException("Empty XML Document");
      
      // Parse through the services
      for (Iterator i = root.getChildren("service").iterator(); i.hasNext(); ) {
          Element e = (Element) i.next();
          String svcID = e.getAttributeValue("id");
          String svcClassName = e.getAttributeValue("class");
          
          try {
              Class c = Class.forName(svcClassName);
              WebService ws = (WebService) c.newInstance();
              results.put(svcID.toLowerCase(), ws);
          } catch (ClassNotFoundException cnfe) {
              log.error("Cannot find class " + svcClassName + " for " + svcID);
          } catch (Exception ex) {
        	  log.error("Cannot load " + svcClassName + " - " + ex.getMessage());
          }
      }
      
      // Return results
      log.info("Loaded " + results.size() + " services");
      return results;
   }
}