// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.Date;

/**
 * A bean to store airport METAR data.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class METAR extends WeatherDataBean {
	
	/**
	 * Initializes the bean.
	 * @param code the observation station code
	 */
	public METAR(String code) {
		super(code);
	}
	
	/**
	 * Sets the expiration date of the bean (15 minutes after effective date).
	 */
	public Date getExpiryDate() {
		return new Date(getDate().getTime() + 900000);
	}
	
	public String getType() {
		return "METAR";
	}

	public int hashCode() {
		return cacheKey().hashCode();
	}
}