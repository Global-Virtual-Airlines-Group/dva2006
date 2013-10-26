// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.ViewEntry;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Navigation Data cycle information.
 * @author Luke
 * @version 5.2
 * @since 5.1
 */

public class CycleInfo implements Cacheable, ViewEntry, Comparable<CycleInfo> {

	private final String _id;
	private final int _year;
	private final int _seq;
	private Date _releasedOn;
	private boolean _isLoaded;
	
	/**
	 * Creates the cycle information bean.
	 * @param cycleID the navigqation cycle ID
	 */
	public CycleInfo(String cycleID) {
		this(cycleID, null);
	}
	
	/**
	 * Creates the cycle information bean.
	 * @param cycleID the navigqation cycle ID
	 * @param releaseDate the cycle release datae
	 */
	public CycleInfo(String cycleID, Date releaseDate) {
		super();
		_id = cycleID;
		_year = Integer.parseInt(_id.substring(0, 2)) + 2000;
		_seq = Integer.parseInt(_id.substring(2));
		_releasedOn = releaseDate;
	}
	
	/**
	 * Returns the year.
	 * @return the year
	 */
	public int getYear() {
		return _year;
	}
	
	/**
	 * Returns the sequence number.
	 * @return the sequence
	 */
	public int getSequence() {
		return _seq;
	}

	/**
	 * Returns the Cycle ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the cycle release date.
	 * @return the release date
	 */
	public Date getReleasedOn() {
		return _releasedOn;
	}
	
	/**
	 * Returns whether this cycle is currently loaded.
	 * @return TRUE if loaded, otherwise FALSE
	 */
	public boolean isLoaded() {
		return _isLoaded;
	}
	
	/**
	 * Updates the cycle release datae.
	 * @param dt the release date
	 */
	public void setReleasedOn(Date dt) {
		_releasedOn = dt;
	}
	
	/**
	 * Sets whether this cycle is currently loaded.
	 * @param isLoaded TRUE if loaded, otherwise FALSE
	 */
	public void setLoaded(boolean isLoaded) {
		_isLoaded = isLoaded;
	}
	
	@Override
	public String getRowClassName() {
		return _isLoaded ? "opt1" : null;
	}

	@Override
	public int compareTo(CycleInfo c2) {
		int tmpResult = Integer.valueOf(_year).compareTo(Integer.valueOf(c2._year));
		return (tmpResult == 0) ? Integer.valueOf(_seq).compareTo(Integer.valueOf(c2._seq)) : tmpResult;
	}

	@Override
	public Object cacheKey() {
		return _id;
	}

	public String toString() {
		return _id;
	}
	
	public int hashCode() {
		return _id.hashCode();
	}
	
	/**
	 * Guesses at the current cycle based on year and date.
	 * @return a CycleInfo bean
	 */
	public static CycleInfo getCurrent() {
		Calendar cld = Calendar.getInstance();
		String id = String.valueOf(cld.get(Calendar.YEAR) - 2000) + String.format("00", Integer.valueOf(cld.get(Calendar.MONTH) + 1));
		return new CycleInfo(id);
	}
}