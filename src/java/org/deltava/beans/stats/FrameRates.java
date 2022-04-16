// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

/**
 * A bean to store simulator frame rate statistics.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class FrameRates implements java.io.Serializable {

	private int _count;
	private int _max;
	private int _min;
	private double _avg;
	
	private final SortedMap<Integer, Integer> _pcts = new TreeMap<Integer, Integer>(); 

	/**
	 * Returns the number of frame rate samples.
	 * @return the number of samples
	 */
	public int getSize() {
		return _count;
	}
	
	/**
	 * Returns the maximum frame rate.
	 * @return the maximum frames per second
	 */
	public int getMax() {
		return _max;
	}
	
	/**
	 * Returns the minimum frame rate.
	 * @return the minimum frames per second
	 */
	public int getMin() {
		return _min;
	}
	
	/**
	 * Returns the average frame rate.
	 * @return the average frames per second
	 */
	public double getAverage() {
		return _avg;
	}
	
	/**
	 * Returns a frame rate percentile.
	 * @param pct the percentile
	 * @return the frame rate in frames per second, or zero if not found
	 */
	public int getPercentile(int pct) {
		return _pcts.getOrDefault(Integer.valueOf(pct), Integer.valueOf(0)).intValue();
	}

	/**
	 * Updates the number of frame rate samples.
	 * @param cnt the number of samples
	 */
	public void setSize(int cnt) {
		_count = cnt;
	}
	
	/**
	 * Updates the maximum frame rate.
	 * @param max the maximum frames per second
	 */
	public void setMax(int max) {
		_max = max;
	}
	
	/**
	 * Updates the minimum frame rate.
	 * @param min the minimum frames per second
	 */
	public void setMin(int min) {
		_min = min;
	}
	
	/**
	 * Updates the average frame rate.
	 * @param avg the average frames per second
	 */
	public void setAverage(double avg) {
		_avg = avg;
	}
	
	/**
	 * Updates a frame rate percentile.
	 * @param pct the percentile
	 * @param frameRate the frame rate in frames per second
	 */
	public void setPercentile(int pct, int frameRate) {
		_pcts.put(Integer.valueOf(pct), Integer.valueOf(frameRate));
	}
}