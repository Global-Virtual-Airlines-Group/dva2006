// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration of Flight attributes. These are stored internally and in the database as a bitmap. 
 * @author Luke
 * @version 12.2
 * @since 12.2
 */

public enum Attribute {
	NOTRATED(0x1, true), VATSIM(0x2), IVAO(0x4), FPI(0x8), ACARS(0x10), ROUTEWARN(0x20, true), TIMEWARN(0x40, true), CHECKRIDE(0x80), CHARTER(0x100), HISTORIC(0x200), ACADEMY(0x400), 
	RANGEWARN(0x800, true), REFUELWARN(0x1000, true), ETOPSWARN(0x2000, true), DISPATCH(0x4000), WEIGHTWARN(0x8000, true), XACARS(0x10000), RWYWARN(0x20000, true), 
	RWYSFCWARN(0x40000, true), SIMFDR(0x80000), PEDGE(0x100000), AIRSPACEWARN(0x200000, true), DIVERT(0x400000, true), POSCON(0x800000), SIMBRIEF(0x1000000);
	
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
	
	Attribute(int mask) {
		this(mask, false);
	}
	
	Attribute(int mask, boolean isWarning) {
		_mask = mask;
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