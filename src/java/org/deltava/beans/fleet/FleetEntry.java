// Copyright 2005, 2009, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.io.File;
import java.time.Instant;

/**
 * A bean to store information about Fleet Library entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public abstract class FleetEntry extends LibraryEntry {
	
	private Instant _lastModified;
    private int _majorVersion;
    private int _minorVersion;
    private int _minorSubVersion;
    
    /**
     * Create the entry for a given File.
     * @param f the File
     * @see LibraryEntry#getFileName()
     */
    public FleetEntry(File f) {
        super(f);
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
     * Updates the date this entry was last modified.
     * @return the last modification date/time
     * @see FleetEntry#setLastModified(Instant)
     */
    public Instant getLastModified() {
    	return _lastModified;
    }
    
    /**
     * Updates the version number of this resource.
     * @param major the major version number
     * @param minor the minor version number
     * @param subMinor the sub-minor version number
     * @see FleetEntry#getMajorVersion()
     * @see FleetEntry#getMinorVersion()
     * @see FleetEntry#getSubVersion()
     */
    public void setVersion(int major, int minor, int subMinor) {
        _majorVersion = Math.max(1, major);
        _minorVersion = Math.max(0, minor);
        _minorSubVersion = Math.max(0, subMinor);
    }
    
    /**
     * Updates the date this entry was last modified.
     * @param dt the last modified date/time
     * @see FleetEntry#getLastModified()
     */
    public void setLastModified(Instant dt) {
    	_lastModified = dt;
    }
    
    /**
     * Returns a string representation of the version.
     * @return a version string
     */
    public abstract String getVersion();
}