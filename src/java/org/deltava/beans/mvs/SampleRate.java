// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

import org.deltava.beans.ComboAlias;

/**
 * Valid MVS sample rates.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public enum SampleRate implements Comparable<SampleRate>, ComboAlias {

	SR11K(11025), SR22K(22050), SR44K(44100);
	
	private int _rate;
	
	/**
	 * Creates a SampleRate from a numeric bit rate.
	 */
	public static SampleRate getRate(int rate) {
		switch (rate) {
			case 44100:
				return SR44K;
			case 22050:
				return SR22K;
			default:
				return SR11K;
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
	
	public String getComboName() {
		return name();
	}
	
	public String getComboAlias() {
		return String.valueOf(_rate);
	}
}