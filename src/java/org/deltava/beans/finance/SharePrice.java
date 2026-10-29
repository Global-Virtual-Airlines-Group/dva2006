// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.finance;

import org.deltava.beans.Auditable;

/**
 * A bean to track share prices and amounts. 
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class SharePrice implements Auditable, Comparable<SharePrice>  {
	
	private final int _year;
	private final int _price;
	private int _cnt;
	private boolean _active;

	/**
	 * Creates the bean.
	 * @param year the year
	 * @param price the share price in cents
	 */
	public SharePrice(int year, int price) {
		super();
		_year = year;
		_price = price;
	}

	/**
	 * Returns the fiscal year.
	 * @return the year
	 */
	public int getYear() {
		return _year;
	}
	
	/**
	 * Returns the price per share.
	 * @return the price in cents
	 */
	public int getPrice() {
		return _price;
	}
	
	/**
	 * Returns the numebr of shares issued in this fiscal year.
	 * @return the share count
	 */
	public int getCount() {
		return _cnt;
	}
	
	/**
	 * Returns whether fundraising is active for this fiscal year.
	 * @return TRUE if active, otherwise FALSE
	 */
	public boolean getActive() {
		return _active;
	}

	/**
	 * Updates the number of shares issued in this fiscal year.
	 * @param cnt
	 */
	public void setCount(int cnt) {
		_cnt = Math.max(0, cnt);
	}

	/**
	 * Updates whether fundraising is active for this fiscal year.
	 * @param isActive TRUE if active, otherwise FALSE
	 */
	public void setActive(boolean isActive) {
		_active = isActive;
	}
	
	@Override
	public int hashCode() {
		return _year;
	}
	
	@Override
	public int compareTo(SharePrice sp2) {
		return Integer.compare(_year, sp2._year);
	}

	@Override
	public String getAuditID() {
		return String.valueOf(_year);
	}
}