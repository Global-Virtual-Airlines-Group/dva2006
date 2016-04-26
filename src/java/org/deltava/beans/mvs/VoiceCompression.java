// Copyright 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

import org.deltava.beans.ComboAlias;

/**
 * Valid MVS data compression schemes.
 * @author Luke
 * @version 7.0
 * @since 4.0
 */

public enum VoiceCompression implements ComboAlias {

	NONE(0, "None"), ALAW(1, "G.711 a-law"), ACMALAW(2, "ACM G.711 a-law"), GSM(3, "GSM 6.10"),
	ADPCM(4, "Microsoft ADPCM"), MLAW(5, "ACM G.711 mu-law"), SPEEX(6, "Speex Narrow");
	
	private final int _type;
	private final String _name;
	
	VoiceCompression(int type, String name) {
		_type = type;
		_name = name;
	}
	
	public int getType() {
		return _type;
	}
	
	public String getName() {
		return _name;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
}