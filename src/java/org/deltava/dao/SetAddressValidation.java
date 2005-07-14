// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.AddressValidation;

/**
 * A Data Access Object to write e-mail address validation data to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetAddressValidation extends DAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetAddressValidation(Connection c) {
      super(c);
   }

   /**
    * Writes an address validation entry to the database.
    * @param addr the AddressValidation bean
    * @throws DAOException if a JDBC error occurs
    */
   public void write(AddressValidation addr) throws DAOException {
      try {
         prepareStatement("INSERT INTO EMAIL_VALIDATION (ID, EMAIL, HASH) VALUES (?, ?, ?)");
         _ps.setInt(1, addr.getID());
         _ps.setString(2, addr.getAddress());
         _ps.setString(3, addr.getHash());
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
  
   /**
    * Deletes an address validation entry from the database.
    * @param id the database ID of the Pilot/Applicant
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(int id) throws DAOException {
      try {
         prepareStatement("DELETE FROM EMAIL_VALIDATION WHERE (ID=?)");
         _ps.setInt(1, id);
         executeUpdate(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}