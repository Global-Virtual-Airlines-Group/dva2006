// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2020, 2021, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.apache.logging.log4j.*;
import org.deltava.beans.MapType;
import org.deltava.beans.system.*;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert a JavaScript link to the Google Maps API.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class InsertGoogleAPITag extends TagSupport {
	
	private static final Logger log = LogManager.getLogger(API.class);

	static final String API_VER_ATTR_NAME = "$googleMapAPIVersion$";
	private static final List<String> CYCLES = List.of("weekly", "quarterly");
	
	private static final int MIN_API_VERSION = 3;
	private static final String DEFAULT_CYCLE = "quarterly";
	
	private static final String V3_API_URL = "maps.googleapis.com/maps/api/js?v=";
	
	private int _majorVersion = MIN_API_VERSION;
	private String _minorVersion;
	private String _cycle;
	private String _cb;
	private final Collection<String> _libraries = new LinkedHashSet<String>();
	private final Collection<String> _jsOnLoad = new LinkedHashSet<String>();
	
	private boolean _isAnonymous;
	
	/**
	 * Sets the Google API version to pull down.
	 * @param ver the API major version
	 */
	public void setVersion(int ver) {
		_majorVersion = Math.max(MIN_API_VERSION, ver);
	}

	/**
	 * Sets the Google API revision to pull down.
	 * @param ver the API minor version.
	 */
	public void setMinor(String ver) {
		_minorVersion = ver;
	}
	
	/**
	 * Sets the Google Maps release cycle to use.
	 * @param c the cycle name
	 */
	public void setCycle(String c) {
		if (c != null) {
			String lc = c.toLowerCase();
			_cycle = CYCLES.contains(lc) ? lc : null;
		}
	}
	
	/**
	 * Sets the Google Maps v3 libraries to load.
	 * @param libList a comma-separated list of libraries
	 */
	public void setLibraries(String libList) {
		_libraries.addAll(StringUtils.split(libList, ","));
	}
	
	/**
	 * Sets the Javascript libraries to load after the API is loaded.
	 * @param jsList a comma-separated list of libraries
	 */
	public void setJs(String jsList) {
		_jsOnLoad.addAll(StringUtils.split(jsList, ","));
	}
	
	/**
	 * Sets the JavaScript callback function to be called when the Google Maps API has completed loading.
	 * @param cb the callback function name
	 */
	public void setCallback(String cb) {
		_cb = cb;
	}
	
	@Override
	public void setPageContext(PageContext ctx) {
		super.setPageContext(ctx);
		HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
		_isAnonymous = (req.getUserPrincipal() == null);
	}

	@Override
	public void release() {
		super.release();
		_libraries.clear();
		_majorVersion = MIN_API_VERSION;
		_minorVersion = null;
		_cycle = null;
		_cb = null;
	}

	/**
	 * Renders the JSP tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if no Google Maps API key defined
	 */
	@Override
	public int doEndTag() throws JspException {

		// Check if we've already included the content
		if (ContentHelper.containsContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME))
			return EVAL_PAGE;
		
		// Write log entry
		HttpServletRequest hreq = (HttpServletRequest) pageContext.getRequest();
		StringBuffer urlBuf = hreq.getRequestURL();
		if (!StringUtils.isEmpty(hreq.getQueryString()))
			urlBuf.append('?').append(hreq.getQueryString());
		
		log.info("{} {} {}", urlBuf, hreq.getRemoteUser(), (_cb != null) ? "async" : "sync");
		
		// Translate stable/release v3 to minor version
		APIUsage.track(APIUsage.Type.DYNAMIC, _isAnonymous);
		if ((_majorVersion == 3) && (_minorVersion == null) && (_cycle == null))
			_cycle = DEFAULT_CYCLE;
		
		// Build the Map context object
		String jsFileName = "googleMapsV" + String.valueOf(_majorVersion);
		JSONObject mco = new JSONObject();
		mco.put("IMG_PATH", SystemData.get("path.img"));
		mco.put("API", _majorVersion);
		mco.put("async", Boolean.valueOf(_cb != null));
		mco.put("type", "google");
		mco.put("util", new JSONObject());
		mco.putOpt("cycle", _cycle);
		mco.putOpt("minor", _minorVersion);
		mco.put("seriesData", Collections.emptyMap());
		if (_cb != null) {
			mco.put("path", String.format("%s/v%s", SystemData.get("path.js"), VersionInfo.getFullBuild()));
			mco.put("library", String.format("%s.js", jsFileName));
			mco.put("callback", _cb);
			mco.put("jsLoad", _jsOnLoad);
		}
		
		// Insert the API version
		ContentHelper.addContent(pageContext, "JS", jsFileName);
		pageContext.setAttribute("$mapType", MapType.GOOGLE);
		pageContext.setAttribute(API_VER_ATTR_NAME, Integer.valueOf(_majorVersion), PageContext.REQUEST_SCOPE);
		try {
			JspWriter out = pageContext.getOut();
			
			// Write the MCO
			out.print("<script id=\"golgothaMCO\">");
			out.print("golgotha.maps = ");
			out.print(mco.toString());
			out.println(";</script>");
			
			// Load the Google API
			out.print("<script");
			if (_cb != null)
				out.print(" async");
			
			out.print(" src=\"https://");
			out.print(V3_API_URL);
			if (_cycle == null) {
				out.print(String.valueOf(_majorVersion));
				if (_minorVersion != null) {
					out.print('.');
					out.print(_minorVersion);
				}
			} else
				out.print(_cycle);
			
			if (!_libraries.isEmpty()) {
				out.print("&libraries=");
				for (Iterator<String> i = _libraries.iterator(); i.hasNext(); ) {
					out.print(i.next());
					if (i.hasNext())
						out.print(',');
				}
			}
			
			out.print("&key=");
			out.print(SystemData.get("security.key.googleMaps." + (_isAnonymous ? "anon" : "auth")));
			if (_cb != null)
				out.print("&loading=async");
			out.print("&callback=golgotha.util.mapAPILoaded\"></script>");
			
			// Add JS support file
			if (_cb == null) {
				out.print("<script src=\"");
				out.print(SystemData.get("path.js"));
				out.print("/v");
				out.print(VersionInfo.getFullBuild());
				out.print('/');
				out.print(jsFileName);
				out.println(".js\"></script>");
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", GoogleMapEntryTag.API_JS_NAME);
		return EVAL_PAGE;
	}
}