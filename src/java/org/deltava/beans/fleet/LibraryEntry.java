// Copyright 2005, 2009, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.io.*;

import org.deltava.beans.ViewEntry;
import org.deltava.util.cache.Cacheable;

/**
 * An abstract bean to store information about Library entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public abstract class LibraryEntry implements Comparable<LibraryEntry>, Cacheable, ViewEntry {

   private final File _file;
   private long _fileSize;
   private String _name;
   private String _description;
   private int _downloadCount;
   private Security _securityLevel = Security.PUBLIC;
   
   /**
    * Creates a new Library entry.
    * @param f the File
    */
   protected LibraryEntry(File f) {
      super();
      _file = f;
      _fileSize = (int) _file.length();
   }
   
   /**
    * Returns the name of the resource.
    * @return the name
    * @see LibraryEntry#setName(String)
    */
   public final String getName() {
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
    * @see LibraryEntry#setSecurity(Security)
    */
   public Security getSecurity() {
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
    */
   public void setDownloadCount(int count) {
       _downloadCount = Math.max(0, count);
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
    * @see LibraryEntry#getSecurity()
    */
   public void setSecurity(Security level) {
       _securityLevel = level;
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
    */
   @Override
   public int compareTo(LibraryEntry e2) {
    	return _name.compareTo(e2._name);
   }
   
   /**
    * Returns the hashcode of the entry name.
    * @return the name's hashcode
    */
   @Override
   public int hashCode() {
      return _name.hashCode();
   }
   
   /**
    * Returns the entry's table row CSS class name. 
    */
   @Override
   public String getRowClassName() {
      return _file.exists() ? null : "warn";
   }

   /**
    * Returns the object's cache key.
    * @return the entry name
    */
   @Override
   public final Object cacheKey() {
      return getName();
   }
  
   @Override
   public String toString() {
	   return _name;
   }
}