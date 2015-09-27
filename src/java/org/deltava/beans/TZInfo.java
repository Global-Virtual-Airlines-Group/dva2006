// Copyright 2005, 2007, 2008, 2010, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.text.*;

/**
 * A class for dealing with Time Zones.
 * @author Luke
 * @version 6.2
 * @since 1.0
 */

public class TZInfo implements java.io.Serializable, ComboAlias, Comparable<TZInfo>, ViewEntry {

	/**
	 * Time Zone for GMT/UTC.
	 */
	public static final TZInfo UTC = new TZInfo("Etc/Universal", "Coordinated Universal Time", "UTC");
	private static final TZInfo _local = new TZInfo(TimeZone.getDefault().getID(), null, null);

	private static final Map<String, TZInfo> _timeZones = new HashMap<String, TZInfo>();

	private final TimeZone _tz;
	private final String _displayName;
	private final String _abbr;
	
	// Init default time zones
	static {
		_timeZones.put("UTC", UTC);
		_timeZones.put("local", _local);
	}
	
	private TZInfo(String tzName, String dName, String abbr) {
		super();
		_tz = TimeZone.getTimeZone(tzName);
		_displayName = dName;
		_abbr = (abbr != null) ? abbr.toUpperCase().trim() : null;
	}

	/**
	 * Creates a Time Zone Wrapper with user-provided name and abbreviation.
	 * @param tzName A standard Java Time Zone ID
	 * @param dName If not null, the name to display instead of the JVM name
	 * @param abbr An abbreviation for this time zone
	 * @return the TZInfo instance created
	 * @see TimeZone#getAvailableIDs()
	 * @see TimeZone#getTimeZone(java.lang.String)
	 */
	public static TZInfo init(String tzName, String dName, String abbr) {
		TZInfo tz = new TZInfo(tzName, dName, abbr);
		_timeZones.put(tzName, tz);
		return tz;
	}

	/**
	 * Gets a Time Zone wrapper using the standard Java Time Zone name.
	 * @param tzName A standard Java Time Zone ID
	 * @see TimeZone#getAvailableIDs()
	 * @see TimeZone#getTimeZone(String)
	 */
	public static TZInfo get(String tzName) {
		return _timeZones.get(tzName);
	}

	/**
	 * Creates/returns a Time Zone wrapper for the local time zone.
	 * @return a Time Zome wrapper for the local zone
	 */
	public static TZInfo local() {
		return _local;
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
	public TimeZone getTimeZone() {
		return _tz;
	}

	/**
	 * Returns the JVM ID of the underlying Java TimeZone object
	 * @return The Java TimeZone ID
	 * @see TimeZone#getID()
	 */
	public String getID() {
		return _tz.getID();
	}

	/**
	 * Returns the abbreviation of this Time Zone
	 * @return The user-supplied abbreviation, or <b>null </b> if none provided
	 */
	public String getAbbr() {
		return _abbr;
	}

	/**
	 * Returns this Time Zone's name
	 * @return The name of the time zone; if no user-supplied name is present then use the Java name
	 */
	public String getName() {
		return (_displayName == null) ? _tz.getDisplayName(false, TimeZone.LONG) : _displayName;
	}

	/**
	 * Converts this time zone into a String
	 * @return This displays name, UTC offset and DST usage in the following format: <br>
	 * Name (ABBR) [GMT{+/-}hh:mm{/DST}]
	 */
	@Override
	public String toString() {
		int ofs = _tz.getRawOffset() / 60000;

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
		if (_tz.useDaylightTime())
			msg.append("/DST");

		msg.append(']');
		return msg.toString();
	}

	/**
	 * Converts a user-supplied UNIX 32-bit timestamp to UTC.
	 * @param d A UNIX 32-bit timestamp
	 * @return A new 32-bit timestamp that contains the specified date/time when converted to UTC
	 */
	public long getUTC(long d) {
		return d + (_tz.getOffset(d) * -1);
	}

	/**
	 * Compares this Time Zone to another object by comparing the UTC offset.
	 * @param tz2 the TimeZone entry to compare to
	 */
	public int compareTo(TZInfo tz2) {
		if ((tz2 == null) || (_tz.getRawOffset() > tz2.getTimeZone().getRawOffset()))
			return 1;
		else if (_tz.getRawOffset() < tz2.getTimeZone().getRawOffset())
			return -1;

		return _tz.getID().compareTo(tz2.getID());
	}

	/**
	 * Compares this Time Zone to another TimeZone by comparing the JVM time zone ID.
	 */
	@Override
	public boolean equals(Object o2) {
		return (o2 instanceof TZInfo) && (_tz.getID().equals(((TZInfo) o2)._tz.getID()));
	}
	
	@Override
	public int hashCode() {
		return _tz.getID().hashCode();
	}

	protected static void reset() {
		_timeZones.clear();
	}
	
	/**
	 * Returns the CSS class name when displayed in a view table.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		return _tz.useDaylightTime() ? "opt1" : null;
	}
}