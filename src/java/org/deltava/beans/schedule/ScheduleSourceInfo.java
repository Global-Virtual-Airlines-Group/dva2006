// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.ComboAlias;

import org.deltava.util.ComboUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store raw schedule statistics data.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class ScheduleSourceInfo implements ComboAlias, Cacheable {

	private final ScheduleSource _src;
	private final Map<Airline, Integer> _airlineLegs = new TreeMap<Airline, Integer>();
	
	/**
	 * Creates the bean.
	 * @param src the ScheduleSource 
	 */
	public ScheduleSourceInfo(ScheduleSource src) {
		super();
		_src = src;
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
		return _airlineLegs.entrySet().stream().mapToInt(me -> me.getValue().intValue()).sum();
	}
	
	/**
	 * Returns the Airlines loaded from this schedule source.
	 * @return a Collection of Airline beans
	 */
	public Collection<Airline> getAirlines() {
		return _airlineLegs.keySet();
	}
	
	/**
	 * Returns the number of flight legs for a particular Airline from this source.
	 * @param a an Airline bean
	 * @return the number of legs
	 */
	public int getLegs(Airline a) {
		return _airlineLegs.getOrDefault(a, Integer.valueOf(0)).intValue();
	}
	
	/**
	 * Converts the airline/leg information into a set of Combobox/Checkbox options.
	 * @return a Collection of ComboAlias objects
	 */
	public Collection<ComboAlias> getOptions() {
		return _airlineLegs.entrySet().stream().map(me -> ComboUtils.fromString(me.getKey().getName() + " (" + me.getValue() + ")", me.getKey().getComboAlias())).collect(Collectors.toList());
	}
	
	/**
	 * Updates the number of flight legs for a particular Airline.
	 * @param a the Airline bean
	 * @param legs the number of legs
	 */
	public void setLegs(Airline a, int legs) {
		_airlineLegs.put(a, Integer.valueOf(legs));
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

	@Override
	public String getComboAlias() {
		return _src.name();
	}

	@Override
	public String getComboName() {
		StringBuilder buf = new StringBuilder(_src.getDescription());
		buf.append(" (");
		buf.append(getLegs());
		buf.append(" entries)");
		return buf.toString();
	}
}