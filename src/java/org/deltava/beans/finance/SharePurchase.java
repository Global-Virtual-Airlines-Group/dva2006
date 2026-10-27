// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.finance;

import org.deltava.beans.AuthoredBean;
import org.deltava.beans.DatabaseBean;

/**
 * A bean to store share purhcase information.
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class SharePurchase implements AuthoredBean, Comparable<SharePurchase> {
	
	private int _ownerID;
	private final int _year;
	private final int _count;

	/**
	 * Creates the entry.
	 * @param year the fiscal year
	 * @param ownerID the owner database ID
	 * @param cnt the number of shares 
	 */
	public SharePurchase(int year, int ownerID, int cnt) {
		super();
		_year = year;
		setAuthorID(ownerID);
		_count = Math.max(0, cnt);
	}

	/**
	 * Returns the fiscal year of the purchase.
	 * @return the year
	 */
	public int getYear() {
		return _year;
	}
	
	@Override
	public int getAuthorID() {
		return _ownerID;
	}
	
	/**
	 * Returns the number of shares purchased.
	 * @return the number of shares
	 */
	public int getCount() {
		return _count;
	}
	
	@Override
	public void setAuthorID(int id) {
		DatabaseBean.validateID(_ownerID, id);
		_ownerID = id;
	}
	
	@Override
	public int compareTo(SharePurchase sp2) {
		int tmpResult = Integer.compare(_year, sp2._year);
		if (tmpResult == 0) tmpResult = Integer.compare(_ownerID, sp2._ownerID);
		return (tmpResult == 0) ? Integer.compare(_count, sp2._count) : tmpResult;
	}
}