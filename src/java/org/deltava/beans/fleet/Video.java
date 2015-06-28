// Copyright 2006, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.io.File;

/**
 * A bean to store video data.
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public class Video extends FileEntry {
	
	public enum Type {
		WMV, MP4, OGV
	}
	
	private Type _type;

	/**
	 * Creates a new video bean. 
	 * @param f the File
	 * @throws NullPointerException if fName is null
	 * @throws IllegalArgumentException if fName has an unknown extension
	 */
	public Video(File f) {
		super(f);
		setType(f.getName());
	}

	/**
	 * Returns the video type.
	 * @return the type code
	 */
	public Type getType() {
		return _type;
	}
	
	private static Type getType(String fName) {
		try {
			String ext = fName.substring(fName.lastIndexOf('.') + 1);
			return Type.valueOf(ext.toUpperCase());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
	
	/**
	 * Updates the video type.
	 * @param fName the file name
	 * @throws IllegalArgumentException if fName has an unknown extension
	 */
	public void setType(String fName) {
		Type t = getType(fName);
		if (t == null)
			throw new IllegalArgumentException("Invalid file type - " + fName);
		
		_type = t;
	}

	/**
	 * Returns whether this video is in a supported format.
	 * @param fName the video file name
	 * @return TRUE if supported, otherwise FALSE
	 */
	public static boolean isValidFormat(String fName) {
		return (getType(fName) != null);
	}
}