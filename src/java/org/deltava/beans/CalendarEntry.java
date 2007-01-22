// Copyright 2005, 2006, 2007 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans;

import java.util.Date;

/**
 * An interface to define beans that appear in a Calendar view table.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface CalendarEntry extends Comparable {
	
	public Date getDate();
}