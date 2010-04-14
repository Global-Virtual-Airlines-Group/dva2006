// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert weather.com Series List data into a JSP page. 
 * @author Luke
 * @version 3.0
 * @since 3.0
 */

public class InsertWeatherTag extends TagSupport {
	
	public static final String USAGE_ATTR_NAME = "$wxLayerUsage$";
	private static final Collection<String> _layers = new LinkedHashSet<String>();
	
	/**
	 * Sets the layers to download image series data for.
	 * @param layers a comma-delimited list of weather imagery layers
	 */
	public void setLayers(String layers) {
		_layers.addAll(StringUtils.split(layers, ","));
	}
	
	/**
	 * Executed before the Tag is rendered. This will check for the presence of required JavaScript files in the
	 * request. Tags that do not require this check can override this method.
	 * @return TagSupport.SKIP_BODY always
	 * @throws IllegalStateException if the Google Maps API or googleMaps.js not included in request
	 */
	public int doStartTag() throws JspException {
		super.doStartTag();
		
		// Check if we've already included the Google API
		if (!ContentHelper.containsContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME))
			throw new JspException("Google Maps API not loaded");
		
		// Check for Google Maps support JavaScript
		if (!ContentHelper.containsContent(pageContext, "JS", "googleMaps"))
			throw new JspException("googleMaps.js not included in request");
		
		// Check for Google Maps support JavaScript
		if (!ContentHelper.containsContent(pageContext, "JS", "acarsMapWX"))
			throw new JspException("acarsMapWX.js not included in request");
		
		return SKIP_BODY;
	}
	
	public int doEndTag() throws JspException {
		
		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", USAGE_ATTR_NAME))
			return EVAL_PAGE;
		
		String host = SystemData.get("weather.tileHost");
		if (StringUtils.isEmpty(host))
			return EVAL_PAGE;
		else if (host.indexOf('%') != -1)
			host = host.replace("%", "");

		JspWriter out = pageContext.getOut();
		try {
			out.print("<script type=\"text/javascript\" src=\"http://");
			out.print(host);
			out.print("/TileServer/jserieslist.do?function=loadSeries&amp;id=wx&amp;type=");
			out.print(StringUtils.listConcat(_layers, ","));
			out.println("\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", USAGE_ATTR_NAME);
		return EVAL_PAGE;
	}
}