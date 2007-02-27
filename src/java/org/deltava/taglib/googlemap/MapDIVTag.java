// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;
import javax.servlet.http.*;

import org.deltava.commands.CommandContext;

import org.deltava.taglib.ContentHelper;
import org.deltava.taglib.html.ElementTag;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to insert a DIV element to store a Google Map.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MapDIVTag extends ElementTag {
   
   private int _mapX;
   private int _mapWidth;
   
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
    * Sets the default map width.
    * @param width the width in pixels at 1024x768, or a percentage of the parent element
    * @throws IllegalArgumentException if width is non-numeric
    */
   public void setX(String width) {
	   if ((width != null) && (width.endsWith("%"))) {
		   _mapWidth = StringUtils.parse(width.substring(0, width.length() - 1), 100);
		   if ((_mapWidth < 1) || (_mapWidth > 100))
			   _mapWidth = 100;
	   } else {
		   _mapX = StringUtils.parse(width, 0);
		   if (_mapX == 0)
			   throw new IllegalArgumentException("Invalid map DIV width - " + width);
	   }
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
      _mapX = 0;
      _mapWidth = 0;
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
      Integer sX = (s == null) ? null : (Integer) s.getAttribute(CommandContext.SCREENX_ATTR_NAME);
      Integer sY = (s == null) ? null : (Integer) s.getAttribute(CommandContext.SCREENY_ATTR_NAME);
      if ((!_fixedSize) && (sX != null) && (sY != null)) {
         int screenX = sX.intValue();
         int screenY = sY.intValue();
         
         // Adjust the map size proportionally between the screen size and 1024/768
         _mapX *= (screenX / 1024.0);
         _mapY *= (screenY / 768.0);
      }
      
      // Save the DIV size as a style
      if (_mapWidth > 0)
    	  _data.setAttribute("style", "height:" + _mapY + "px; width:" + _mapWidth + "%");
      else
    	  _data.setAttribute("style", "height:" + _mapY + "px; width:" + _mapX + "px");
      
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
         _out.print(_data.open(true, false));
         _out.print(_data.close());
      } catch (Exception e) {
         throw new JspException(e);
      } finally {
         release();
      }

      return EVAL_PAGE;
   }
}