// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Airline-wide statistics.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class AirlineTotals implements Comparable<AirlineTotals>, Cacheable {
	
	/**
	 * Date the airline statistics commenced.
	 */
	public static final Calendar BIRTHDATE = new GregorianCalendar(2001, 6, 10);

	private long _effectiveDate;
	
	private int _totalLegs;
	private long _totalMiles;
	private double _totalHours;
	
	private int _totalPilots;
	private int _activePilots;
	
	private int _acarsLegs;
	private int _acarsMiles;
	private double _acarsHours;
	
	private int _onlineLegs;
	private long _onlineMiles;
	private double _onlineHours;
	
	private int _mtdLegs;
	private int _mtdMiles;
	private double _mtdHours;
	
	private int _ytdLegs;
	private int _ytdMiles;
	private double _ytdHours;
	
	private long _dbSize;
	private long _dbRows;
	
	/**
	 * Initializes the bean with an effective date.
	 * @param ed the effective date/time of the statistics as a 32-bit UNIX timestamp
	 * @see AirlineTotals#getEffectiveDate()
	 */
	public AirlineTotals(long ed) {
		super();
		_effectiveDate = ed;
	}
	
	/**
	 * Helper method to calculate the age of the airline.
	 * @return the age of the airline in days, including the birthdate and today
	 */
	public int getAge() {
		return (int) ((_effectiveDate - BIRTHDATE.getTimeInMillis()) / 86400000); 
	}
	
	/**
	 * Returns the effective date of these statistics.
	 * @return the date/time the statistics were generated as a 32-bit UNIX timestamp
	 */
	public long getEffectiveDate() {
		return _effectiveDate;
	}

	/**
	 * Returns the total number of legs flown.
	 * @return the number of legs
	 * @see AirlineTotals#setTotalLegs(int)
	 */
	public int getTotalLegs() {
		return _totalLegs;
	}
	
	/**
	 * Returns the total number of miles flown.
	 * @return the number of miles
	 * @see AirlineTotals#setTotalMiles(long)
	 */
	public long getTotalMiles() {
		return _totalMiles;
	}
	
	/**
	 * Returns the total number of hours flown.
	 * @return the number of hours
	 * @see AirlineTotals#setTotalHours(double)
	 */
	public double getTotalHours() {
		return _totalHours;
	}
	
	/**
	 * Returns the total number of pilots.
	 * @return the number of pilots
	 * @see AirlineTotals#setTotalPilots(int)
	 */
	public int getTotalPilots() {
		return _totalPilots;
	}
	
	/**
	 * Returns the number of active pilots.
	 * @return the number of active pilots
	 * @see AirlineTotals#setActivePilots(int)
	 */
	public int getActivePilots() {
		return _activePilots;
	}
	
	/**
	 * Returns the number of legs flown online.
	 * @return the number of legs
	 * @see AirlineTotals#setOnlineLegs(int)
	 */
	public int getOnlineLegs() {
		return _onlineLegs;
	}
	
	/**
	 * Returns the total number of hours flown online.
	 * @return the number of hours
	 * @see AirlineTotals#setOnlineHours(double)
	 */
	public double getOnlineHours() {
		return _onlineHours;
	}
	
	/**
	 * Returns the total number of miles flown online.
	 * @return the number of miles
	 * @see AirlineTotals#setOnlineMiles(long)
	 */
	public long getOnlineMiles() {
		return _onlineMiles;
	}
	
	/**
	 * Returns the number of legs flown with ACARS.
	 * @return the number of legs
	 * @see AirlineTotals#setACARSLegs(int)
	 */
	public int getACARSLegs() {
		return _acarsLegs;
	}
	
	/**
	 * Returns the total number of hours flown with ACARS.
	 * @return the number of hours
	 * @see AirlineTotals#setACARSHours(double)
	 */
	public double getACARSHours() {
		return _acarsHours;
	}
	
	/**
	 * Returns the total number of miles flown with ACARS.
	 * @return the number of miles
	 * @see AirlineTotals#setACARSMiles(int)
	 */
	public int getACARSMiles() {
		return _acarsMiles;
	}
	
	/**
	 * Returns the number of legs flown since the start of the current Month.
	 * @return the number of legs
	 * @see AirlineTotals#setMTDLegs(int)
	 */
	public int getMTDLegs() {
		return _mtdLegs;
	}
	
	/**
	 * Returns the number of hours flown since the start of the current Month.
	 * @return the number of hours
	 * @see AirlineTotals#setMTDHours(double)
	 */
	public double getMTDHours() {
		return _mtdHours;
	}
	
	/**
	 * Returns the number of miles flown since the start of the current Month.
	 * @return the number of miles
	 * @see AirlineTotals#setMTDMiles(int)
	 */
	public int getMTDMiles() {
		return _mtdMiles;
	}
	
	/**
	 * Returns the number of legs flown since the start of the current Year.
	 * @return the number of legs
	 * @see AirlineTotals#setYTDLegs(int)
	 */
	public int getYTDLegs() {
		return _ytdLegs;
	}
	
	/**
	 * Returns the number of hours flown since the start of the current Year.
	 * @return the number of hours
	 * @see AirlineTotals#setYTDHours(double)
	 */
	public double getYTDHours() {
		return _ytdHours;
	}
	
	/**
	 * Returns the number of miles flown since the start of the current Year.
	 * @return the number of miles
	 * @see AirlineTotals#setYTDMiles(int)
	 */
	public int getYTDMiles() {
		return _ytdMiles;
	}
	
	/**
	 * Returns the total size of all database tables.
	 * @return the size of the database tables in bytes
	 * @see AirlineTotals#setDBSize(long)
	 */
	public long getDBSize() {
		return _dbSize;
	}
	
	/**
	 * Returns the total number of rows in all database tables.
	 * @return the total number of rows in all tables
	 * @see AirlineTotals#setDBRows(long)
	 */
	public long getDBRows() {
		return _dbRows;
	}
	
	/**
	 * Updates the total number of legs flown.
	 * @param legs the number of legs
	 * @see AirlineTotals#getTotalLegs()
	 */
	public void setTotalLegs(int legs) {
		_totalLegs = legs;
	}
	
	/**
	 * Updates the total number of miles flown.
	 * @param miles the number of miles
	 * @see AirlineTotals#getTotalMiles()
	 */
	public void setTotalMiles(long miles) {
		_totalMiles = miles;
	}
	
	/**
	 * Updates the total number of hours flown.
	 * @param hours the number of hours
	 * @see AirlineTotals#getTotalHours()
	 */
	public void setTotalHours(double hours) {
		_totalHours = hours;
	}
	
	/**
	 * Updates the total number of pilots.
	 * @param pilots the number of pilots
	 * @see AirlineTotals#getTotalPilots()
	 */
	public void setTotalPilots(int pilots) {
		_totalPilots = pilots;
	}
	
	/**
	 * Updates the total number of active pilots.
	 * @param pilots the number of actve pilots
	 * @see AirlineTotals#getActivePilots()
	 */
	public void setActivePilots(int pilots) {
		_activePilots = pilots;
	}
	
	/**
	 * Updates the total number of legs flown with ACARS.
	 * @param legs the number of legs
	 * @see AirlineTotals#getACARSLegs()
	 */
	public void setACARSLegs(int legs) {
		_acarsLegs = legs;
	}
	
	/**
	 * Updates the total number of miles flown with ACARS.
	 * @param miles the number of miles
	 * @see AirlineTotals#getACARSMiles()
	 */
	public void setACARSMiles(int miles) {
		_acarsMiles = miles;
	}
	
	/**
	 * Updates the total number of hours flown with ACARS.
	 * @param hours the number of hours
	 * @see AirlineTotals#getACARSHours()
	 */
	public void setACARSHours(double hours) {
		_acarsHours = hours;
	}
	
	/**
	 * Updates the total number of legs flown online.
	 * @param legs the number of legs
	 * @see AirlineTotals#getOnlineLegs()
	 */
	public void setOnlineLegs(int legs) {
		_onlineLegs = legs;
	}
	
	/**
	 * Updates the total number of miles flown online.
	 * @param miles the number of miles
	 * @see AirlineTotals#getOnlineMiles()
	 */
	public void setOnlineMiles(long miles) {
		_onlineMiles = miles;
	}
	
	/**
	 * Updates the total number of hours flown online.
	 * @param hours the number of hours
	 * @see AirlineTotals#getOnlineHours()
	 */
	public void setOnlineHours(double hours) {
		_onlineHours = hours;
	}
	
	/**
	 * Updates the total number of legs flown since the start of the current Month.
	 * @param legs the number of legs
	 * @see AirlineTotals#getMTDLegs()
	 */
	public void setMTDLegs(int legs) {
		_mtdLegs = legs;
	}
	
	/**
	 * Updates the total number of hours flown since the start of the current Month.
	 * @param hours the number of hours
	 * @see AirlineTotals#getMTDHours()
	 */
	public void setMTDHours(double hours) {
		_mtdHours = hours;
	}
	
	/**
	 * Updates the total number of miles flown since the start of the current Month.
	 * @param miles the number of miles
	 * @see AirlineTotals#getMTDMiles()
	 */
	public void setMTDMiles(int miles) {
		_mtdMiles = miles;
	}

	/**
	 * Updates the total number of legs flown since the start of the current Yes.
	 * @param legs the number of legs
	 * @see AirlineTotals#getYTDLegs()
	 */
	public void setYTDLegs(int legs) {
		_ytdLegs = legs;
	}
	
	/**
	 * Updates the total number of hours flown since the start of the current Year.
	 * @param hours the number of hours
	 * @see AirlineTotals#getYTDHours()
	 */
	public void setYTDHours(double hours) {
		_ytdHours = hours;
	}
	
	/**
	 * Updates the total number of miles flown since the start of the current Year.
	 * @param miles the number of miles
	 * @see AirlineTotals#getYTDMiles()
	 */
	public void setYTDMiles(int miles) {
		_ytdMiles = miles;
	}

	/**
	 * Updates the total size of all database tables.
	 * @param size the size of the tables in bytes
	 * @see AirlineTotals#getDBSize()
	 */
	public void setDBSize(long size) {
		_dbSize = size;
	}
	
	/**
	 * Updates the total number of rows in all database tables.
	 * @param rows the number of rows in all tables
	 * @see AirlineTotals#getDBRows()
	 */
	public void setDBRows(long rows) {
		_dbRows = rows;
	}
	
	/**
	 * Compares the effective date/times.
	 */
	public int compareTo(AirlineTotals at2) {
		return new Long(_effectiveDate).compareTo(new Long(at2._effectiveDate));
	}
	
	/**
	 * Returns the cache key for this object.
	 * @return the class object
	 */
	public Object cacheKey() {
		return AirlineTotals.class;
	}
}