// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;
import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to generate a Google Maps Marker.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MarkerTag extends GoogleMapEntryTag {

   private String _label;
   private String _color;
   private GeoLocation _entry;
   
   /**
    * Sets the location of the marker.
    * @param loc the marker's location
    */
   public void setPoint(GeoLocation loc) {
      _entry = loc;
   }
   
   /**
    * Sets the label for this marker. This overrides any label provided by the point.
    * @param label the label HTML text
    * @see MapEntry#getInfoBox()
    */
   public void setLabel(String label) {
      _label = label;
   }
   
   /**
    * Sets the icon color for this marker. This overrides any color provided by the point.
    * @param color the icon color
    * @see MapEntry#getIconColor()
    */
   public void setColor(String color) {
      _color = color;
   }
   
   /**
    * Resets the tag's state variables.
    */
   public void release() {
      _label = null;
      _color = null;
      super.release();
   }
   
   /**
    * Renders the JSP tag, creating a Javascript line.
    * @return TagSupport.EVAL_PAGE always
    * @throws JspException
    */
   public int doEndTag() throws JspException {
      
      // Calculate if color or label need to be overridden
      if (_entry instanceof MapEntry) {
         MapEntry mapInfo = (MapEntry) _entry;
         if (_color == null) _color = mapInfo.getIconColor();
         if (_label == null) _label = mapInfo.getInfoBox();
      }
      
      JspWriter out = pageContext.getOut();
      try {
         // Assign to a variable if a name was provided, otherwise make an anonymous object
         if (_jsVarName != null) {
            out.print("var ");
            out.print(_jsVarName);
            out.print(" = ");
         }
         
         // Call the googleMarker function
         out.print(generateMarker(_entry, _color, _label));
         out.print(';');
      } catch (Exception e) {
         throw new JspException(e);
      }
      
      // Mark the JavaScript variable as included
      if (_jsVarName != null)
         ContentHelper.addContent(pageContext, API_JS_NAME, _jsVarName);
      
      // Release state and return
      release();
      return EVAL_PAGE;
   }
}