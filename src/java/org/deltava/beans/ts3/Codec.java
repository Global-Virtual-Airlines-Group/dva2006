// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts3;

/**
 * An enumeration to store TeamSpeak 3 voice codecs. 
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public enum Codec {
	SPEEX_NARROW("Speex Narrowband"), SPEEX_WIDE("Speex wideband"), SPEEX_UWIDE("Speex UltraWide"), CELT("Celt Mono");
	
	private final String _desc;
	
	Codec(String desc) {
		_desc = desc; 
	}
	
	public String getDescription() {
		return _desc;
	}
}