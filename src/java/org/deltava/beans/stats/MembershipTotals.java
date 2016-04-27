// Copyright 2006, 2007, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store membership totals by date.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class MembershipTotals extends DatabaseBean {

	private final Instant _dt;
	private int _count;
	
	/**
	 * Creates a new bean.
	 * @param dt the date
	 */
	public MembershipTotals(Instant dt) {
		super();
		_dt = dt;
	}
	
	public Instant getDate() {
		return _dt;
	}
	
	public int getCount() {
		return _count;
	}
	
	public void setCount(int count) {
		_count = Math.max(0, count);
	}
}