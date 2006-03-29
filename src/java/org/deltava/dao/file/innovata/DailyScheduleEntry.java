// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import org.deltava.beans.schedule.*;

/**
 * A Flight Schedule Entry that counts how many days per week the leg is operated.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class DailyScheduleEntry extends ScheduleEntry {
	
	private int _dayCount;

	/**
	 * Creates a new Schedule Entry.
	 * @param a the Airline bean
	 * @param fNumber the Flight Number
	 * @param leg the Leg number
	 */
	DailyScheduleEntry(Airline a, int fNumber, int leg) {
		super(a, fNumber, leg);
	}

	public int getDays() {
		return _dayCount;
	}
	
	public void setDays(String days) {
		_dayCount = 0;
		for (int x = 0; x < days.length(); x++)
			_dayCount += Character.isDigit(days.charAt(x)) ? 1 : 0;
	}
}