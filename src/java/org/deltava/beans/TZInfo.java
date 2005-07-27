// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.text.DecimalFormat;

/**
 * A class for dealing with Time Zones.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TZInfo implements ComboAlias, Comparable, java.io.Serializable {

    private static Map _timeZones = new HashMap();

    private TimeZone _tz;
    private String _displayName;
    private String _abbr;

    private static final DecimalFormat df = new DecimalFormat("00");

    private TZInfo(String tzName, String dName, String abbr) {
        super();
        _tz = TimeZone.getTimeZone(tzName);
        _displayName = dName;
        if (abbr != null)
            _abbr = abbr.toUpperCase();
    }

    /**
     * Creates a Time Zone Wrapper with user-provided name and abbreviation
     * @param tzName A standard Java Time Zone ID
     * @param dName If not null, the name to display instead of the JVM name
     * @param abbr An abbreviation for this time zone
     * @return the Time Zone
     * @see TimeZone#getAvailableIDs()
     * @see TimeZone#getTimeZone(java.lang.String)
     */
    public static TZInfo init(String tzName, String dName, String abbr) {
        synchronized (_timeZones) {
            TZInfo tz = (TZInfo) _timeZones.get(tzName);
            if (tz == null) {
                tz = new TZInfo(tzName, dName, abbr);
                _timeZones.put(tzName, tz);
            }
            
            return tz;
        }
    }

    /**
     * Gets and optionally Initializes a Time Zone wrapper using the standard Java Time Zone name.
     * @param tzName A standard Java Time Zone ID
     * @see TimeZone#getAvailableIDs()
     * @see TimeZone#getTimeZone(java.lang.String)
     */
    public static TZInfo init(String tzName) {
        return init(tzName, null, null);
    }

    /**
     * Creates a Time Zone wrapper using the standard Java Time Zone object.
     * @param tz A standard Java Time Zone object
     */
    public static TZInfo init(TimeZone tz) {
        return init(tz.getID(), null, null);
    }
    
    /**
     * Creates/returns a Time Zone wrapper for the local time zone.
     * @return a Time Zome wrapper for the local zone
     */
    public static TZInfo local() {
        return init(TimeZone.getDefault());
    }
    
    /**
     * Creates/returns a Time Zone wrapper for Greenwich Mean Time
     * @return a Time Zone wrapper for GMT/UTC
     */
    public static TZInfo gmt() {
        return init("GMT");
    }

    public String getComboAlias() {
        return getID();
    }

    public String getComboName() {
        return toString();
    }
    
    /**
     * Returns all initialized Time Zones.
     * @return a sorted Set of TZInfo objects
     */
    public static Collection getAll() {
        return new TreeSet(_timeZones.values());
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
     * @return This displays name, UTC offset and DST usage in the following format: <br>Name (ABBR)
     * [GMT{+/-}hh:mm{/DST}]
     */
    public String toString() {
        int ofs = _tz.getRawOffset() / 60000;

        // Start with the name and add the abbreviation if any
        StringBuffer msg = new StringBuffer(getName());
        if (_abbr != null) {
            msg.append(" (");
            msg.append(_abbr);
            msg.append(')');
        }

        msg.append(" [GMT");
        if (ofs >= 0)
            msg.append('+');

        // Append hours & formatted minutes to include leading zeroes
        msg.append(String.valueOf(ofs / 60));
        msg.append(':');
        msg.append(df.format(Math.abs(ofs % 60)));

        // Display DST flag
        if (_tz.useDaylightTime())
            msg.append("/DST");

        msg.append(']');
        return msg.toString();
    }

    /**
     * Converts a user-supplied UNIX 32-bit timestamp to UTC
     * @param d A UNIX 32-bit timestamp
     * @return A new 32-bit timestamp that contains the specified date/time when converted to UTC
     */
    public long getUTC(long d) {
        return d + (_tz.getOffset(d) * -1);
    }

    /**
     * Compares this Time Zone to another object by comparing the UTC offset
     * @throws ClassCastException If we pass in anything other than a TZWrapper
     */
    public int compareTo(Object o2) {
        TZInfo tz2 = (TZInfo) o2;
        if ((o2 == null) || (_tz.getRawOffset() > tz2.getTimeZone().getRawOffset())) {
            return 1;
        } else if (_tz.getRawOffset() < tz2.getTimeZone().getRawOffset()) {
            return -1;
        }

        return 0;
    }

    /**
     * Compares this Time Zone to another TimeZone by comparing the JVM time zone ID
     */
    public boolean equals(Object o2) {
        try {
            TZInfo tz2 = (TZInfo) o2;
            return _tz.getID().equals(tz2.getID());
        } catch (Exception e) {
            return false;
        }
    }
    
    protected static void reset() {
    	_timeZones.clear();
    }
}