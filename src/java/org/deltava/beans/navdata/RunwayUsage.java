// Copyright 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.UseCount;

/**
 * A bean to store runway and usage data.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class RunwayUsage extends Runway implements UseCount {
	
	private int _useCount;
	private int _pct;
	
	/**
	 * Creates the bean.
	 * @param lat the runway latitude
	 * @param lng the runway longitude
	 */
	public RunwayUsage(double lat, double lng) {
		super(lat, lng);
	}
	
	@Override
	public int getUseCount() {
		return _useCount;
	}

	/**
	 * Returns the percentage of total uses for this Runway.
	 * @return the percentage
	 */
	public int getPercentage() {
		return _pct;
	}

	/**
	 * Updates the number of times this Runway was used.
	 * @param uses the number of uses
	 */
	public void setUseCount(int uses) {
		_useCount = uses;
	}

	/**
	 * Updates the percentage of total uses for this Runway.
	 * @param pct the percentage
	 */
	public void setPercentage(int pct) {
		_pct = pct;
	}
	
	@Override
	public int hashCode() {
		return getComboAlias().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof RunwayUsage) && (hashCode() == o.hashCode());
	}
	
	@Override
	public String getComboName() {
		StringBuilder buf = new StringBuilder(super.getComboName());
		buf.append(" [");
		buf.append(_useCount);
		buf.append("x, ");
		buf.append(_pct);
		buf.append("%]");
		return buf.toString();
	}
	
	@Override
	public int compareTo(NavigationDataBean nd2) {
		if (!(nd2 instanceof RunwayUsage)) return super.compareTo(nd2);
		RunwayUsage sr2 = (RunwayUsage) nd2;
		int tmpResult = Integer.compare(_useCount, sr2._useCount);
		if (tmpResult == 0)
			tmpResult = getName().compareTo(sr2.getName());
		
		return (tmpResult == 0) ? getCode().compareTo(sr2.getCode()) : tmpResult;
	}
}