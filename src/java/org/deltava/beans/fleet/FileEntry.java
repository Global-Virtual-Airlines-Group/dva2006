// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import org.deltava.beans.*;

/**
 * A bean to store information about File Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FileEntry extends LibraryEntry implements AuthoredBean {
   
   private int _authorID;
   private String _category;

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
	 * Returns the file category.
	 * @return the category
	 * @see FileEntry#setCategory(String)
	 */
	public String getCategory() {
		return _category;
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
   
	/**
	 * Updates the file category.
	 * @param ct the category
	 * @see FileEntry#getCategory()
	 */
	public void setCategory(String ct) {
		_category = ct;
	}
}