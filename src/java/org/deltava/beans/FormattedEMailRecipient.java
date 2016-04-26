// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import org.deltava.beans.schedule.Airport.Code;

/**
 * An interface to describe recipients of formatted e-mail messages, where
 * numbers, airports and distances are formatted according to user preferences.
 * @author Luke
 * @version 7.0
 * @since 6.2
 */

public interface FormattedEMailRecipient extends EMailAddress {

	/**
	 * Returns the preferred date format pattern.
	 * @return the date format pattern
	 */
	public String getDateFormat();

	/**
	 * Returns the preferred time format pattern.
	 * @return the time format pattern
	 */
	public String getTimeFormat();

	/**
	 * Returns the preferred number format pattern.
	 * @return the number format pattern
	 */
	public String getNumberFormat();
	
	/**
	 * Return this preferred Time Zone.
	 * @return the time zone
	 */
	public TZInfo getTZ();
	
	/**
	 * Returns the preferred distance unit.
	 * @return the unit type
	 */
	public DistanceUnit getDistanceType();
	
	/**
	 * Returns the preferred airport code type (IATA/ICAO).
	 * @return the Airport Code type
	 */
	public Code getAirportCodeType();
}