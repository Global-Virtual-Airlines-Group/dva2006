// Copyright 2005, 2014, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.AddressValidation;

/**
 * A Data Access Object to write e-mail address validation data to the database.
 * @author Luke
 * @version 9.0
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
	   try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO EMAIL_VALIDATION (ID, EMAIL, HASH, VALID) VALUES (?, ?, ?, ?)")) {
         ps.setInt(1, addr.getID());
         ps.setString(2, addr.getAddress());
         ps.setString(3, addr.getHash());
         ps.setBoolean(4, addr.getIsValid());
         executeUpdate(ps, 1);
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
	   try (PreparedStatement ps = prepare("DELETE FROM EMAIL_VALIDATION WHERE (ID=?)")) {
         ps.setInt(1, id);
         executeUpdate(ps, 0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}