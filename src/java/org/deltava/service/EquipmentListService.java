// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;
import javax.servlet.http.*;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to return available equipment types. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EquipmentListService extends WebService {

   /**
    * Executes the Web Service, returning a list of equipment types.
    * @param ctx the Web Service context
    * @return the HTTP status code
    * @throws ServiceException if an error occurs
    */
   public int execute(ServiceContext ctx) throws ServiceException {

      // Get the equipment types and sort them
      List eqTypes = (List) SystemData.getObject("eqtypes");
      Collections.sort(eqTypes);
      
      // Generate the data element
      Document doc = new Document();
      Element re = new Element("wsdata");
      doc.setRootElement(re);
      
      // Create the list
      Element le  = new Element("eqTypes");
      re.addContent(le);
      for (Iterator i = eqTypes.iterator(); i.hasNext(); ) {
         String eqType = (String) i.next();
         Element e = new Element("eqType");
         e.addContent(eqType);
         le.addContent(e);
      }
      
      // Dump the XML to the output stream
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      try {
         ctx.getResponse().setContentType("text/xml");
         ctx.println(xmlOut.outputString(doc));
         ctx.commit();
      } catch (IOException ie) {
         throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
      }

      // Write result code
      return HttpServletResponse.SC_OK;
   }
}