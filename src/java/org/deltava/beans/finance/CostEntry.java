// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.finance;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A bean to track costs.
 * @author Luke
 * @version 13.1
 * @since 13.1
 */

public class CostEntry implements Comparable<CostEntry> {
	
	private final String _category;
	private final Instant _date;
	private final int _amount;
	private String _desc;

	/**
	 * Creates the bean.
	 * @param category the cost cateogry
	 * @param dt the date incurred
	 * @param amt the the amount in cents
	 */
	public CostEntry(String category, Instant dt, int amt) {
		super();
		_category = category;
		_date = dt.truncatedTo(ChronoUnit.DAYS);
		_amount = amt;
	}

	/**
	 * Returns the cost category.
	 * @return the category name
	 */
	public String getCategory() {
		return _category;
	}
	
	/**
	 * Returns the date incurred.
	 * @return the date
	 */
	public Instant getDate() {
		return _date;
	}
	
	/**
	 * Returns the amount of the cost.
	 * @return the amount in cents
	 */
	public int getAmount() {
		return _amount;
	}

	/**
	 * Returns the cost description.
	 * @return the desctription
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Updates the cost description.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}

	@Override
	public int compareTo(CostEntry c2) {
		int tmpResult = _date.compareTo(c2._date);
		if (tmpResult == 0) tmpResult = _category.compareTo(c2._category);
		return (tmpResult == 0) ? Integer.compare(_amount, c2._amount) : tmpResult;
	}
}