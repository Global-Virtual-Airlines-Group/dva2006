// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store Examination and Check Ride statistics.
 * @author Luke
 * @version 3.0
 * @since 3.0
 */

public class ExamStatsEntry implements ViewEntry {
	
	private String _label;
	private String _subLabel;
	
	private int _total;
	private int _passed;
	private int _users;

	/**
	 * Initializes the bean.
	 * @param label the entry label
	 * @throws NullPointerException if label is null 
	 */
	public ExamStatsEntry(String label) {
		super();
		_label = label.trim();
	}
	
	/**
	 * Returns the entry label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns the entry sub-label.
	 * @return the sub-label
	 */
	public String getSubLabel() {
		return _subLabel;
	}
	
	/**
	 * Returns the total number of examinations or check rides in this entry's period.
	 * @return the total number of examinations or check rides
	 * @see ExamStatsEntry#setTotal(int)
	 */
	public int getTotal() {
		return _total;
	}
	
	/**
	 * Returns the number of passed examinations or check rides in this entry's period.
	 * @return the number of passed examinations or check rides
	 * @see ExamStatsEntry#setPassed(int)
	 */
	public int getPassed() {
		return _passed;
	}
	
	/**
	 * Returns the total number of distinct users in this time period.
	 * @return the number of users
	 * @see ExamStatsEntry#setUsers(int)
	 */
	public int getUsers() {
		return _users;
	}
	
	/**
	 * Updates the entry sub-label.
	 * @param label the sub-label
	 */
	public void setSubLabel(String label) {
		_subLabel = label;
	}
	
	/**
	 * Updates the total number of examinations or check rides.
	 * @param cnt the total
	 * @see ExamStatsEntry#getTotal()
	 */
	public void setTotal(int cnt) {
		_total = Math.max(0, cnt);
	}

	/**
	 * Updates the number of passed examinations or check rides.
	 * @param cnt the number of passed examinations
	 * @see ExamStatsEntry#getPassed()
	 */
	public void setPassed(int cnt) {
		_passed = Math.max(0, cnt);
	}
	
	/**
	 * Updates the total number of users in this time period.
	 * @param cnt the number of users
	 * @see ExamStatsEntry#getUsers()
	 */
	public void setUsers(int cnt) {
		_users = Math.max(0, cnt); 
	}
	
	@Override
	public String getRowClassName() {
		if (_total == 0)
			return null;
		else if ((_total == _passed))
			return "opt1";
		else if (_passed == 0)
			return "warn";
			
		return null;
	}
}