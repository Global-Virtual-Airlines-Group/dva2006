// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.io.File;

import org.deltava.beans.ViewEntry;
import org.deltava.util.cache.Cacheable;

/**
 * An abstract bean to store information about Library entries.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public abstract class LibraryEntry implements java.io.Serializable, Comparable, Cacheable, ViewEntry {

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
   private File _file;
   private long _fileSize;

   private String _name;
   private String _description;
   private int _downloadCount;
   private int _securityLevel;
   
   /**
    * Creates a new Library entry.
    * @param fName the file name 
    */
   protected LibraryEntry(String fName) {
      super();
      _file = new File(fName.trim());
      _fileSize = (int) _file.length();
   }
   
   /**
    * Returns the name of the resource.
    * @return the name
    * @see LibraryEntry#setName(String)
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
    * @see LibraryEntry#setDescription(String)
    */
   public String getDescription() {
       return _description;
   }
   
   /**
    * Returns the number of times this resource has been downloaded.
    * @return the number of downloads
    * @see LibraryEntry#setDownloadCount(int)
    */
   public int getDownloadCount() {
       return _downloadCount;
   }

   /**
    * Returns this resource's security level.
    * @return the security level for this entry
    * @see LibraryEntry#setSecurity(int)
    */
   public int getSecurity() {
   	return _securityLevel;
   }
   
   /**
    * Returns the size of the resource.
    * @return the size of the resource in bytes, or 0 if it does not exist on the filesystem
    * @see LibraryEntry#setSize(long)
    * @see File#length()
    */
   public long getSize() {
       return _fileSize;
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
    * @see LibraryEntry#getDescription()
    */
   public void setDescription(String desc) {
       _description = desc.trim();
   }
   
   /**
    * Updates the name of this resource.
    * @param name the name
    * @throws NullPointerException if name is null
    * @see LibraryEntry#getName()
    */
   public void setName(String name) {
   	_name = name.trim();
   }
   
   /**
    * Updates the size of this resource.
    * @param size the size of the file in bytes
    * @see LibraryEntry#getSize()
    */
   public void setSize(long size) {
      _fileSize = Math.max(0, size);
   }

   /**
    * Updates this resource's security level.
    * @param level the security level code
    * @throws IllegalArgumentException if level is negative or invalid
    * @see LibraryEntry#getSecurity()
    */
   public void setSecurity(int level) {
   	 if ((level < 0) || (level >= SECURITY_LEVELS.length))
   	 	throw new IllegalArgumentException("Invalid Security level code - " + level);
   	 
       _securityLevel = level;
   }

   /**
    * Helper method to validate numeric parameters.
    * @param pValue the parameter value
    * @param pName the parameter name
    * @throws IllegalArgumentException if pValue is negative
    */
   protected void validateParameter(int pValue, String pName) {
       if (pValue < 0)
           throw new IllegalArgumentException(pName + " cannot be negative");
   }
   
   /**
    * Returns the underlying filesystem entry.
    * @return the filesystem entry
    */
   public File file() {
      return _file;
   }
   
   /**
    * Compares two Library entries by comparing their names.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
    	LibraryEntry e2 = (LibraryEntry) o2;
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
    * Returns the object's cache key.
    * @return the entry name
    */
   public final Object cacheKey() {
      return getName();
   }
}