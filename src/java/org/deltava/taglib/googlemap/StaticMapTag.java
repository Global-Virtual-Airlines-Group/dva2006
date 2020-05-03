// Copyright 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.*;

import org.deltava.taglib.html.ElementTag;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert a static Google Map on the page.
 * @author Luke
 * @version 9.0
 * @since 6.0
 */

public class StaticMapTag extends ElementTag {
	
	private int _h;
	private int _w;
	private boolean _isAnonymous;
	
	private final List<MapEntry> _mrks = new ArrayList<MapEntry>();
	private final Collection<GeoLocation> _path = new ArrayList<GeoLocation>();

	/**
	 * Creates a new map IMG Tag.
	 */
	public StaticMapTag() {
		super("img");
		release();
	}

	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_mrks.clear();
		_path.clear();
	}
	
	/**
	 * Adds markers to the map.
	 * @param mrks a Collection of MapEntry beans
	 */
	public void setMarkers(Collection<MapEntry> mrks) {
		_mrks.addAll(mrks);
	}
	
	/**
	 * Adds a path to the map.
	 * @param path a Collection of GeoLocations
	 */
	public void setPath(Collection<GeoLocation> path) {
		_path.addAll(path);
	}
	
	/**
	 * Sets the height of the map image.
	 * @param h the height in pixels
	 */
	public void setH(int h) {
		_h = Math.max(1, h);
		setNumericAttr("height", _h, 1);
	}
	
	/**
	 * Sets the width of the map image.
	 * @param w the width in pixels
	 */
	public void setW(int w) {
		_w = Math.max(1, w);
		setNumericAttr("width", _w, 1);
	}
	
	@Override
	public void setPageContext(PageContext ctx) {
		super.setPageContext(ctx);
		HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
		_isAnonymous = (req.getUserPrincipal() == null);
	}
	
	@Override
	public int doEndTag() throws JspException {
		APIUsage.track(APIUsage.Type.STATIC, _isAnonymous);
		
		// Build params
		StringBuilder buf = new StringBuilder("maptype=terrain&size=");
		buf.append(_w).append('x').append(_h);
		
		// Add markers
		if (!_mrks.isEmpty()) {
			buf.append("&markers=");
			MapEntry me = _mrks.get(0);
			if (me instanceof IconMapEntry) {
				buf.append("icon:https://");
				IconMapEntry ime = (IconMapEntry) me;
				if (ime.getPaletteCode() == 0) {
					buf.append(pageContext.getRequest().getServerName()).append('/');
					buf.append(SystemData.get("path.img")).append("/maps/pal");
				} else
					buf.append("maps.google.com/mapfiles/kml/pal");
				
				buf.append(ime.getPaletteCode()).append("/icon").append(ime.getIconCode()).append(".png");
			} else if (me instanceof MarkerMapEntry) {
				buf.append("color:");
				MarkerMapEntry mme = (MarkerMapEntry) me;
				buf.append(mme.getIconColor());
			}
			
			buf.append('|');
			for (Iterator<? extends GeoLocation> i = _mrks.iterator(); i.hasNext(); ) {
				GeoLocation loc = i.next();
				buf.append(StringUtils.format(loc.getLatitude(), "##0.00000"));
				buf.append(',');
				buf.append(StringUtils.format(loc.getLongitude(), "##0.00000"));
				if (i.hasNext())
					buf.append('|');
			}
		}
		
		// Add path
		if (!_path.isEmpty()) {
			buf.append("&path=weight:2|geodesic:true|");
			for (Iterator<GeoLocation> i = _path.iterator(); i.hasNext(); ) {
				GeoLocation loc = i.next();
				buf.append(StringUtils.format(loc.getLatitude(), "##0.00000"));
				buf.append(',');
				buf.append(StringUtils.format(loc.getLongitude(), "##0.00000"));
				if (i.hasNext())
					buf.append('|');
			}
		}
		
		// Build the image URL
		StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?");
		url.append(buf.toString());
		_data.setAttribute("src", url.toString());
		
		try {
			_out.print(_data.open(true, true));
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}