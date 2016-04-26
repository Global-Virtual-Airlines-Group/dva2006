// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

/**
 * A bean to combine an Accomplishment with an Achievement date. 
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class DatedAccomplishment extends Accomplishment {
	
	private final Instant _dt;

	/**
	 * Creates the bean.
	 * @param dt the date/time the Accomplishment was achieved.
	 * @param a the Accomplishment
	 */
	public DatedAccomplishment(Instant dt, Accomplishment a) {
		super(a.getName());
		setID(a.getID());
		setUnit(a.getUnit());
		setActive(a.getActive());
		setValue(a.getValue());
		setPilots(a.getPilots());
		setColor(a.getColor());
		setOwner(a.getOwner());
		_dt = dt;
	}

	/**
	 * Returns the date the Accomplishment was achieved.
	 * @return the date/time of the Accomplishment
	 */
	public Instant getDate() {
		return _dt;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_dt.toString());
		buf.append('-');
		buf.append(getName());
		return buf.toString();
	}
	
	/**
	 * Compares two DatedAccomplishments by comparing their accomplishment date/times. If they
	 * are equal, it sorts using Accomplishment's native comparator.
	 */
	@Override
	public int compareTo(Object o) {
		DatedAccomplishment da2 = (DatedAccomplishment) o;
		int tmpResult = _dt.compareTo(da2._dt);
		return (tmpResult == 0) ? super.compareTo(da2) : tmpResult;
	}
}