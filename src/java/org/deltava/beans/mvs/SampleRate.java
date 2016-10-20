// Copyright 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

import org.deltava.beans.ComboAlias;

/**
 * Valid MVS sample rates.
 * @author Luke
 * @version 7.2
 * @since 4.0
 */

public enum SampleRate implements ComboAlias {

	SR5K(5000), SR6K(6000), SR8K(8000);
	
	private final int _rate;
	
	/**
	 * Creates a SampleRate from a numeric bit rate.
	 * @param rate the bit rate
	 * @return a SampleRate
	 */
	public static SampleRate getRate(int rate) {
		switch (rate) {
			case 6000:
				return SR6K;
			case 5000:
				return SR5K;
			default:
				return SR8K;
		}
	}
	
	SampleRate(int rate) {
		_rate = rate;
	}
	
	/**
	 * Returns the sample rate.
	 * @return the rate in Hz
	 */
	public int getRate() {
		return _rate;
	}
	
	@Override
	public String getComboName() {
		return name();
	}

	@Override
	public String getComboAlias() {
		return String.valueOf(_rate);
	}
}