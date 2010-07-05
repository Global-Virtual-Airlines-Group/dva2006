// Copyright 2005, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Person;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to selectively display airport codes.
 * @author Luke
 * @version 3.2
 * @since 1.0 
 */

public class AirportFormatTag extends TagSupport {

   private Airport.Code _codeType = Airport.Code.IATA;
   private Airport _airport;

   /**
    * Sets the airport to display.
    * @param a the Airport to display
    * @see AirportFormatTag#setAirportCode(String)
    */
   public void setAirport(Airport a) {
      _airport = a;
   }

   /**
    * Sets the airport code to lookup.
    * @param code the case-insensitive airport code
    * @see AirportFormatTag#setAirport(Airport)
    */
   public void setAirportCode(String code) {
      setAirport(SystemData.getAirport(code));
   }

   /**
    * Sets the code type to display.
    * @param codeType a code type constant
    */
   public void setCode(String codeType) {
	   try {
		   _codeType = Airport.Code.valueOf(codeType.toUpperCase());
	   } catch (Exception e) {
		   // empty
	   }
   }

   /**
    * Release's the tag's state variables. This is required since the tag can take either a code or a bean and therefore
    * neither tag can be made required.
    */
   public void release() {
      super.release();
      _airport = null;
      _codeType = Airport.Code.IATA;
   }

   /**
    * Sets the tag's JSP context and loads the code type to display from the user's preferences.
    * @param ctxt the JSP context
    * @see Person#getAirportCodeType()
    */
   public final void setPageContext(PageContext ctxt) {
      super.setPageContext(ctxt);
      HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
      Principal user = req.getUserPrincipal();
      if (user instanceof Person) {
         Person p = (Person) user;
         _codeType = p.getAirportCodeType();
      }
   }

   /**
    * Writes the specific code type to the JSP output stream.
    * @return TagSupport.EVAL_PAGE
    * @throws JspException if an error occurs
    */
   public int doEndTag() throws JspException {

      // Check that an airport has been set
      if (_airport == null)
         return EVAL_PAGE;

      JspWriter out = pageContext.getOut();
      try {
         switch (_codeType) {
            case ICAO:
               out.print(_airport.getICAO());
               break;

            default:
            case IATA:
               out.print(_airport.getIATA());
         }
      } catch (Exception e) {
         throw new JspException(e);
      } finally {
    	  release();
      }

      return EVAL_PAGE;
   }
}