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
	private int _maxLine;
	private LocalDate _effDate;
	private Instant _importDate;
	private boolean _autoImport;
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
	 * Returns whether this schedule import was an automatic import.
	 * @return TRUE if automatic, otherwise FALSE
	 */
	public boolean getAutoImport() {
		return _autoImport;
	}
	
	/**
	 * Returns the maximum imported line number for this source.
	 * @return the maximum line number
	 */
	public int getMaxLineNumber() {
		return _maxLine;
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
	 * Returns whether this is a current schedule import.
	 * @return TRUE if the effective date is less than 72 hours in the past, otherwise FALSE
	 */
	public boolean getIsCurrent() {
		return (_effDate != null) && (Duration.between(LocalDateTime.now(), _effDate.atStartOfDay()).abs().toHours() < 96);
	}
	
	/**
	 * Returns the base schedule effective date, tied to the Monday of the week of the effective date, if not current.
	 * @return the Monday at the start of the week, or the effective date if current
	 */
	public LocalDate getBaseDate() {
		if (getIsCurrent() || (_effDate == null)) return _effDate;
		return _effDate.minusDays(_effDate.getDayOfWeek().ordinal());
	}
	
	/**
	 * Returns the date to be used if filtering flights from this source today.
	 * @return today is current or no previous import, otherwise the base date adjusted by the day of week
	 * @see ScheduleSourceInfo#getBaseDate()
	 */
	public LocalDate getNextImportDate() {
		if (getIsCurrent() || (_effDate == null)) return LocalDate.now();
		return getBaseDate().plusDays(LocalDate.now().getDayOfWeek().ordinal());
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
	
	/**
	 * Sets the last import as an automatic schedule import.
	 * @param isAuto TRUE if automatic, otherwise FALSE
	 */
	public void setAutoImport(boolean isAuto) {
		_autoImport = isAuto;
	}
	
	/**
	 * Updates the maximum imported line number for this source.
	 * @param ln the line number
	 */
	public void setMaxLineNumber(int ln) {
		_maxLine = ln;
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