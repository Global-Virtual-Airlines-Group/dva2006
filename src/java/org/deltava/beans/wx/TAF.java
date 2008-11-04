// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.Date;

/**
 * A bean to store Terminal Area Forecast data.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class TAF extends WeatherDataBean {

	/**
	 * Sets the expiration date of the bean (30 minutes after effective date).
	 */
	public Date getExpiryDate() {
		return new Date(getDate().getTime() + 1800000);
	}
	
	public String getIconColor() {
		return BLUE;
	}

	public String getType() {
		return "TAF";
	}

	public int hashCode() {
		return cacheKey().hashCode();
	}
}