// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

/**
 * A bean to store Flight Report disposal queue statistics.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class DisposalQueueStats {

	private final Date _dt;
	private final int _size;
	private final double _avgAge;
	
	private final Collection<EquipmentCount> _cnts = new TreeSet<EquipmentCount>();
	
	private class EquipmentCount implements Comparable<EquipmentCount> {
		private final String _eqType;
		private final int _count;
		
		EquipmentCount(String eqType, int count) {
			super();
			_eqType = eqType;
			_count = Math.max(0, count);
		}
		
		public String getEquipmentType() {
			return _eqType;
		}
		
		public int getCount() {
			return _count;
		}
		
		public int compareTo(EquipmentCount ec2) {
			int tmpResult = Integer.valueOf(_count).compareTo(Integer.valueOf(ec2._count));
			return (tmpResult == 0) ? _eqType.compareTo(ec2._eqType) : tmpResult;
		}
	}

	/**
	 * Creates the bean.
	 * @param dt the effective date/time
	 * @param total the total number of pending Flight Reports
	 * @param avgAge the average pending time of non-held flight reports in hours
	 */
	public DisposalQueueStats(Date dt, int total, double avgAge) {
		super();
		_dt = dt;
		_size = Math.max(0, total);
		_avgAge = Math.max(0, avgAge);
	}
	
	/**
	 * Returns the effective date.
	 * @return the effective date/time
	 */
	public Date getDate() {
		return _dt;
	}
	
	/**
	 * Returns the total number of pending Flight Reports.
	 * @return the number of flight reports
	 */
	public int getSize() {
		return _size;
	}
	
	/**
	 * Returns the average age of non-held flight reports.
	 * @return the average age in hours
	 */
	public double getAverageAge() {
		return _avgAge;
	}
	
	/**
	 * Adds an equipment-specific pending flight report count.
	 * @param eqType the equipment type
	 * @param cnt the number of pending flight reports
	 */
	public void addCount(String eqType, int cnt) {
		_cnts.add(new EquipmentCount(eqType, cnt));
	}
	
	/**
	 * Returns equipment-specific pending flight report counts.
	 * @return a Map of pending counts, keyed by equipment type
	 */
	public Map<String, Integer> getCounts() {
		Map<String, Integer> results = new LinkedHashMap<String, Integer>();
		for (EquipmentCount ec : _cnts)
			results.put(ec.getEquipmentType(), Integer.valueOf(ec.getCount()));
		
		return results;
	}
}