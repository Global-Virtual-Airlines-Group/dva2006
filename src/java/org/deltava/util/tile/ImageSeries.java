// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.util.*;

/**
 * A bean to store all tile imagery for a particular date/time.  
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class ImageSeries extends HashSet<PNGTile> implements Comparable<ImageSeries> {

	private final String _type;
	private final Date _effDate;
	
	/**
	 * Creates the object.
	 * @param type the image type
	 * @param effDate the effective date/time
	 */
	public ImageSeries(String type, Date effDate) {
		super();
		_type = type;
		_effDate = effDate;
	}

	/**
	 * Returns the image type.
	 * @return the image type
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * Returns the effective date/time
	 * @return the effective date/time
	 */
	public Date getDate() {
		return _effDate;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_type).append(':');
		return buf.append(_effDate).toString();
	}
	
	public int compareTo(ImageSeries is2) {
		int tmpResult = _effDate.compareTo(is2._effDate);
		return (tmpResult == 0) ? _type.compareTo(is2._type) : tmpResult;
	}
}