// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to generate a JavaScript array of Google Maps GMarkers.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MarkerArrayTag extends GoogleMapEntryTag {

   private Collection _entries;
   
   private String _color;
   private String _defaultColor;
   
   /**
    * Sets the icon color for these markers. This overrides any color provided by the points.
    * @param color the icon color
    * @see MapEntry#getIconColor()
    * @see MarkerArrayTag#setDefault(String)
    */
   public void setColor(String color) {
      _color = color;
   }
   
   /**
    * Sets the default icon color for these markers, if no color is specified by the override or
    * the markers themselves.
    * @param color the default icon color
    * @see MapEntry#getIconColor()
    * @see MarkerArrayTag#setColor(String)
    */
   public void setDefault(String color) {
      _defaultColor = color;
   }
   
   /**
    * Sets the points used to generate the array.
    * @param points a Collection of GeoLocations
    */
   public void setItems(Collection points) {
      _entries = points;
   }
   
   /**
    * Releases the tag's state variables.
    */
   public void release() {
      _color = null;
      _defaultColor = null;
      super.release();
   }
   
   /**
    * Renders the tag data to the JSP output stream, generating a JavaScript array with a number
    * of Google Maps markers.
    * @return TagSupport.EVAL_PAGE always
    * @throws JspException if a network error occurs
    */
   public int doEndTag() throws JspException {
      
      // Create the JavaScript array definition
      JspWriter out = pageContext.getOut();
      try {
         out.print("var ");
         out.print(_jsVarName);
         out.println(" = new Array();");
         
         // Create the markers
         for (Iterator i = _entries.iterator(); i.hasNext(); ) {
            GeoLocation entry = (GeoLocation) i.next();
            
            // Calculate the color
            String entryColor = _color;
            if ((_color == null) && (entry instanceof MapEntry)) {
               entryColor = ((MapEntry) entry).getIconColor();
            } else if (_color == null) {
               entryColor = _defaultColor;
            }
            
            // Calculate the label (if any)
            String entryLabel = (entry instanceof MapEntry) ? ((MapEntry) entry).getInfoBox() : null;
            
            // Generate the google marker and push it into the array
            out.print(_jsVarName);
            out.print(".push(");
            out.print(generateMarker(entry, entryColor, entryLabel));
            out.println(");");
         }
      } catch (Exception e) {
         throw new JspException(e);
      }
      
      // Mark the JavaScript variable as included
      ContentHelper.addContent(pageContext, API_JS_NAME, _jsVarName);
      
      // Release state and return
      release();
      return EVAL_PAGE;
   }
}