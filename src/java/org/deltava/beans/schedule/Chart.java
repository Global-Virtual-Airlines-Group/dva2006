// Copyright 2004, 2005, 2006, 2007, 2009, 2010, 2012, 2015, 2016, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A class for storing approach/procedure chart data.
 * @author Luke
 * @version 10.6
 * @since 1.0
 */

public class Chart extends ImageBean implements ComboAlias, UseCount, ViewEntry {

	/**
	 * Chart type enumeration.
	 */
	public enum Type implements EnumDescription {
		UNKNOWN("???"), ILS("ILS Approach"), APR("Approach"), STAR("Standard Terminal Arrival"), SID("Standard Instrument Departure"), GROUND("Facility"), PACKAGE("Combined Package"), MIN("Minimums");
		
		private final String _desc;
		
		Type(String desc) {
			_desc = desc;
		}

		@Override
		public String getDescription() {
			return _desc;
		}
	}

	/**
	 * Chart Image type enumeration.
	 */
	public enum ImageFormat {
		GIF, JPG, PNG, PDF;
	}
	
	private ImageFormat _imgType;
	private Type _type;
	private String _name;
	private int _useCount;

	private Airport _airport;
	private Instant _lastMod;

	/**
	 * Create a new Chart with a name and an Airport.
	 * @param name the chart name
	 * @param a the airport
	 * @throws NullPointerException if name is null
	 */
	public Chart(String name, Airport a) {
		super();
		setName(name);
		setAirport(a);
	}

	/**
	 * Return the chart's Airport.
	 * @return the Airport bean associated with this chart
	 * @see Chart#setAirport(Airport)
	 */
	public Airport getAirport() {
		return _airport;
	}

	/**
	 * Return the chart name.
	 * @return the chart name
	 * @see Chart#getName()
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Return the chart's image type.
	 * @return the ImageType
	 * @see Chart#setImgFormat(ImageFormat)
	 */
	public ImageFormat getImgFormat() {
		return _imgType;
	}
	
	@Override
	public ImageType getImageType() {
		return ImageType.CHART;
	}

	/**
	 * Returns the chart type.
	 * @return the chart type
	 * @see Chart#setType(Type)
	 */
	public Type getType() {
		return _type;
	}

	/**
	 * Returns whether this is an external chart.
	 * @return FALSE
	 */
	@SuppressWarnings("static-method")
	public boolean getIsExternal() {
		return false;
	}
	
	/**
	 * Returns the number of times this chart has been viewed.
	 * @return the number of views
	 * @see Chart#setUseCount(int)
	 */
	@Override
	public int getUseCount() {
		return _useCount;
	}
	
	/**
	 * Returns the last modification date of the chart.
	 * @return the modification date/time
	 */
	public Instant getLastModified() {
		return _lastMod;
	}
	
	/**
	 * Updates the last modification date of the chart.
	 * @param dt the modification date/time
	 */
	public void setLastModified(Instant dt) {
		_lastMod = dt;
	}

	/**
	 * Updates the Chart name.
	 * @param name the new name
	 * @throws NullPointerException if name is null
	 * @see Chart#getName()
	 */
	public void setName(String name) {
		_name = name.trim().toUpperCase();
	}

	/**
	 * Updates the Airport.
	 * @param a the new Airport
	 * @see Chart#getAirport()
	 */
	public void setAirport(Airport a) {
		_airport = a;
	}

	/**
	 * Set the chart type.
	 * @param t the chart Type
	 */
	public void setType(Type t) {
		_type = t;
	}

	/**
	 * Set the chart image format.
	 * @param fmt the ImageFormat
	 * @see Chart#getImgFormat()
	 */
	public void setImgFormat(ImageFormat fmt) {
		_imgType = fmt;
	}

	/**
	 * Updates the number of times this chart has been viewed.
	 * @param cnt the number of views
	 * @see Chart#getUseCount()
	 */
	public void setUseCount(int cnt) {
		_useCount = Math.max(0, cnt);
	}

	/**
	 * Compares two Charts by comparing their Airports, then their names.
	 */
	@Override
	public int compareTo(Object o2) {
		Chart c2 = (Chart) o2;
		int tmp = _airport.compareTo(c2._airport);
		return (tmp == 0) ? _name.compareTo(c2._name) : tmp;
	}

	@Override
	public String getRowClassName() {
		return (_type == Type.UNKNOWN) ? null : _type.name().toLowerCase();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Chart) && (compareTo(o) == 0);
	}
	
	@Override
	public String getComboName() {
		return getName();
	}

	@Override
	public String getComboAlias() {
		return getHexID();
	}

	@Override
	public String toString() {
		return getName();
	}
}