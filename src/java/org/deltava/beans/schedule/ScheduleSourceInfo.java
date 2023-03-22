// Copyright 2019, 2020, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;

import org.deltava.beans.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store raw schedule statistics data.
 * @author Luke
 * @version 10.5
 * @since 9.0
 */

public class ScheduleSourceInfo implements ComboAlias, Cacheable, CalendarEntry {

	private final ScheduleSource _src;
	private int _maxLine;
	private LocalDate _effDate;
	private Instant _importDate;
	private boolean _autoImport;
	private boolean _isActive;
	private final Map<Airline, Integer> _airlineLegs = new TreeMap<Airline, Integer>();

	/**
	 * The number of skipped flight legs.
	 */
	protected int _skipped;
	
	/**
	 * The number of flight legs adjusted for DST.
	 */
	protected int _adjusted;
	private boolean _purged;
	
	/**
	 * Creates the bean.
	 * @param src the ScheduleSource 
	 */
	public ScheduleSourceInfo(ScheduleSource src) {
		super();
		_src = src;
	}
	
	/**
	 * Creates the bean from an existing ScheduleSourceInfo bean.
	 * @param inf the ScheduleSourceInfo
	 */
	protected ScheduleSourceInfo(ScheduleSourceInfo inf) {
		this(inf._src);
		_maxLine = inf._maxLine;
		_effDate = (inf._effDate == null) ? null : inf._effDate.plusDays(0); // clone
		_importDate = (inf._importDate == null) ? null : inf._importDate.plusMillis(0); // clone
		_autoImport = inf._autoImport;
		_isActive = inf._isActive;
		_skipped = inf._skipped;
		_adjusted = inf._adjusted;
		_purged = inf._purged;
		_airlineLegs.putAll(inf._airlineLegs);
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
	
	@Override
	public Instant getDate() {
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
	 * Returns if the schedule import is currently active.
	 * @return TRUE if active, otherwise FALSE
	 */
	public boolean getActive() {
		return _isActive;
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
	 * Returns the number of skipped flight legs in the last import.
	 * @return the number of legs
	 */
	public int getSkipped() {
		return _skipped;
	}
	
	/**
	 * Returns the number of flight legs with adjusted arrival times (for DST) in the last import.
	 * @return the number of legs
	 */
	public int getAdjusted() {
		return _adjusted;
	}
	
	/**
	 * Returns whether this source was purged during the current import.
	 * @return TRUE if purged, otherwise FALSE
	 */
	public boolean getPurged() {
		return _purged;
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
	 * @param a the Airline
	 * @param legs the number of legs
	 */
	public void addLegs(Airline a, int legs) {
		Integer i = _airlineLegs.getOrDefault(a, Integer.valueOf(0));
		_airlineLegs.put(a, Integer.valueOf(i.intValue() + legs));
	}
	
	/**
	 * Updates the Airlines for this source.
	 * @param airlines a Colleciton of Airlines
	 */
	public void setAirlines(Collection<Airline> airlines) {
		_airlineLegs.clear();
		airlines.forEach(al -> _airlineLegs.put(al, Integer.valueOf(0)));
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
	 * Sets if the schedule import is currently active.
	 * @param isActive TRUE if active, otherwise FALSE
	 */
	public void setActive(boolean isActive) {
		_isActive = isActive;
	}
	
	/**
	 * Updates the maximum imported line number for this source.
	 * @param ln the line number
	 */
	public void setMaxLineNumber(int ln) {
		_maxLine = ln;
	}
	
	/**
	 * Increments the number of skipped flight legs for this source.
	 */
	public void skip() {
		_skipped++;
	}
	
	/**
	 * Increments the number flight legs with adjusted arrival times for this source.
	 */
	public void adjust() {
		_adjusted++;
	}
	
	/**
	 * Updates whether this source was purged during the current import.
	 * @param isPurged TRUE if purged, otherwise FALSE
	 */
	public void setPurged(boolean isPurged) {
		_purged = isPurged;
	}
	
	@Override
	public int compareTo(Object o) {
		ScheduleSourceInfo ssi = (ScheduleSourceInfo) o;
		Instant d2 = ssi.getDate();
		if (_importDate == null)
			return (d2 == null) ? _src.compareTo(ssi.getSource()) : 0;

		return (d2 == null) ? 1 : _importDate.compareTo(d2) ;
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