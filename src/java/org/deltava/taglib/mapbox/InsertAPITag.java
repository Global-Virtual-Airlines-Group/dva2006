// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import java.util.*;

import jakarta.servlet.jsp.*;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.json.JSONObject;

import org.deltava.beans.MapType;
import org.deltava.beans.system.*;
import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;


/**
 * A JSP tag to insert a link to the Mapbox JS API. 
 * @author Luke
 * @version 12.1
 * @since 12.0
 */

public class InsertAPITag extends TagSupport {
	
	private static final int MAJOR = 3;
	
	static final String API_JS_NAME = "$mapboxAPI$";
	static final String API_VER_ATTR_NAME = "$mapBoxAPIVersion$";
	
	private final Collection<String> _jsOnLoad = new LinkedHashSet<String>();
	
	private int _major = MAJOR;
	private int _minor;
	
	/**
	 * Sets the Google API version to pull down.
	 * @param ver the API major version
	 */
	public void setVersion(int ver) {
		_major = ver;
	}
	
	/**
	 * Sets the Javascript libraries to load after the API is loaded.
	 * @param jsList a comma-separated list of libraries
	 */
	public void setJs(String jsList) {
		_jsOnLoad.addAll(StringUtils.split(jsList, ","));
	}
	
	@Override
	public int doStartTag() throws JspException {
		_minor = SystemData.getInt("config.mapbox.v" + _major, 12);
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		
		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", API_JS_NAME))
			return EVAL_PAGE;
		
		// Build the Map context object
		String jsFileName = "mapBoxV" + String.valueOf(_major);
		JSONObject mco = new JSONObject();
		mco.put("IMG_PATH", SystemData.get("path.img"));
		mco.put("API", _major);
		mco.put("minor", _minor);
		mco.put("async", false);
		mco.put("type", "mapbox");
		mco.put("util", new JSONObject());
		mco.put("wx", new JSONObject());
		mco.put("seriesData", Collections.emptyMap());
		
		// Build base URL
		String baseURL = String.format("https://api.mapbox.com/mapbox-gl-js/v%d.%d.0/mapbox-gl", Integer.valueOf(_major), Integer.valueOf(_minor));
		String jsURL = baseURL + ".js";
		String cssURL = baseURL + ".css";
		
		// Insert the API version
		pageContext.setAttribute("$mapType", MapType.MAPBOX);
		pageContext.setAttribute(API_VER_ATTR_NAME, Integer.valueOf(_major), PageContext.REQUEST_SCOPE);
		try {
			JspWriter out = pageContext.getOut();
			
			// Write the MCO
			out.print("<script id=\"golgothaMCO\">");
			out.print("golgotha.maps = ");
			out.print(mco.toString());
			out.println(";</script>");
			
			// Load the Mapbox CSS and JS
			out.println(String.format("<link href=\"%s\" rel=\"stylesheet\">", cssURL));
			out.println(String.format("<script src=\"%s\"></script>", jsURL));
			
			// Load the Mapbox library 
			out.print("<script src=\"/");
			out.print(SystemData.get("path.js"));
			out.print("/v");
			out.print(VersionInfo.getFullBuild());
			out.print('/');
			out.print(jsFileName);
			out.println(".js\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", API_JS_NAME);
		ContentHelper.addContent(pageContext, "CSS", API_JS_NAME);
		ContentHelper.addContent(pageContext, "JS", jsFileName);
		ContentHelper.pushContent(pageContext, cssURL, "style");
		ContentHelper.pushContent(pageContext, jsURL, "script");
		ContentHelper.addCSP(pageContext, ContentSecurity.CONNECT, "*.tiles.mapbox.com", "api.mapbox.com", "events.mapbox.com");
		ContentHelper.addCSP(pageContext, ContentSecurity.WORKER, "blob:");
		ContentHelper.addCSP(pageContext, ContentSecurity.SCRIPT, "api.mapbox.com", "'unsafe-eval'");
		ContentHelper.addCSP(pageContext, ContentSecurity.IMG, "data:");
		ContentHelper.addCSP(pageContext, ContentSecurity.FONT, "data:");
		ContentHelper.addCSP(pageContext, ContentSecurity.STYLE, "api.mapbox.com");
		return EVAL_PAGE;
	}
}