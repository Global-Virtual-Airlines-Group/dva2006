// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.fleet;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store information about File Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FileEntry extends LibraryEntry {
   
   private int _authorID;

   /**
    * Creates a new File Library bean.
    * @param fName the file name
    */
   public FileEntry(String fName) {
      super(fName);
   }
   
   /**
    * Returns the ID of the contributor.
    * @return the contributor's database ID
    * @see FileEntry#setAuthorID(int)
    */
   public int getAuthorID() {
      return _authorID;
   }
   
   /**
    * Updates the ID of the contributor.
    * @param id the contributor's database ID
    * @throws IllegalArgumentException if id is zero or negative
    */
   public void setAuthorID(int id) {
      DatabaseBean.validateID(_authorID, id);
      _authorID = id;
   }
}