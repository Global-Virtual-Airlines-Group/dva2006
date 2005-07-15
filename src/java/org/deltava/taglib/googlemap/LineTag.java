// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.text.DecimalFormat;

import javax.servlet.jsp.*;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to generate a Google Maps Polyline.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LineTag extends GoogleMapEntryTag {
   
   private static final DecimalFormat _nf = new DecimalFormat("0.00");

   private String _srcJsVarName;
   private String _color = "#000000";
   
   private int _width = 1;
   private double _transparency = 1.0;
   
   /**
    * Sets the JavaScript variable name for the line source.
    * @param varName the variable name
    */
   public void setSrc(String varName) {
      _srcJsVarName = varName;
   }
   
   /**
    * Sets the line's color.
    * @param color the color as an HTML #RRGGBB value
    */
   public void setColor(String color) {
      _color = color;
   }
   
   /**
    * Sets the line's width.
    * @param width the width in pixels
    */
   public void setWidth(int width) {
      _width = width;
   }
   
   /**
    * Sets the line's transparency.
    * @param trans the transparancy, as a percentage
    */
   public void setTransparency(double trans) {
      if ((trans >= 0) && (trans <= 1))
         _transparency = trans;
   }
   
   /**
    * Releases the tag's state variables.
    */
   public void release() {
      _width = 1;
      _transparency = 1.0;
      _color = "#000000";
      super.release();
   }
   
   /**
    * Validates that the JavaScript source variable exists.
    * @return TagSupport.SKIP_BODY always
    * @throws IllegalStateException if the JavaScript definitions are not in place
    * @see GoogleMapEntryTag#doStartTag()
    */
   public int doStartTag() throws JspException {
      super.doStartTag();
      
      // Check that the source variable has been defined
      if (!ContentHelper.containsContent(pageContext, API_JS_NAME, _srcJsVarName))
         throw new IllegalStateException(_srcJsVarName + " not defined in JavaScript");
      
      return SKIP_BODY;
   }
   
   /**
    * Renders the tag to the JSP output stream, generating a Google Maps Polyline.
    * @return TagSupport.EVAL_PAGE always
    * @throws JspException if an error occurs
    */
   public int doEndTag() throws JspException {
      
      JspWriter out = pageContext.getOut();
      try {
         // Assign to a variable if a name was provided, otherwise make an anonymous object
         if (_jsVarName != null) {
            out.print("var ");
            out.print(_jsVarName);
            out.print(" = ");
         }
         
         // Generate the line
         out.print("new GPolyline(");
         out.print(_srcJsVarName);
         out.print(",\'");
         out.print(_color);
         out.print("\',");
         out.print(String.valueOf(_width));
         out.print(',');
         synchronized (_nf) {
         	out.print(_nf.format(_transparency));
         }
         
         out.print(");");
      } catch (Exception e) {
         throw new JspException(e);
      }
      
      // Release state and return
      release();
      return EVAL_PAGE;
   }
}