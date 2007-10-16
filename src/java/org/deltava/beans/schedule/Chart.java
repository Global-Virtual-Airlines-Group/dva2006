// Copyright 2004, 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A class for storing approach/procedure chart data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Chart extends DatabaseBlobBean implements ComboAlias {

	public static final int UNKNOWN = 0;
	public static final int ILS = 1;
	public static final int APR = 2;
	public static final int STAR = 3;
	public static final int SID = 4;
	public static final int GROUND = 5;

	/**
	 * Chart type descriptions.
	 */
	public static final String[] TYPES = { "???", "ILS", "APR", "STAR", "SID", "GROUND" };

	/**
	 * Human-readable type descriptions.
	 */
	public static final String[] TYPENAMES = { "Unknown", "ILS Approach", "Approach", "Standard Terminal Arrival",
			"Standard Instrument Departure", "Facility" };

	public static final int GIF = 0;
	public static final int JPEG = 1;
	public static final int PNG = 2;
	public static final int PDF = 3;

	/**
	 * Image type codes.
	 */
	public static final String[] IMG_TYPE = { "gif", "jpg", "png", "pdf" };

	/**
	 * Adobe Portable Document Format magic number.
	 */
	public static final String PDF_MAGIC = "%PDF-";

	private int _imgType;
	private int _type;
	private String _name;
	private int _size;

	private Airport _airport;

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
	 * @return the image type code
	 * @see Chart#getImgTypeName()
	 * @see Chart#setImgType(int)
	 */
	public int getImgType() {
		return _imgType;
	}

	/**
	 * Return the chart's image type name.
	 * @return the image type name
	 * @see Chart#getImgType()
	 */
	public String getImgTypeName() {
		return Chart.IMG_TYPE[_imgType].toUpperCase();
	}

	/**
	 * Returns the chart type.
	 * @return the chart type
	 * @see Chart#getTypeName()
	 * @see Chart#setType(int)
	 * @see Chart#setType(String)
	 */
	public int getType() {
		return _type;
	}

	/**
	 * Returns the chart type name.
	 * @return the chart type name
	 * @see Chart#getType()
	 */
	public String getTypeName() {
		return Chart.TYPENAMES[_type];
	}

	/**
	 * Return the size of the chart image.
	 * @return the size of the image in bytes
	 * @see Chart#setSize(int)
	 */
	public int getSize() {
		return (_buffer == null) ? _size : _buffer.length;
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
	 * @param type the chart type code
	 * @throws IllegalArgumentException if the chart type is negative or not in ChartConstants
	 */
	public void setType(int type) {
		if ((type < 0) || (type >= Chart.TYPES.length))
			throw new IllegalArgumentException("Invalid Chart Type - " + type);

		_type = type;
	}

	/**
	 * Set the chart type
	 * @param type the chart type
	 * @throws IllegalArgumentException if the chart type is not in ChartConstants
	 */
	public void setType(String type) {
		int ofs = StringUtils.arrayIndexOf(Chart.TYPES, type);
		if (ofs == -1)
			throw new IllegalArgumentException("Invalid Chart Type - " + type);
		
		setType(ofs);
	}

	/**
	 * Set the chart image type
	 * @param imgType the image type code
	 * @throws IllegalArgumentException if the image type is not in ChartConstants
	 * @see Chart#getImgType()
	 */
	public void setImgType(int imgType) {
		if ((imgType < 0) || (imgType >= Chart.IMG_TYPE.length))
			throw new IllegalArgumentException("Invalid Chart image type - " + imgType);

		_imgType = imgType;
	}

	/**
	 * Set the size of the chart image.
	 * @param size the image size in bytes
	 * @throws IllegalArgumentException if the size is zero or negative
	 */
	public void setSize(int size) {
		if (size < 1)
			throw new IllegalArgumentException("Image Size cannot be zero or negative");

		_size = size;
	}

	/**
	 * Compare two Charts by comparing their IATA codes, then their names
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @throws ClassCastException if o2 is not a Chart
	 */
	public int compareTo(Object o2) {
		Chart c2 = (Chart) o2;
		int tmp = _airport.compareTo(c2.getAirport());
		return (tmp == 0) ? _name.compareTo(c2.getName()) : tmp;
	}

	/**
	 * Determine equality by calling compareTo()
	 * @see Chart#compareTo(Object)
	 */
	public boolean equals(Chart c2) {
		return (compareTo(c2) == 0);
	}
	
	public String getComboName() {
		return getName();
	}

	public String getComboAlias() {
		return StringUtils.formatHex(getID());
	}
	
	public String toString() {
		return getName();
	}
}