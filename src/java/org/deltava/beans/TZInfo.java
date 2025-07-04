// Copyright 2005, 2007, 2008, 2010, 2015, 2016, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.text.*;
import java.time.*;
import java.time.format.TextStyle;

/**
 * A class for dealing with Time Zones.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class TZInfo implements java.io.Serializable, ComboAlias, Comparable<TZInfo>, ViewEntry {

	/**
	 * Time Zone for GMT/UTC.
	 */
	public static final TZInfo UTC = new TZInfo(ZoneOffset.UTC, "Coordinated Universal Time", "UTC");

	private static final Map<String, TZInfo> _timeZones = new HashMap<String, TZInfo>();

	private final ZoneId _tz;
	private final String _displayName;
	private final String _abbr;
	private final boolean _hasDST;
	
	// Init default time zones
	static {
		_timeZones.put("UTC", UTC);
	}
	
	private TZInfo(ZoneId tz, String dName, String abbr) {
		super();
		_tz = tz;
		_displayName = dName;
		_abbr = (abbr != null) ? abbr.toUpperCase().trim() : null;
		_hasDST = (_tz.getRules().nextTransition(Instant.now()) != null);
	}

	/**
	 * Creates a Time Zone Wrapper with user-provided name and abbreviation.
	 * @param tzName A standard Java Time Zone ID
	 * @param displayName If not null, the name to display instead of the JVM name
	 * @param abbr An abbreviation for this time zone
	 * @return the TZInfo instance created
	 * @see TimeZone#getAvailableIDs()
	 * @see TimeZone#getTimeZone(java.lang.String)
	 */
	public static TZInfo init(String tzName, String displayName, String abbr) {
		TZInfo tz = new TZInfo(ZoneId.of(tzName), displayName, abbr);
		_timeZones.put(tzName, tz);
		return tz;
	}

	/**
	 * Gets a Time Zone wrapper using the standard Java Time Zone name.
	 * @param tzName A standard Java Time Zone ID
	 * @return a TZInfo bean, or null if not found
	 * @see TimeZone#getAvailableIDs()
	 * @see TimeZone#getTimeZone(String)
	 */
	public static TZInfo get(String tzName) {
		return _timeZones.get(tzName);
	}
	
	@Override
	public String getComboAlias() {
		return getID();
	}

	@Override
	public String getComboName() {
		return toString();
	}

	/**
	 * Returns all initialized Time Zones.
	 * @return a sorted Set of TZInfo objects
	 */
	public static Collection<TZInfo> getAll() {
		return new TreeSet<TZInfo>(_timeZones.values());
	}
	
	/**
	 * Returns the underlying Java TimeZone object.
	 * @return The JVM time zone
	 */
	public ZoneId getZone() {
		return _tz;
	}

	/**
	 * Returns the JVM ID of the underlying Java TimeZone object.
	 * @return The Java TimeZone ID
	 * @see TimeZone#getID()
	 */
	public String getID() {
		String id = _tz.getId();
		return "Z".equals(id) ? "Etc/UTC" : id;
	}

	/**
	 * Returns the abbreviation of this Time Zone.
	 * @return The user-supplied abbreviation, or <b>null </b> if none provided
	 */
	public String getAbbr() {
		return _abbr;
	}

	/**
	 * Returns this Time Zone's name.
	 * @return The name of the time zone; if no user-supplied name is present then use the Java name
	 */
	public String getName() {
		return (_displayName == null) ? _tz.getDisplayName(TextStyle.FULL_STANDALONE, Locale.US) : _displayName;
	}
	
	/**
	 * Returns if this time zone observers Daylight Savings time at some point in the future.
	 * @return TRUE if DST starts or ends at some future date, otherwise FALSE
	 */
	public boolean hasDST() {
		return _hasDST;
	}

	/**
	 * Converts this time zone into a String.
	 * @return This displays name, UTC offset and DST usage in the following format: <br>
	 * Name (ABBR) [GMT{+/-}hh:mm{/DST}]
	 */
	@Override
	public String toString() {
		int ofs = _tz.getRules().getStandardOffset(Instant.now()).getTotalSeconds() / 60;

		// Start with the name and add the abbreviation if any
		StringBuilder msg = new StringBuilder(getName());
		if (_abbr != null) {
			msg.append(" (");
			msg.append(_abbr);
			msg.append(')');
		}

		msg.append(" [UTC");
		if (ofs >= 0)
			msg.append('+');

		// Append hours & formatted minutes to include leading zeroes
		NumberFormat df = new DecimalFormat("00");
		msg.append(String.valueOf(ofs / 60)).append(':');
		msg.append(df.format(Math.abs(ofs % 60)));

		// Display DST flag
		if (_hasDST)
			msg.append("/DST");

		msg.append(']');
		return msg.toString();
	}

	/**
	 * Compares this Time Zone to another object by comparing the UTC offset.
	 * @param tz2 the TimeZone entry to compare to
	 */
	@Override
	public int compareTo(TZInfo tz2) {
		final Instant now = Instant.now();
		int tmpResult = _tz.getRules().getOffset(now).compareTo(tz2._tz.getRules().getOffset(now));
		return (tmpResult == 0) ? _displayName.compareTo(tz2._displayName) : tmpResult;
	}

	@Override
	public boolean equals(Object o2) {
		return (o2 instanceof TZInfo tz2) && (_tz.getId().equals(tz2._tz.getId()));
	}
	
	@Override
	public int hashCode() {
		return _tz.hashCode();
	}

	/**
	 * Resets the list of time zones. For unit testing only.
	 */
	protected static void reset() {
		_timeZones.clear();
	}
	
	@Override
	public String getRowClassName() {
		return _hasDST ? "opt1" : null;
	}
}