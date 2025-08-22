// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration of Flight attributes. These are stored internally and in the database as a bitmap. 
 * @author Luke
 * @version 12.2
 * @since 12.2
 */

public enum Attribute {
	NOTRATED(true), VATSIM, IVAO, FPI, ACARS, ROUTEWARN(true), TIMEWARN(true), CHECKRIDE, CHARTER, HISTORIC, ACADEMY, RANGEWARN(true), REFUELWARN(true), ETOPSWARN(true), 
	DISPATCH, WEIGHTWARN(true), XACARS, RWYWARN(true),	RWYSFCWARN(true), SIMFDR, PEDGE, AIRSPACEWARN(true), DIVERT(true), POSCON, SIMBRIEF, AUTOAPPROVE;
	
	/**
	 * Bitmap used to search for any online flight in the database.
	 */
	public static final int ONLINE_MASK = VATSIM._mask | IVAO._mask | FPI._mask | PEDGE._mask | POSCON._mask;
	
	/**
	 * Bitmap used to search for any FDR-recorded flight in the database.
	 */
	public static final int FDR_MASK = ACARS._mask | XACARS._mask | SIMFDR._mask;
	
	private final int _mask;
	private final boolean _isWarning;
	
	Attribute() {
		this(false);
	}
	
	Attribute(boolean isWarning) {
		_mask = 1 << ordinal();
		_isWarning = isWarning;
	}
	
	/**
	 * Returns if this attribute is a flight warning.
	 * @return TRUE if a warning, otherwise FALSE
	 */
	public boolean isWarning() {
		return _isWarning;
	}
	
	/**
	 * Returns the bitmap mask.
	 * @return the mask
	 */
	public int getValue() {
		return _mask;
	}
	
	/**
	 * Returns whether this Attribute is included within a particular bitmap.
	 * @param attrs the bitmap
	 * @return TRUE if the Attribute is included, otherwisee FALSE
	 */
	public boolean in(int attrs) {
		return ((attrs & _mask) != 0);
	}
	
	/**
	 * Determines if an Online Netowrk Attribute is included within a particular bitmap. 
	 * @param attrs the bitmap
	 * @return TRUE if an Online Network Attribute is included, otherwise FALSE
	 */
	public static boolean isOnline(int attrs) {
		return ((attrs & ONLINE_MASK) != 0);
	}

	/**
	 * Determines if a Flight Data Recorder Attribute is included within a particular bitmap. 
	 * @param attrs the bitmap
	 * @return TRUE if a Flight Data Recorder Attribute is included, otherwise FALSE
	 */
	public static boolean isFDR(int attrs) {
		return ((attrs & FDR_MASK) != 0);
	}
	
	/**
	 * Returns if there are any warning Attributes included within a particular bitmap
	 * @param attrs the bitmap
	 * @return TRUE if any warning Attributes are present, otherwise FALSE
	 */
	public static boolean hasWarning(int attrs) {
		for (Attribute a : values()) {
			if (a.isWarning() && a.in(attrs))
				return true;
		}
		
		return false;
	}
}