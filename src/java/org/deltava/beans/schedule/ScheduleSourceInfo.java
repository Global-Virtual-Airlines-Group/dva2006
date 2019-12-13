// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store raw schedule statistics data.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class ScheduleSourceInfo implements Cacheable {

	private final ScheduleSource _src;
	private final int _legs;
	
	/**
	 * Creates the bean.
	 * @param src the ScheduleSource 
	 * @param legs the total number of flight legs
	 */
	public ScheduleSourceInfo(ScheduleSource src, int legs) {
		super();
		_src = src;
		_legs = legs;
	}

	/**
	 * Returns the schedule source.
	 * @return the ScheduleSource
	 */
	public ScheduleSource getSource() {
		return _src;
	}
	
	/**
	 * Returns the total number of flight legs from this source. 
	 * @return the number of legs
	 */
	public int getLegs() {
		return _legs;
	}
	
	@Override
	public String toString() {
		return _src.name();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public Object cacheKey() {
		return toString();
	}
}