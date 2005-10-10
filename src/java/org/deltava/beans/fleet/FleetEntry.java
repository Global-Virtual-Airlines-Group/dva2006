// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.fleet;

/**
 * A bean to store information about Fleet Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class FleetEntry extends LibraryEntry {
	
    private int _majorVersion;
    private int _minorVersion;
    private int _minorSubVersion;
    
    /**
     * Create the entry for a given filename.
     * @param fName the full file name
     * @throws NullPointerException if fName is null
     * @see LibraryEntry#getFileName()
     */
    public FleetEntry(String fName) {
        super(fName);
    }
    
    /**
     * Returns the major version of the resource. (eg. <b>3</b>.1.2)
     * @return the major version number
     * @see FleetEntry#setVersion(int, int, int)
     */
    public int getMajorVersion() {
        return _majorVersion;
    }

    /**
     * Returns the minor version of the resource. (eg. 3.<b>1</b>.2)
     * @return the minor version number
     * @see FleetEntry#setVersion(int, int, int)
     */
    public int getMinorVersion() {
        return _minorVersion;
    }
    
    /**
     * Returns the sub-minor version of the resource. (eg. 3.1.<b>2</b>)
     * @return the sub-minor version number
     * @see FleetEntry#setVersion(int, int, int)
     */
    public int getSubVersion() {
        return _minorSubVersion;
    }
    
    /**
     * Updates the version number of this resource.
     * @param major the major version number
     * @param minor the minor version number
     * @param subMinor the sub-minor version number
     * @throws IllegalArgumentException if major, minor or subMinor are negative
     * @see FleetEntry#getMajorVersion()
     * @see FleetEntry#getMinorVersion()
     * @see FleetEntry#getSubVersion()
     */
    public void setVersion(int major, int minor, int subMinor) {
        validateParameter(major, "Major Version");
        validateParameter(minor, "Minor Version");
        validateParameter(subMinor, "Minor Sub-Version");
        _majorVersion = major;
        _minorVersion = minor;
        _minorSubVersion = subMinor;
    }
    
    /**
     * Returns a string representation of the version.
     * @return a version string
     */
    public abstract String getVersion();
}