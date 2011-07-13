// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

import org.deltava.beans.ComboAlias;

/**
 * Valid MVS data compression schemes.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public enum VoiceCompression implements ComboAlias, Comparable<VoiceCompression> {

	NONE(0, "None"), ALAW(1, "G.711 a-law"), ACMALAW(2, "ACM G.711 a-law"), GSM(3, "GSM 6.10"),
	ADPCM(4, "Microsoft ADPCM"), MLAW(5, "ACM G.711 mu-law"), SPEEX(6, "Speex Narrow");
	
	private int _type;
	private String _name;
	
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
	
	public String getComboName() {
		return _name;
	}
	
	public String getComboAlias() {
		return name();
	}
}