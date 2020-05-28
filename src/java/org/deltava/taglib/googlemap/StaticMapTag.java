// Copyright 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.*;

import org.deltava.taglib.html.ElementTag;
import org.deltava.util.CollectionUtils;
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
	private int _scale;
	private GeoLocation _center;
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

	@Override
	public void release() {
		super.release();
		_scale = 1;
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
	
	public void setScale(int s) {
		_scale = Math.max(1, s);
	}

	/**
	 * Updates the map center.
	 * @param loc the center
	 */
	public void setCenter(GeoLocation loc) {
		_center = loc;
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
		
		// Adjust for scae
		if (_scale > 1) {
			_h /= _scale;
			_w /= _scale;
		}

		// Build params
		StringBuilder buf = new StringBuilder("maptype=terrain&size=");
		buf.append(_w).append('x').append(_h);
		buf.append("&center=").append(encodeLL(_center));
		if (_scale > 1)
			buf.append("&scale=").append(_scale);

		// Bucket and add the markers
		Map<String, Collection<MapEntry>> mrks = new LinkedHashMap<String, Collection<MapEntry>>();
		_mrks.forEach(mrk -> CollectionUtils.addMapCollection(mrks, getMarkerStyle(mrk), mrk));
		for (Map.Entry<String, Collection<MapEntry>> me : mrks.entrySet()) {
			buf.append("&markers=").append(me.getKey()); // style
			buf.append('|');
			for (Iterator<? extends GeoLocation> i = me.getValue().iterator(); i.hasNext();) {
				GeoLocation loc = i.next();
				buf.append(encodeLL(loc));
				if (i.hasNext())
					buf.append('|');
			}
		}

		// Ensure visibility and draw path
		if (!_mrks.isEmpty()) {
			buf.append("&path=weight:1|color:0x4080af|geodesic:true|"); GeoLocation lastMrk = null;
			for (Iterator<? extends GeoLocation> i = _mrks.iterator(); i.hasNext();) {
				lastMrk = i.next();
				buf.append(encodeLL(lastMrk));
				if (i.hasNext())
					buf.append('|');
			}
			
			buf.append("&visible=");
			buf.append(encodeLL(_mrks.get(0)));
			buf.append('|');
			buf.append(encodeLL(lastMrk));
		}

		// Add path
		if (!_path.isEmpty()) {
			buf.append("&path=weight:2|color:0x80800f|geodesic:true|enc:");
			buf.append(encodePolyline(_path));
		}

		// Build the image URL
		String apiKey = SystemData.get("security.key.googleMaps." + (_isAnonymous ? "anon" : "auth"));
		StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?");
		url.append(buf.toString());
		url.append("&key=");
		url.append(apiKey);
		_data.setAttribute("src", url.toString());
		_data.setAttribute("style", "max-width:98%");

		try {
			_out.print(_data.open(true, true));
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}

	private static StringBuilder encodeLL(GeoLocation loc) {
		StringBuilder buf = new StringBuilder();
		buf.append(StringUtils.format(loc.getLatitude(), "##0.00000"));
		buf.append(',');
		buf.append(StringUtils.format(loc.getLongitude(), "##0.00000"));
		return buf;
	}

	private static String getMarkerStyle(MapEntry me) {
		StringBuilder buf = new StringBuilder("size:tiny|color:");
		buf.append(((MarkerMapEntry) me).getIconColor());
		return buf.toString();
	}

	private static String encodePolyline(Collection<GeoLocation> locs) {

		StringBuilder buf = new StringBuilder();
		long lastLat = 0; long lastLng = 0;
		for (GeoLocation loc : locs) {
			long lat = Math.round(loc.getLatitude() * 1e5);
			long lng = Math.round(loc.getLongitude() * 1e5);

			// Calculate delta
			long dLat = lat - lastLat; long dLng = lng - lastLng;

			// Encode
			encode(dLat, buf); encode(dLng, buf);
			lastLat = lat; lastLng = lng;
		}

		return buf.toString();
	}

	private static void encode(final long v, StringBuilder result) {
		long v2 = v < 0 ? ~(v << 1) : v << 1;
		while (v2 >= 0x20) {
			result.append(Character.toChars((int) ((0x20 | (v2 & 0x1f)) + 63)));
			v2 >>= 5;
		}
		
		result.append(Character.toChars((int) (v + 63)));
	}
}