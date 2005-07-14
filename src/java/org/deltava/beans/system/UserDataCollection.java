// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

/**
 * A class to support a collection of {@link UserData} beans.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserDataCollection implements java.io.Serializable {

   private Map _entries;
   
   /**
    * Creates a new, empty UserDataCollection.
    */
   public UserDataCollection() {
      super();
      _entries = new HashMap();
   }
   
   /**
    * Creates a new UserDataCollection from a set of Integers containing database IDs.
    * @param data a Collection of UserData objects
    * @see UserDataCollection#addAll(Collection)
    */
   public UserDataCollection(Collection data) {
      this();
      addAll(data);
   }

   /**
    * Adds an entry to the collection.
    * @param usr the UserData object
    */
   public void add(UserData usr) {
      _entries.put(new Integer(usr.getID()), usr);
   }
   
   /**
    * Adds a collection of UserData objects to the container.
    * @param data a Collection of UserData objects
    */
   public void addAll(Collection data) {
      for (Iterator i = data.iterator(); i.hasNext(); )
         add((UserData) i.next()); 
   }
   
   /**
    * Returns if the collection contains an entry for a particular user.
    * @param id the User's database ID
    * @return TRUE if data for the User is present, otherwise FALSE
    */
   public boolean contains(int id) {
      return _entries.containsKey(new Integer(id));
   }
   
   /**
    * Returns an entry for a particular user.
    * @param id the User's database ID
    * @return the UserData bean, or null if not found
    */
   public UserData get(int id) {
      return (UserData) _entries.get(new Integer(id));
   }
   
   /**
    * Returns all entries present within a particular table.
    * @param tableName the database table name
    * @return a Collection of UserData objects
    * @throws NullPointerException if tableName is null
    */
   public Collection getByTable(String tableName) {
      
      Set results = new HashSet();
      for (Iterator i = _entries.values().iterator(); i.hasNext(); ) {
         UserData usr = (UserData) i.next();
         String usrTable = usr.getDB() + "." + usr.getTable();
         if (tableName.equals(usrTable))
            results.add(usr);
      }
      
      return results;
   }
   
   /**
    * Returns all tables containing Users within this container.
    * @return a Collection of table names in DB.TABLE format
    */
   public Collection getTableNames() {

      Set results = new HashSet();
      for (Iterator i = _entries.values().iterator(); i.hasNext(); ) {
         UserData usr = (UserData) i.next();
         results.add(usr.getDB() + "." + usr.getTable());
      }
      
      return results;
   }
   
   /**
    * Returns the size of the collection.
    * @return the number of entries
    */
   public int size() {
      return _entries.size();
   }
}