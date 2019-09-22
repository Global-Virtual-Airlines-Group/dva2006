// Copyright 2006, 2007, 2008, 2010, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An enumeration of aircraft fuel tank types.
 * @author Luke
 * @version 8.7
 * @since 8.7
 */

public enum TankType {
	PRIMARY, SECONDARY, OTHER;
	
	public String getDescription() {
		return toString();
	}
	
	@Override
	public String toString() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
}