// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;

import org.deltava.beans.*;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to generate a Google Maps v2 Marker.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class MarkerTag extends GoogleMapEntryTag {

   private String _jsPointVarName;
   
   private String _label;
   private String _color;
   private GeoLocation _entry;
   private boolean _useMarker;
   
   /**
    * Sets the location of the marker.
    * @param loc the marker's location
    */
   public void setPoint(GeoLocation loc) {
      _entry = loc;
   }
   
	/**
	 * Forces the marker to be rendered using a marker image instead of a Google Earth icon.
	 * @param useMarker TRUE if a marker must be used, otherwise FALSE
	 */
	public void setMarker(boolean useMarker) {
		_useMarker = useMarker;
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
    * @see MarkerMapEntry#getIconColor()
    */
   public void setColor(String color) {
      _color = color;
   }
   
   /**
    * Sets the JavaScript point variable name. If this is specified a seperate GPoint variable
    * will be set.
    * @param varName the variable name
    */
   public void setPointVar(String varName) {
      _jsPointVarName = varName;
   }
   
   /**
    * Resets the tag's state variables.
    */
   public void release() {
      _jsPointVarName = null;
      _label = null;
      _color = null;
      _useMarker = false;
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
         if (_label == null) _label = mapInfo.getInfoBox();
         if ((_color == null) && (_entry instanceof MarkerMapEntry))
        	 _color = ((MarkerMapEntry) mapInfo).getIconColor();
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
         if ((_entry instanceof IconMapEntry) && !_useMarker) { 
        	 IconMapEntry mapInfo = (IconMapEntry) _entry;
        	 out.print(generateIconMarker(_entry, mapInfo.getPaletteCode(), mapInfo.getIconCode(), _label));
         } else
        	 out.print(generateMarker(_entry, _color, _label));
         
         out.print(';');
         
         // Write the point variable
         if (_jsPointVarName != null) {
            out.print("\nvar ");
            out.print(_jsPointVarName);
            out.print(" = new GLatLng(");
            out.print(StringUtils.format(_entry.getLatitude(), "##0.00000"));
            out.print(',');
            out.print(StringUtils.format(_entry.getLongitude(), "##0.00000"));
            out.print(");");
         }
         
         // Mark the JavaScript point variable as included
         if (_jsPointVarName != null)
            ContentHelper.addContent(pageContext, API_JS_NAME, _jsPointVarName);
         
         // Mark the JavaScript marker variable as included
         if (_jsVarName != null)
            ContentHelper.addContent(pageContext, API_JS_NAME, _jsVarName);
      } catch (Exception e) {
         throw new JspException(e);
      } finally {
         release();
      }
      
      return EVAL_PAGE;
   }
}