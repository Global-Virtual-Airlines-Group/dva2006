// Copyright 2004, 2005, 2009, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.time.Instant;

/**
 * An interface to describe Oceanic track airways and NOTAMs.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public interface OceanicTrackInfo {
	
	/**
	 * An enumeration for Oceanic Track types.
	 */
	public enum Type {
		NAT, PACOT, AUSOT;
	}
	
	/**
	 * An enumeration for Oceanic Track direction.
	 */
	public enum Direction {
		EAST, WEST, ALL;
	}
	
    /**
     * Returns the effective date of the Oceanic.
     * @return the route date
     */
    public Instant getDate();
    
    /**
     * Returns the route type code.
     * @return the route type
     */
    public Type getType();
}