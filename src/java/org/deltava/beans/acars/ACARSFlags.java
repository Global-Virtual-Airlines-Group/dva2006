// Copyright 2004, 2005, 2006, 2008, 2013, 2015, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An interface to store ACARS Flags constants.
 * @author Luke
 * @author Rahul
 * @version 8.2
 * @since 1.0
 */

public enum ACARSFlags {
	PAUSED(0), TOUCHDOWN(1), PARKED(2), ONGROUND(3), SP_ARMED(4), GEARDOWN(5), AFTERBURNER(6),
	AP_GPS(8), AP_NAV(9), AP_HDG(10), AP_APR(11), AP_ALT(12), AT_IAS(13), AT_MACH(14),
	PUSHBACK(15), STALL(16), OVERSPEED(17), CRASH(18), AT_VNAV(19), AP_LNAV(20),
	REVERSE(21),
	AT_FLCH(22);
	
	private static final int AP_ANY = AP_GPS.getMask() | AP_NAV.getMask() | AP_HDG.getMask() | AP_APR.getMask() | AP_ALT.getMask() | AP_LNAV.getMask();
	private static final int AT_ANY = AT_FLCH.getMask() | AT_VNAV.getMask() | AT_IAS.getMask() | AT_MACH.getMask();
	
	private final int _mask;
	
	ACARSFlags(int bit) {
		_mask = 1 << bit;
	}
	
	/**
	 * Returns the bitmask for this flag.
	 * @return the bitmask
	 */
	public int getMask() {
		return _mask;
	}
	
	/**
	 * Adds a flag to a compound bitmap.
	 * @param flags the bitmap
	 * @return the bitmap with the flag
	 */
	public int add(int flags) {
		return (flags | _mask);
	}
	
	/**
	 * Returns whether a compound bitmap has this flag.
	 * @param flags the bitmap
	 * @return TRUE whether the bitmap has the flag, otherwise FALSE
	 */
	public boolean has(int flags) {
		return ((flags & _mask) != 0);
	}
	
	/**
	 * Returns wehther a combound bitmap has any autopilot flags. 
	 * @param flags the bitmap
	 * @return TRUE whether any autopilot flag is set, otherwise FALSE
	 */
	public static boolean hasAP(int flags) {
		return ((flags & AP_ANY) != 0);
	}

	/**
	 * Returns wehther a combound bitmap has any autothrottle flags. 
	 * @param flags the bitmap
	 * @return TRUE whether any autothrottle flag is set, otherwise FALSE
	 */
	public static boolean hasAT(int flags) {
		return ((flags & AT_ANY) != 0);
	}
}