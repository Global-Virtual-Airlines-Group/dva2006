// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

/**
 * Valid MVS data compression schemes.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public enum VoiceCompression implements Comparable<VoiceCompression> {

	NONE(0), GZIP(1);
	
	private int _type;
	
	VoiceCompression(int type) {
		_type = type;
	}
	
	public int getType() {
		return _type;
	}
}