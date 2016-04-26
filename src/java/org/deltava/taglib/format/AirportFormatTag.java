// Copyright 2005, 2009, 2010, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;

import org.deltava.beans.Person;
import org.deltava.beans.schedule.Airport;

/**
 * A JSP tag to selectively display airport codes.
 * @author Luke
 * @version 7.0
 * @since 1.0 
 */

public class AirportFormatTag extends UserSettingsTag {

   private Airport.Code _codeType = Airport.Code.IATA;
   private Airport _airport;

   /**
    * Sets the airport to display.
    * @param a the Airport to display
    */
   public void setAirport(Airport a) {
      _airport = a;
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
    * Sets the tag's JSP context and loads the code type to display from the user's preferences.
    * @param ctxt the JSP context
    * @see Person#getAirportCodeType()
    */
   @Override
   public final void setPageContext(PageContext ctxt) {
      super.setPageContext(ctxt);
      _codeType = (_user != null) ? _user.getAirportCodeType() : Airport.Code.IATA;
   }

   /**
    * Writes the specific code type to the JSP output stream.
    * @return TagSupport.EVAL_PAGE
    * @throws JspException if an error occurs
    */
   @Override
   public int doEndTag() throws JspException {

      // Check that an airport has been set
      if (_airport == null)
         return EVAL_PAGE;

      try {
    	  JspWriter out = pageContext.getOut();
    	  out.print((_codeType == Airport.Code.IATA) ? _airport.getIATA() : _airport.getICAO()); 
      } catch (Exception e) {
         throw new JspException(e);
      } finally {
    	  release();
      }

      return EVAL_PAGE;
   }
}