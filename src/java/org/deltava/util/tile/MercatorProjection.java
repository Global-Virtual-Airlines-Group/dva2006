// Copyright 2007, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.Point;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A utility class for translating canvas coordinates to latitude and longitude using the Microsoft Virtual
 * Earth and Google Maps modified Mercator projection.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class MercatorProjection implements Projection {
	
	public static final double MAX_LATITUDE = 85.051111;
	public static final double MIN_LATITUDE = -MAX_LATITUDE;
	
	private static final double maxY = toMercator(MAX_LATITUDE);
	private static final double minY = toMercator(MIN_LATITUDE);
	private static final double dLat = maxY - minY;
	
	private final int _zoomLevel;
	private final double _xScale; // width of entire map in pixels
	private final double _yScale; // height of entire map in pixels

	/**
	 * Initializes the projection.
	 * @param zoom the zoom level
	 */
	public MercatorProjection(int zoom) {
		super();
		_zoomLevel = Math.max(1,  zoom);
		_xScale = (1 << _zoomLevel) << 8;
		_yScale = (1 << _zoomLevel) << 8;
	}

	/**
	 * Returns the zoom level.
	 * @return the zoom level
	 */
	@Override
	public int getZoomLevel() {
		return _zoomLevel;
	}
	
	/**
	 * Returns the address of a Tile containing the provided point.
	 * @param loc the GeoLocation
	 * @return the TileAddress of the Tile containing this point at the current zoom level
	 */
	@Override
	public org.gvagroup.tile.TileAddress getAddress(GeoLocation loc) {
		Point p = getPixelAddress(loc);

		// Normalize
		while (p.x >= _xScale)
			p.x -= _xScale;
		while (p.y >= _yScale)
			p.y -= _yScale;
		
		return new org.gvagroup.tile.TileAddress(p.x >> 8, p.y >> 8, _zoomLevel);
	}
	
    private static double fromMercator(double lat) {   
        double ry = Math.toRadians(lat);
        double plat = 2 * StrictMath.atan(StrictMath.pow(Math.E, ry)) - (Math.PI / 2d);
        return Math.toDegrees(plat);
    }
    
    private static double toMercator(double lat) {
        double ry = Math.toRadians(lat);
        double plat = StrictMath.log(StrictMath.tan(ry) + (1 / StrictMath.cos(ry)));
        return Math.toDegrees(plat);
    }
	
	/**
	 * Returns the pixel address of the provided point on the global canvas.
	 * @param loc the GeoLocation
	 * @return a Point with the pixel coordinates
	 */
    @Override
	public Point getPixelAddress(GeoLocation loc) {
		if ((loc.getLatitude() > MAX_LATITUDE) || (loc.getLatitude() < MIN_LATITUDE)) return null; 
		
		// Convert latitude to mercator
		double pmY = toMercator(loc.getLatitude());
		double pY = (pmY - minY) / dLat;
		
		double pX = (-loc.getLongitude() - 180) / 360;
		int x = (int) StrictMath.floor(_xScale - (pX * _xScale));
		int y = (int) StrictMath.floor(_yScale - (pY * _yScale));
		
		// Normalize
		while (x > _xScale)
			x -= _xScale;
		while (y > _yScale)
			y -= _yScale;
		
		return new Point(x, y);
	}
	
	/**
	 * Returns the latitude/longitude of a pixel on the global canvas.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return a GeoLocation
	 */
	@Override
	public GeoLocation getGeoPosition(int x, int y) {
        double px = (_xScale - x ) / _xScale;
        double py = (_yScale - y ) / _yScale;
        double lon = -(180 + (px * 360));
        while (lon < -180)
			lon += 360;
        
        return new GeoPosition(fromMercator(minY + (py * dLat)), lon);
	}
}