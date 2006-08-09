// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import org.deltava.util.StringUtils;

/**
 * A bean to store video data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Video extends FileEntry {
	
	public static final int WMV = 0;
	public static final int AVI = 1;
	public static final int DIVX = 2;
	
	public static final String[] TYPES = {"WMV", "AVI", "DIVX" };
	
	private int _type;

	/**
	 * Creates a new video bean. 
	 * @param fName the file name
	 * @throws NullPointerException if fName is null
	 * @throws IllegalArgumentException if fName has an unknown extension
	 */
	public Video(String fName) {
		super(fName);
		setType(fName);
	}

	/**
	 * Returns the video type.
	 * @return the type code
	 * @see Video#getTypeName()
	 */
	public int getType() {
		return _type;
	}
	
	/**
	 * Returns the icon name to display.
	 * @return the icon name, minus extension
	 */
	public String getIconName() {
		return (_type == DIVX) ? "divx" : "wmp";
	}
	
	/**
	 * Returns the video type name.
	 * @return the type name
	 * @see Video#getType()
	 */
	public String getTypeName() {
		return TYPES[_type];
	}
	
	/**
	 * Updates the video type.
	 * @param fName the file name
	 * @throws IllegalArgumentException if fName has an unknown extension
	 */
	public void setType(String fName) {
		if (fName.indexOf('.') == -1)
			throw new IllegalArgumentException("Invalid file name - " + fName);
		
		String ext = fName.substring(fName.lastIndexOf('.') + 1).toUpperCase();
		int videoType = StringUtils.arrayIndexOf(TYPES, ext);
		if (videoType == -1)
			throw new IllegalArgumentException("Invalid extension - " + fName);
		
		_type = videoType;
	}
}