// Copyright 2005 Luke J. Kolin. All Rights Reserved.
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
 * @version 1.0
 * @since 1.0
 */

public class ServiceFactory {
   
   private static final Logger log = Logger.getLogger(ServiceFactory.class);

   // Private constructor
   private ServiceFactory() {
   }

   public static Map<String, String> load(String configXML) throws IOException {
      
      // Gracefully fail if no commands found
	   Map<String, String> results = new HashMap<String, String>();
      if (configXML == null) {
          log.warn("No Web Services loaded");
          return results;
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
      
      // Parse through the services
      for (Iterator i = root.getChildren("service").iterator(); i.hasNext(); ) {
          Element e = (Element) i.next();
          String svcID = e.getAttributeValue("id");
          String svcClassName = e.getAttributeValue("class");
          
          try {
              Class c = Class.forName(svcClassName);
              log.debug("Validated service " + c.getName());
          } catch (ClassNotFoundException cnfe) {
              log.error("Cannot find class " + svcClassName + " for " + svcID);
          }
          
          // Save the command in the map
          results.put(svcID.toLowerCase(), svcClassName);
      }
      
      // Return results
      log.info("Validated " + results.size() + " services");
      return results;
   }
}