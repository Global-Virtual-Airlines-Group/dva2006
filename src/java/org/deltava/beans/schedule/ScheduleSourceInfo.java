// Copyright 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.time.*;
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
	private LocalDate _effDate;
	private Instant _importDate;
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
	 * Returns the effective date of the last schedule filter using this source.
	 * @return the effective date, or null
	 */
	public LocalDate getEffectiveDate() {
		return _effDate;
	}
	
	/**
	 * Returns the date of the last schedule filter using this source.
	 * @return the import date/time
	 */
	public Instant getImportDate() {
		return _importDate;
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
	 * Returns whether this schedule source includes a particular airline.
	 * @param a an Airline
	 * @return TRUE if included, otherwise FALSE
	 */
	public boolean contains(Airline a) {
		return (a != null) && (_airlineLegs.containsKey(a));
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
	
	/**
	 * Updates the effective date of the last schedule filter using this source.
	 * @param dt the effective date/time, the time will be discarded
	 */
	public void setEffectiveDate(Instant dt) {
		_effDate = (dt == null) ? null : LocalDate.ofInstant(dt, ZoneOffset.UTC);
	}
	
	/**
	 * Updates the date of the last schedule filter using this source.
	 * @param dt the filter date/time
	 */
	public void setImportDate(Instant dt) {
		_importDate = dt;
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