// Copyright 2008, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

/**
 * A bean to store Terminal Area Forecast data.
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class TAF extends WeatherDataBean {
	
	private boolean _amended;

	@Override
	public String getIconColor() {
		return BLUE;
	}

	@Override
	public Type getType() {
		return Type.TAF;
	}

	@Override
	public int hashCode() {
		return cacheKey().hashCode();
	}
	
	/**
	 * Returns if this is a TAF amended since original issue. 
	 * @return TRUE if amended, otherwise FALSE
	 */
	public boolean getAmended() {
		return _amended;
	}

	/**
	 * Sets whether this is a TAF amended since original issue. 
	 * @param isAmended TRUE if amended, otherwise FALSE
	 */
	public void setAmended(boolean isAmended) {
		_amended = isAmended;
	}
}