// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.Collection;

import org.deltava.beans.Person;

/**
 * A Data Access Object interface to check for uniqueness.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface PersonUniquenessDAO {

   /**
    * Checks if a Person exists within a Particular database.
    * @param usr the Person to check for
    * @param dbName the database name
    * @return a Collection of Database IDs as Integers 
    */
   public Collection checkUnique(Person usr, String dbName) throws DAOException;
   
   /**
    * Performs a soundex search on a Person's full name to detect possible matches. The soundex
    * implementation is dependent on the capabilities of the underlying database engine, and is not
    * guaranteed to be consistent (or even supported) across different database servers.
    * @param usr the Person to check for
    * @param dbName the database name
    * @return a Collection of Database IDs as Integers
    */
   public Collection checkSoundex(Person usr, String dbName) throws DAOException;
}