// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.text.*;

/**
 * A bean to store Runway visual range METAR components. 
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class RunwayVisualRange implements Comparable<RunwayVisualRange> {
	
	private final NumberFormat _vfmt = new DecimalFormat("0000");
	
	private String _rwyCode;
	
	private int _minViz;
	private int _maxViz;

	/**
	 * Initializes the bean.
	 * @param rwyCode the runway code
	 * @throws NullPointerException if rwyCode is null
	 */
	public RunwayVisualRange(String rwyCode) {
		super();
		_rwyCode = rwyCode.toUpperCase();
	}

	/**
	 * Retrieves the maximum visibility for this runway.
	 * @return the visibility in feet
	 */
	public int getMaxVisibility() {
		return _maxViz;
	}
	
	/**
	 * Retrieves the minimum visibility for this runway.
	 * @return the visibility in feet
	 */
	public int getMinVisibility() {
		return _minViz;
	}
	
	/**
	 * Returns the runway code.
	 * @return the runway code
	 */
	public String getRunwayCode() {
		return _rwyCode;
	}
	
	/**
	 * Updates the maximum visibility for this runway.
	 * @param viz the visibility in feet
	 */
	public void setMaxVisibility(int viz) {
		_maxViz = Math.max(0, viz);
	}
	
	/**
	 * Updates the minimum visibility for this runway.
	 * @param viz the visibility in feet
	 */
	public void setMinVisibility(int viz) {
		_minViz = Math.max(0, viz);	
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder("R");
		buf.append(_rwyCode);
		buf.append('/');
		buf.append(_vfmt.format(_minViz));
		if (_maxViz > _minViz) {
			buf.append('V');
			buf.append(_vfmt.format(_maxViz));
		}
		
		buf.append("FT");
		return buf.toString();
	}
	
	/**
	 * Compares two RVRs by comparing their runway codes.
	 */
	public int compareTo(RunwayVisualRange rvr2) {
		return _rwyCode.compareTo(rvr2._rwyCode);
	}
	
	public int hashCode() {
		return _rwyCode.hashCode();
	}
}