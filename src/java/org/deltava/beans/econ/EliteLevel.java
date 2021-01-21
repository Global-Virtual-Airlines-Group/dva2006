// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.time.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Pilot loyalty status level definitions for a particular year. 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteLevel implements EliteTotals, Comparable<EliteLevel>, Cacheable {
	
	/**
	 * A dummy, empty Elite level.
	 */
	public static final EliteLevel EMPTY = new EliteLevel(0, "$EMPTY");
	
	/**
	 * The first year of the Elite program.
	 */
	public static final int MIN_YEAR = 2003;
	
	private final String _name;
	private final int _year;
	private int _targetPercentile;
	
	private int _legs;
	private int _distance;
	private int _points;
	
	private float _pointBonus;
	private int _color;
	private boolean _visible = true;
	
	/**
	 * Calculates the Elite program year for a given date.
	 * @param dt the date/time
	 * @return the year
	 */
	public static int getYear(Instant dt) {
		return LocalDate.ofInstant(dt, ZoneOffset.UTC).getYear();
	}
	
	/**
	 * Creates the bean.
	 * @param year the year
	 * @param name the level name
	 */
	public EliteLevel(int year, String name) {
		super();
		_year = year;
		_name = name;
	}
	
	/**
	 * Returns the level name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the year these requirements were in effect.
	 * @return the year
	 */
	public int getYear() {
		return _year;
	}

	/**
	 * Returns the number of flight legs required for this level.
	 * @return the number of legs
	 */
	@Override
	public int getLegs() {
		return _legs;
	}
	
	/**
	 * Returns the total flight distance required for this level.
	 * @return the number of miles
	 */
	@Override
	public int getDistance() {
		return _distance;
	}
	
	/**
	 * Returns the number of status points required for this level.
	 * @return the number of points
	 */
	@Override
	public int getPoints() {
		return _points;
	}
	
	/**
	 * Returns the bonus factor to be applied to points earned on a flight when the pilot is at this level.
	 * @return the bonus factor
	 */
	public float getBonusFactor() {
		return _pointBonus;
	}
	
	/**
	 * Returns the color used to display this level.
	 * @return the RGB color code
	 */
	public int getColor() {
		return _color;
	}
	
	/**
	 * Returns this level's target Pilot percentile.
	 * @return the percentile
	 */
	public int getTargetPercentile() {
		return _targetPercentile;
	}
	
	/**
	 * Returns the color used to display this level in a CSS-friendly format.
	 * @return the RGB color code
	 */
	public String getHexColor() {
		StringBuilder buf = new StringBuilder(Integer.toHexString(_color).toLowerCase());
		while (buf.length() < 6)
			buf.insert(0, '0');
		
		return buf.toString();
	}
	
	/**
	 * Returns whether the level is visible.
	 * @return TRUE if visible, otherwise FALSE
	 */
	public boolean getIsVisible() {
		return _visible;
	}
	
	/**
	 * Updates the number of flight legs required for this level.
	 * @param legs the number of legs
	 */
	public void setLegs(int legs) {
		_legs = legs;
	}
	
	/**
	 * Updates the total flight distance required for this level.
	 * @param dst the distance in miles
	 */
	public void setDistance(int dst) {
		_distance = dst;
	}

	/**
	 * Updates the number of status points required for this level.
	 * @param pts the number of points
	 */
	public void setPoints(int pts) {
		_points = pts;
	}
	
	/**
	 * Updates the bonus factor to be applied to points earned on a flight when the pilot is at this level.
	 * @param factor the bonus factor
	 */
	public void setBonusFactor(float factor) {
		_pointBonus = factor;
	}
	
	/**
	 * Updates the color used to display this level.
	 * @param c the RGB code
	 */
	public void setColor(int c) {
		_color = c;
	}
	
	/**
	 * Updates this level's target Pilot percentile.
	 * @param pct the percentile
	 */
	public void setTargetPercentile(int pct) {
		_targetPercentile = Math.max(0,  Math.min(100, pct));
	}
	
	/**
	 * Updates whether the level is visible.
	 * @param isVisible TRUE if visible, otherwise FALSE
	 */
	public void setVisible(boolean isVisible) {
		_visible = isVisible;
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof EliteLevel) && (compareTo((EliteLevel) o) == 0);
	}

	@Override
	public Object cacheKey() {
		return toString();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_name);
		buf.append('/');
		buf.append(_year);
		return buf.toString();
	}

	@Override
	public int compareTo(EliteLevel el2) {
		int tmpResult = Integer.compare(_points, el2._points);
		if (tmpResult == 0)
			tmpResult = Integer.compare(_legs, el2._legs);
		
		return (tmpResult == 0) ? Integer.compare(_distance, el2._distance) : tmpResult;
	}
}