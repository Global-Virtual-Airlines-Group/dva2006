// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An interface to store ACARS Flags constants.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ACARSFlags {
   
	// Database Flags
	public static final int FLAG_PAUSED = 0x0001;
	public static final int FLAG_SLEW = 0x0002;
	public static final int FLAG_AFTERBURNER = 0x0004;
	public static final int FLAG_ONGROUND = 0x0008;
	
	public static final int FLAG_AUTOPILOT = 0x0100;
	public static final int FLAG_AP_NAV = 0x0200;
	public static final int FLAG_AP_HDG = 0x0400;
	public static final int FLAG_AP_APR = 0x0800;
	
	public static final int FLAG_AUTOTHROTTLE = 0x1000;
	public static final int FLAG_AT_IAS = 0x2000;
	public static final int FLAG_AT_MACH = 0x4000;
}