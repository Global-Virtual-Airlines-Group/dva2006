// Copyright 2007, 2010, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to set the base layer on a  Google Map.
 * @author Luke
 * @version 5.1
 * @since 2.1
 */

public class MapTypeTag extends GoogleMapEntryTag {
	
	private static final String[] MAP_CODES = {"MAP", "SAT", "TRN"};
	private static final String[] V2_MAP_OPTS = {"G_NORMAL_MAP", "G_SATELLITE_MAP", "G_PHYSICAL_MAP"};
	private static final String[] V3_MAP_OPTS = {"ROADMAP", "SATELLITE", "TERRAIN"};
	
	private static final String V2_DEFAULT = "G_SATELLITE_MAP";
	private static final String V3_DEFAULT = "SATELLITE";
	
	private String _mapVar;
	private String _mapType;
	private String _default;

	/**
	 * Sets the name of the Google Map JavaScript object.
	 * @param varName the variable name
	 */
	public void setMap(String varName) {
		_mapVar = varName;
	}

	/*
	 * Helper method to convert short codes into map type object names.
	 */
	private String convertType(String type) {
		if (type == null)
			return null;
		
		String[] MAP_OPTS = (getAPIVersion() == 3) ? V3_MAP_OPTS : V2_MAP_OPTS;
		String mapType = type.toUpperCase();
		int ofs = StringUtils.arrayIndexOf(MAP_OPTS, mapType, -1);
		if (ofs != -1)
			return mapType;
		
		ofs = StringUtils.arrayIndexOf(MAP_CODES, mapType, -1);
		return (ofs != -1) ? MAP_OPTS[ofs] : null;
	}
	
	/**
	 * Sets the Map type to display.
	 * @param mapType the map type
	 */
	public void setType(String mapType) {
		_mapType = convertType(mapType);
	}
	
	/**
	 * Sets the default map type if no valid type is selected.
	 * @param defType the default map type
	 */
	public void setDefault(String defType) {
		String type = convertType(defType);
		if (type != null)
			_default = type;
	}
	
	/**
	 * Resets the tag's state variables.
	 */
	@Override
	public void release() {
		_default = null;
		_mapType = null;
		super.release();
	}
	
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		if (_default == null)
			_default = (getAPIVersion() == 3) ? V3_DEFAULT : V2_DEFAULT;
			
		return SKIP_BODY;
	}
	
	/**
	 * Creates the JavaScript to set the map type.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if a network error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
	
		JspWriter out = pageContext.getOut();
		try {
			out.print(_mapVar);
			if (getAPIVersion() == 3)
				out.print(".setMapTypeId(google.maps.MapTypeId.");
			else
				out.print(".setMapType(");
			
			out.print((_mapType == null) ? _default : _mapType);
			out.println(");");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}