// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

/**
 * A bean to store Examination / Check Ride / Question statistics.
 * @author Luke
 * @version 9.1
 * @since 9.1
 */

public class TestStatistics implements PassStatistics, java.io.Serializable {

	private int _total;
	private int _success;
	
	@Override
	public int getTotal() {
		return _total;
	}
	
	@Override
	public int getPassCount() {
		return _success;
	}
	
	/**
	 * Updates the total number of times this examination / question has been presented.
	 * @param t the total count
	 */
	public void setTotal(int t) {
		_total = t;
	}
	
	/**
	 * Updates the number of times this exam has been passed or the question has been correctly answered.
	 * @param s the number of successful invocations / correct answers
	 */
	public void setPassCount(int s) {
		_success = s;
	}
}