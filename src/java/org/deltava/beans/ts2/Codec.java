// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts2;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration to store TeamSpeak 2 channel codecs. 
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public enum Codec implements ComboAlias {
	CELP51("CELP 5.1 kbit"),	CELP63("CELP 6.3 kbit"), GSM148("GSM 14.8 kbit"), GSM164("GSM 16.4 kbit"),
	CELPW52("CELP Windows 5.2 kbit"), SPEEX34("Speex 3.4 kbit"), SPEEX52("Speex 5.2 kbit"),
	SPEEX72("Speex 7.2 kbit"), SPEEX93("Speex 9.3 kbit"), SPEEX123("Speex 12.3 kbit"),
	SPEEX163("Speex 16.3 kbit"), SPEEX195("Speex 19.5 kbit"), SPEEX259("Speex 25.9 kbit");

	private final String _desc;
	
	Codec(String desc) {
		_desc = desc;
	}

	/**
	 * Returns the codec description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	@Override
	public String getComboName() {
		return _desc;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
}