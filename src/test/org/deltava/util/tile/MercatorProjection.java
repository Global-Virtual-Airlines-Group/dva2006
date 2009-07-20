// Copyright 2007 The Weather Channel Interactive. All Rights Reserved.
package org.deltava.util.tile;

import java.awt.Point;

/**
 * A utility class for translating canvas coordinates to latitude and longitude using the Microsoft Virtual
 * Earth and Google Maps modified Mercator projection.
 * @author LKolin
 * @version 1.0
 * @since 1.0
 */

public class MercatorProjection {
	
	public static final double MAX_LATITUDE = 85.051111;
	public static final double MIN_LATITUDE = -85.051111;
	
	private static final double maxY = toMercator(MAX_LATITUDE);
	private static final double minY = toMercator(MIN_LATITUDE);
	private static final double dLat = maxY - minY;
	
	private int _zoomLevel;
	private double _xScale;
	private double _yScale;

	/**
	 * Initializes the projection.
	 * @param zoom the zoom level
	 */
	public MercatorProjection(int zoom) {
		super();
		_zoomLevel = Math.max(1,  zoom);
		_xScale = (1 << _zoomLevel) * Tile.WIDTH;
		_yScale = (1 << _zoomLevel) * Tile.HEIGHT;
	}

	/**
	 * Returns the zoom level.
	 * @return the zoom level
	 */
	public int getZoomLevel() {
		return _zoomLevel;
	}
	
	/**
	 * Returns the address of a Tile containing the provided point.
	 * @param lat the latitude in degrees
	 * @param lng the longitude in degrees
	 * @return the TileAddress of the Tile containing this point at the current zoom level
	 */
	public TileAddress getAddress(double lat, double lng) {
		Point p = getPixelAddress(lat, lng);
		return new TileAddress(p.x / Tile.WIDTH, p.y / Tile.HEIGHT, _zoomLevel);
	}
	
    private static double fromMercator( double lat )
    {   
        double ry = Math.toRadians( lat );
        double plat = 2 * Math.atan( Math.pow( Math.E, ry ) ) - ( Math.PI / 2d );
        return Math.toDegrees( plat );
    }
    
    private static double toMercator(double lat) {
        double ry = Math.toRadians( lat );
        double plat = Math.log( Math.tan( ry ) + ( 1 / Math.cos( ry ) ) );
        return Math.toDegrees( plat );
    }
	
	/**
	 * Returns the pixel address of the provided point on the global canvas.
	 * @param lat the latitude in degrees
	 * @param lng the longitude in degrees
	 * @return a Point with the pixel coordinates
	 */
	public Point getPixelAddress(double lat, double lng) {
		// Bounds-check the lat
		lat = Math.min(lat, MAX_LATITUDE);
		lat = Math.max(lat, MIN_LATITUDE);
		
		// Translate longitude
		while (lng > 180)
			lng -= 180;
		
		while (lng < -180)
			lng += 180;
		
		// Convert latitude to mercator
		double pmY = toMercator(lat);
		double pY = (pmY - minY) / dLat;
		
		double pX = ((lng * -1) - 180) / 360;
		int x = (int) Math.floor(_xScale - (pX * _xScale)) - 1;
		int y = (int) Math.floor(_yScale - (pY * _yScale)) - 1;
		
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
	 * @return an array of double [latitude,longitude] coordinates
	 */
	public double[] getGeoPosition(int x, int y) {
        double px = (_xScale - x ) / _xScale;
        double py = (_yScale - y ) / _yScale;
        double lon = (180 + ( px * 360 )) * -1;
        return new double[] { fromMercator(minY + (py * dLat)), lon };
	}
}