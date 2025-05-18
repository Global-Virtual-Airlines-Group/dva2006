// Copyright 2005, 2006, 2007, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import javax.servlet.jsp.*;

import org.deltava.taglib.ContentHelper;
import org.deltava.taglib.html.ElementTag;

/**
 * A JSP Tag to insert a DIV element to store a Map.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public class MapDIVTag extends ElementTag {
   
   private int _mapHeight;
   
   /**
    * Creates a new map DIV Tag.
    */
   public MapDIVTag() {
       super("div");
   }
   
   /**
    * Sets the preferred height of the map, assuming a screen height of 800 pixels. The actual
    * size of the map will be scaled relative to the client window.
    * @param h the preferred height in pixels
    */
   public void setHeight(int h) {
	   _mapHeight = Math.max(0, h);
   }
   
   /**
    * Releases the tag's state variables.
    */
   @Override
   public void release() {
      super.release();
      _mapHeight = 0;
   }
   
   @Override
   public int doStartTag() throws JspException {
      super.doStartTag();
      
      // Check for Google Maps API
      if (!ContentHelper.containsContent(pageContext, "JS", InsertAPITag.API_JS_NAME))
         throw new IllegalStateException("Google Maps API not included in request");
      
      // Get the API version
      Integer rawVersion = (Integer) pageContext.getAttribute(InsertAPITag.API_VER_ATTR_NAME, PageContext.REQUEST_SCOPE);
      if (rawVersion == null)
    	  rawVersion = Integer.valueOf(3);
      
      // Calculate height / width
      StringBuilder buf = new StringBuilder("width:100%;");
      if (_mapHeight > 0) {
    	  buf.append(" height:");
    	  buf.append(_mapHeight);
    	  buf.append("px;");
      }
      
      // Save the DIV size as a style and API version
      _data.setAttribute("class", "mapBoxV" + String.valueOf(rawVersion));
      _data.setAttribute("style", buf.toString());
      if (_mapHeight > 0)
    	  _data.setAttribute("h", String.valueOf(_mapHeight));
      
      return SKIP_BODY;
   }

   @Override
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