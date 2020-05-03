// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import org.deltava.beans.system.VersionInfo;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert a JavaScript link to the Google Maps API.
 * @author Luke
 * @version 8.4
 * @since 1.0
 */

public class InsertGoogleAPITag extends TagSupport {

	static final String API_VER_ATTR_NAME = "$googleMapAPIVersion$";
	private static final List<String> CYCLES = List.of("weekly", "quarterly");
	
	private static final int MIN_API_VERSION = 3;
	private static final String DEFAULT_CYCLE = "quarterly";
	
	private static final String V3_API_URL = "maps.googleapis.com/maps/api/js?v=";
	
	private int _majorVersion = MIN_API_VERSION;
	private String _minorVersion;
	private String _cycle;
	private final Collection<String> _libraries = new LinkedHashSet<String>();
	
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
	 * @param libList a comma-separated list of libraries.
	 */
	public void setLibraries(String libList) {
		_libraries.addAll(StringUtils.split(libList, ","));
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
		
		// Translate stable/release v3 to minor version
		APIUsage.track(APIUsage.Type.DYNAMIC, _isAnonymous);
		if ((_majorVersion == 3) && (_minorVersion == null) && (_cycle == null))
			_cycle = DEFAULT_CYCLE;
		
		// Insert the API version
		pageContext.setAttribute(API_VER_ATTR_NAME, Integer.valueOf(_majorVersion), PageContext.REQUEST_SCOPE);
		try {
			JspWriter out = pageContext.getOut();
			out.print("<script src=\"https://");
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
			out.print(SystemData.get("security.key.googleMaps"));
			out.println("\"></script>");
			
			// Build the Map context object
			JSONObject mco = new JSONObject();
			mco.put("IMG_PATH", SystemData.get("path.img"));
			mco.put("API", _majorVersion);
			mco.putOpt("cycle", _cycle);
			mco.putOpt("minor", _minorVersion);
			mco.put("protocol", pageContext.getRequest().isSecure() ? "https" : "http");
			mco.put("tileHost", SystemData.get("weather.tileHost"));
			mco.put("seriesData", Collections.emptyMap());
			mco.put("wxHost", SystemData.get("weather.apiHost"));
			JSONObject mkeys = new JSONObject();
			mkeys.put("wu", SystemData.get("security.key.wunderground"));
			mkeys.put("twc", SystemData.get("security.key.twc"));
			mco.put("keys", mkeys);
			
			// Init common code
			out.print("<script id=\"golgothaMCO\">");
			out.print("golgotha.maps = ");
			out.print(mco.toString());
			out.println(";</script>");
			
			// Add JS support file
			String jsFileName = "googleMapsV" + String.valueOf(_majorVersion);
			out.print("<script src=\"");
			out.print(SystemData.get("path.js"));
			out.print("/v");
			out.print(VersionInfo.BUILD);
			out.print('/');
			out.print(jsFileName);
			out.println(".js\"></script>");
			
			// Mark as added
			ContentHelper.addContent(pageContext, "JS", jsFileName);
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