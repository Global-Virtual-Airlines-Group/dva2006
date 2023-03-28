// Copyright 2006, 2007, 2009, 2017, 2020, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of database image types.
 * @author Luke
 * @version 10.6
 * @since 1.0
 */

public enum ImageType implements FileType {
	CHART("charts"), GALLERY("gallery"), EXAM("exam_rsrc"), EVENT("event"), PARTNER("partner"), NEWS("news"), NOTAM("notam");
	
	private final String _urlPart;
	
	ImageType(String urlPart) {
		_urlPart = urlPart;
	}
	
	@Override
	public String getURLPart() {
		return _urlPart;
	}
}