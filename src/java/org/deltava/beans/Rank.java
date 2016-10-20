// Copyright 2004, 2005, 2007, 2009, 2010, 2016 Global Virtual Airlines Group. All Righs Reserved.
package org.deltava.beans;

/**
 * An enumeration for rank names.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public enum Rank {
	FO("First Officer"), C("Captain"), SC("Senior Captain"), ACP("Assistant Chief Pilot"), CP("Chief Pilot");
	
	private final String _name;
	
	Rank(String name) {
		_name = name;
	}
	
	/**
	 * Returns the rank name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
	/**
	 * Compares this rank to a text name.
	 * @param s2 the text name 
	 * @return TRUE if the code or name equals the supplied name, otherwise FALSE
	 */
	public boolean equals(String s2) {
		return (s2 != null) && name().equalsIgnoreCase(s2) || _name.equalsIgnoreCase(s2);
	}
	
	/**
	 * Returns if this is a Chief Pilot or Assistant Chief Pilot rank.
	 * @return TRUE if equals ACP or CP, otherwise FALSE
	 */
	public boolean isCP() {
		return (this == ACP) || (this == CP);
	}
	
	/**
	 * Gets a rank from a name.
	 * @param s the name
	 * @return a Rank, or null if unknown 
	 */
	public static Rank fromName(String s) {
		Rank[] all = Rank.values();
		for (int x = 0; x < all.length; x++) {
			Rank r = all[x];
			if (r.equals(s))
				return r;
		}
		
		return null;
	}
}