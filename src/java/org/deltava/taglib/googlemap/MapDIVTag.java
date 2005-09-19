// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;
import javax.servlet.http.*;

import org.deltava.commands.CommandContext;

import org.deltava.taglib.ContentHelper;
import org.deltava.taglib.html.ElementTag;

/**
 * A JSP Tag to insert a DIV element to store a Google Map.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MapDIVTag extends ElementTag {
   
   private int _mapX;
   private int _mapY;
   private boolean _fixedSize;
   
   /**
    * Creates a new Form element Tag.
    */
   public MapDIVTag() {
       super("div");
       release();
   }
   
   /**
    * Marks the map as fixed size, using the same dimensions for all screen resolutions.
    * @param isFixed TRUE if dimensions should not be scaled, otherwise FALSE
    */
   public void setFixed(boolean isFixed) {
      _fixedSize = isFixed;
   }
   
   /**
    * Sets the default map width at 1024x768.
    * @param x the width in pixels
    */
   public void setX(int x) {
      _mapX = x;
   }
   
   /**
    * Sets the default map height at 1024x768.
    * @param y the height in pixels
    */
   public void setY(int y) {
      _mapY = y;
   }
   
   /**
    * Releases the tag's state variables.
    */
   public void release() {
      super.release();
      _fixedSize = false;
   }
   
   /**
    * Checks for the Google Maps API JavaScript tag, and loads the screen size from the user session.
    * @return TagSupport.SKIP_BODY
    * @throws JspException if the Google Maps JavaScript file is not included 
    */
   public int doStartTag() throws JspException {
      super.doStartTag();
      
      // Check for Google Maps API
      if (!ContentHelper.containsContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME))
         throw new IllegalStateException("Google Maps API not included in request");
      
      // Load the screen size
      HttpSession s = ((HttpServletRequest) pageContext.getRequest()).getSession(false);
      Integer sX = (Integer) s.getAttribute(CommandContext.SCREENX_ATTR_NAME);
      Integer sY = (Integer) s.getAttribute(CommandContext.SCREENY_ATTR_NAME);
      if ((!_fixedSize) && (sX != null) && (sY != null)) {
         int screenX = sX.intValue();
         int screenY = sY.intValue();
         
         // Adjust the map size proportionally between the screen size and 1024/768
         _mapX *= (screenX / 1024);
         _mapY *= (screenY / 768);
      }
      
      // Save the screen size as a style
      _attrs.put("style", "height:" + _mapX + "px; width:" + _mapY + "px");
      
      // Skip Body
      return SKIP_BODY;
   }

   /**
    * Renders the Google Map DIV tag to the JSP output stream.
    * @return TagSupport.EVAL_PAGE
    * @throws JspException if an error occurs
    */
   public int doEndTag() throws JspException {
      try {
         openHTML(false);
         _out.print(" />");
      } catch (Exception e) {
         throw new JspException(e);
      } finally {
         release();
      }

      return EVAL_PAGE;
   }
}