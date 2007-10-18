// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

/**
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MembershipTotals extends DatabaseBean {

	private Date _dt;
	private int _count;
	
	/**
	 * 
	 */
	public MembershipTotals(Date dt) {
		super();
		_dt = dt;
	}
	
	public Date getDate() {
		return _dt;
	}
	
	public int getCount() {
		return _count;
	}
	
	public void setCount(int count) {
		if (count < 0)
			throw new IllegalArgumentException("Invalid count - " + count);
		
		_count = count;
	}
}