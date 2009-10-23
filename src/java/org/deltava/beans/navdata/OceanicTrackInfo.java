// Copyright 2004, 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.Date;

/**
 * An interface to describe Oceanic track airways and NOTAMs.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public interface OceanicTrackInfo {
	
	/**
	 * An enumeration for Oceanic Track types.
	 */
	public enum Type {
		NAT, PACOT;
	}
	
	/**
	 * An enumeration for Oceanic Track direction.
	 */
	public enum Direction {
		EAST, WEST;
	}
	
    /**
     * Track type names.
     */
    public static final String[] TYPES = {"NAT", "PACOT"};

    /**
     * Returns the effective date of the Oceanic.
     * @return the route date
     */
    public Date getDate();
    
    /**
     * Returns the route type code.
     * @return the route type
     */
    public Type getType();
    
    /**
     * Returns the route type name.
     * @return the route type name
     * @see OceanicTrackInfo#getType()
     */
    public String getTypeName();
}