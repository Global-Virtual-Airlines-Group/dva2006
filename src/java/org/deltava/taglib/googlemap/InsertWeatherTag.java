// Copyright 2010, 2011, 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert weather.com Series List data into a JSP page. 
 * @author Luke
 * @version 5.2
 * @since 3.0
 */

public class InsertWeatherTag extends WeatherJSTag {
	
	private static final String USAGE_ATTR_NAME = "$wxLayerUsage$";
	
	private int _max = 0;
	private final Collection<String> _layers = new TreeSet<String>();
	
	/**
	 * Constructor. Sets the default function name.
	 */
	public InsertWeatherTag() {
		super("loadSeries");
	}
	
	/**
	 * Sets the layers to download image series data for.
	 * @param layers a comma-delimited list of weather imagery layers
	 */
	public void setLayers(String layers) {
		_layers.addAll(StringUtils.split(layers, ","));
	}
	
	/**
	 * Sets the maximum number of series entries to load per type.
	 * @param max the maximum number of series, or zero for all
	 */
	public void setMax(int max) {
		_max = Math.max(0, max);
	}
	
	/**
	 * Releases the tag's state data.
	 */
	@Override
	public void release() {
		super.release();
		_layers.clear();
	}
	
	/**
	 * Executed when tag is rendered. Creates a JavaScript block to dynamically load a weather.com
	 * image series list.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		
		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", USAGE_ATTR_NAME))
			return EVAL_PAGE;
		
		String host = SystemData.get("weather.tileHost");
		if (StringUtils.isEmpty(host) || _layers.isEmpty())
			return EVAL_PAGE;
		else if (host.indexOf('%') != -1)
			host = host.replace("%", "");

		try {
			JspWriter out = pageContext.getOut();
			out.print("<script id=\"ginsuLoader\" src=\"http://");
			out.print(host);
			out.print("/TileServer/jserieslist.do?function=");
			out.print(_jsFunc);
			out.print("&amp;id=wx&amp;type=");
			out.print(StringUtils.listConcat(_layers, ","));
			if (_max > 0) {
				out.print("&amp;count=");
				out.print(_max);
			}
			
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