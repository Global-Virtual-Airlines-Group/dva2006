// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

/**
 * A bean to store information about Manuals.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Manual extends FleetEntry {
   
    /**
     * Creates a new Manual bean.
     * @param fName the file name of the manual
     */
    public Manual(String fName) {
        super(fName);
    }

    /**
     * Returns this manual's version number. Manuals only have a major version number.
     * @see FleetEntry#getVersion()
     */
    public String getVersion() {
        return String.valueOf(getMajorVersion());
    }
    
    public final void setVersion(int major) {
        super.setVersion(major, 0, 0);
    }

    public final void setVersion(int major, int minor, int subVersion) {
        super.setVersion(major, 0, 0);
    }
}