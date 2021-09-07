// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store Flight Schedule filter history. 
 * @author Luke
 * @version 10.1
 * @since 10.1
 */

public class ScheduleSourceHistory extends ScheduleSourceInfo implements AuthoredBean, ViewEntry {

	private int _time;
	private int _legs;
	private int _userID;
	
	/**
	 * Creates the bean.
	 * @param src the ScheduleSource
	 */
	public ScheduleSourceHistory(ScheduleSource src) {
		super(src);
	}
	
	/**
	 * Creates the bean from a ScheduleSourceInfo bean.
	 * @param inf the ScheduleSourceInfo
	 */
	public ScheduleSourceHistory(ScheduleSourceInfo inf) {
		super(inf);
	}
	
	@Override
	public int getAuthorID() {
		return _userID;
	}
	
	/**
	 * Returns the end time of the schedule filter operation.
	 * @return the end date/time
	 */
	public Instant getEndDate() {
		Instant sd = getDate();
		return (sd == null) ? null : sd.plusMillis(_time);
	}
	
	@Override
	public int getLegs() {
		return (_legs == 0) ? super.getLegs() : _legs;
	}
	
	/**
	 * Returns the execution time of the schedule filter operation.
	 * @return the execution time in milliseconds
	 */
	public int getTime() {
		return _time;
	}
	
	@Override
	public void setAuthorID(int id) {
		if (id != 0) DatabaseBean.validateID(_userID, id);
		_userID = id;
		setAutoImport(id == 0);
	}
	
	/**
	 * Updates the total number of flight legs with arrival times adjusted for DST.
	 * @param cnt the number of legs
	 */
	public void setAdjusted(int cnt) {
		_adjusted = cnt;
	}
	
	/**
	 * Updates the total number of flight legs.
	 * @param cnt the number of legs
	 */
	public void setLegs(int cnt) {
		_legs = cnt;
	}
	
	/**
	 * Updates the total number of flight legs skipped for this source.
	 * @param cnt the number of legs
	 */
	public void setSkipped(int cnt) {
		_skipped = cnt;
	}

	/**
	 * Updates the execution time of the schedule filter operation.
	 * @param ms the execution time in milliseconds
	 */
	public void setTime(int ms) {
		_time = ms;
	}

	@Override
	public String getRowClassName() {
		return (_userID == 0) ? null : "opt1";
	}
}