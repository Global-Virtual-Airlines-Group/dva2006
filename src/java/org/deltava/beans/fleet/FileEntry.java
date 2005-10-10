// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.fleet;

/**
 * A bean to store information about File Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FileEntry extends LibraryEntry {

   /**
    * Creates a new File Library bean.
    * @param fName the file name
    */
   public FileEntry(String fName) {
      super(fName);
   }
}