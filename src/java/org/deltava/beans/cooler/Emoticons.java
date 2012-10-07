// Copyright 2005, 2006, 2007, 2009, 2011, 2012 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.cooler;

/**
 * An interface to store Water Cooler emoticon names.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public enum Emoticons {
	SMILE(":)"), WINK(";)"), COOL, FROWN(":("), EEK(":O"), MAD, REDFACE, CONFUSED, ROLLEYES,
	BIGGRIN(":D"), RAZZ(":p"), PLOTTING, JUDGE, SLITWRIST, SCARED, EVILGRIN(">)"), ROFL, NUTS,
	BLAHBLAH, RAWK, RIMSHOT, KISS(":*"), BANGHEAD, FACEPALM, DROOL;
	
	private final String _code;
	
	Emoticons() {
		this(null);
	}
	
	Emoticons(String code) {
		_code = code;
	}

	/**
	 * Returns the emoticon shortcut code.
	 * @return the code
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * Returns the emoticon name.
	 * @return the lowercase name
	 */
	public String getName() {
		return name().toLowerCase();
	}

	/**
	 * Find an emoticon based on name or code.
	 * @param name the emoticon
	 * @return an Emoticons entry, or null if not found
	 */
	public static Emoticons find(String name) {
		if (name == null) return null;
		String n = name.toUpperCase();
		for (Emoticons ei : values()) {
			if (ei.name().equals(n))
				return ei;
			if ((ei.getCode() != null) && n.equals(ei.getCode()))
				return ei;
		}
		
		return null;
	}
}