// Copyright 2006, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

/**
 * A bean to store video data.
 * @author Luke
 * @version 5.2
 * @since 1.0
 */

public class Video extends FileEntry {
	
	public enum Type {
		WMV, AVI, DIVX, MP3, MP4
	}
	
	private Type _type;

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
	 */
	public Type getType() {
		return _type;
	}
	
	/**
	 * Returns the icon name to display.
	 * @return the icon name, minus extension
	 */
	public String getIconName() {
		return (_type == Type.DIVX) ? "divx" : "wmp";
	}
	
	/**
	 * Updates the video type.
	 * @param fName the file name
	 * @throws IllegalArgumentException if fName has an unknown extension
	 */
	public void setType(String fName) {
		if (fName.indexOf('.') == -1)
			throw new IllegalArgumentException("Invalid file name - " + fName);
		
		String ext = fName.substring(fName.lastIndexOf('.') + 1);
		try {
			_type = Type.valueOf(ext.toUpperCase());
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("Invalid extension - " + ext);
		}
	}
}