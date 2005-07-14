package org.deltava.beans.fleet;

import java.io.*;

import org.deltava.beans.ViewEntry;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store information about Fleet Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class FleetEntry implements Serializable, Cacheable, Comparable, ViewEntry {
	
	public static final int PUBLIC = 0;
	public static final int AUTH_ONLY = 1;
	public static final int STAFF_ONLY = 2;
	
	/**
	 * Security level names.
	 */
	public static final String[] SECURITY_LEVELS = {"Public Resource", "Authorized Users", "Staff Only"};
    
    /**
     * The resource this FleetEntry points to on the filesystem.
     */
    protected File _file;
    private String _name;
    private String _description;
    
    private int _downloadCount;
    private int _majorVersion;
    private int _minorVersion;
    private int _minorSubVersion;
    private int _securityLevel;
    private long _fileSize;
    
    /**
     * Create the entry for a given filename.
     * @param fName the full file name
     * @throws NullPointerException if fName is null
     * @see FleetEntry#getFileName()
     */
    public FleetEntry(String fName) {
        super();
        _file = new File(fName.trim());
        _fileSize = (int) _file.length();
    }
    
    /**
     * Returns the name of the resource.
     * @return the name
     * @see FleetEntry#setName(String)
     */
    public String getName() {
    	return _name;
    }

    /**
     * Gets the full pathname of the resource on the local filesystem.
     * @return the full path to the resource
     * @see File#getAbsolutePath()
     */
    public String getFullName() {
        return _file.getAbsolutePath();
    }
    
    /**
     * Returns the filename of the resource, without paths. 
     * @return the filename
     * @see File#getName()
     */
    public String getFileName() {
        return _file.getName();
    }
    
    /**
     * Returns a description of this resource.
     * @return the resource's description
     * @see FleetEntry#setDescription(String)
     */
    public String getDescription() {
        return _description;
    }
    
    /**
     * Returns the number of times this resource has been downloaded.
     * @return the number of downloads
     * @see FleetEntry#setDownloadCount(int)
     */
    public int getDownloadCount() {
        return _downloadCount;
    }
    
    /**
     * Returns this resource's security level.
     * @return the security level for this entry
     * @see FleetEntry#setSecurity(int)
     * @see FleetEntry#setSecurity(String)
     */
    public int getSecurity() {
    	return _securityLevel;
    }
    
    /**
     * Returns the size of the resource.
     * @return the size of the resource in bytes, or 0 if it does not exist on the filesystem
     * @see FleetEntry#setSize(long)
     * @see File#length()
     */
    public long getSize() {
        return _fileSize;
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
     * Helper method to validate numeric parameters.
     */
    private void validateParameter(int pValue, String pName) {
        if (pValue < 0)
            throw new IllegalArgumentException(pName + " cannot be negative");
    }
    
    /**
     * Updates the number of times this resource has been downloaded.
     * @param count the download count
     * @throws IllegalArgumentException if count is negative
     */
    public void setDownloadCount(int count) {
        validateParameter(count, "Download Count");        
        _downloadCount = count;
    }
    
    /**
     * Updates the description of this resource.
     * @param desc the description
     * @throws NullPointerException if desc is null
     * @see FleetEntry#getDescription()
     */
    public void setDescription(String desc) {
        _description = desc.trim();
    }
    
    /**
     * Updates the name of this resource.
     * @param name the name
     * @throws NullPointerException if name is null
     * @see FleetEntry#getName()
     */
    public void setName(String name) {
    	_name = name.trim();
    }
    
    /**
     * Updates the size of this resource.
     * @param size the size of the file in bytes
     * @throws IllegalArgumentException if size is negative
     * @throws IllegalStateException if the file exists
     * @see FleetEntry#getSize()
     */
    public void setSize(long size) {
       if (_fileSize != 0)
          throw new IllegalStateException("File Size already set");
       
       if (size < 0)
          throw new IllegalArgumentException("Invalid File Size - " + size);
       
       _fileSize = size;
    }
    
    /**
     * Updates this resource's security level.
     * @param level the security level code
     * @throws IllegalArgumentException if level is negative or invalid
     * @see FleetEntry#setSecurity(String)
     * @see FleetEntry#getSecurity()
     */
    public void setSecurity(int level) {
    	 if ((level < 0) || (level >= SECURITY_LEVELS.length))
    	 	throw new IllegalArgumentException("Invalid Security level code - " + level);
    	 
        _securityLevel = level;
    }
    
    /**
     * Updates this resource's security level.
     * @param levelName the security level name
     * @throws IllegalArgumentException if levelName is not within SECURITY_LEVELS
     * @see FleetEntry#setSecurity(int)
     * @see FleetEntry#getSecurity()
     */
    public void setSecurity(String levelName) {
    	for (int x = 0; x < SECURITY_LEVELS.length; x++) {
    		if (SECURITY_LEVELS[x].equals(levelName)) {
    			_securityLevel = x;
    			return;
    		}
    	}
    	
    	throw new IllegalArgumentException("Invalid Security Level - " + levelName);
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
     * Returns the cache key for this object, for use in caches.
     * @return the cache key (the filename)
     */
    public Object cacheKey() {
        return getName();
    }
    
    /**
     * Compares two Fleet Library entries by comparing their names.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
    	FleetEntry e2 = (FleetEntry) o2;
    	return _name.compareTo(e2.getName());
    }
    
    /**
     * Returns the hashcode of the entry name.
     * @return the name's hashcode
     */
    public int hashCode() {
       return _name.hashCode();
    }
    
    /**
     * Returns the entry's table row CSS class name. 
     */
    public String getRowClassName() {
       return _file.exists() ? null : "warn";
    }

    /**
     * Returns a string representation of the version.
     * @return a version string
     */
    public abstract String getVersion();
}