// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.TransferRequest;

/**
 * A Data Access Object to write equipment program Transfer Requests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetTransferRequest extends DAO {

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetTransferRequest(Connection c) {
      super(c);
   }

   /**
    * Writes a Transfer Request to the database. This can handle INSERTs and UPDATEs.
    * @param txreq the TransferRequest bean
    * @throws DAOException if a JDBC error occurs
    */
   public void write(TransferRequest txreq) throws DAOException {
      try {
         if (txreq.getDate() == null) {
            prepareStatement("INSERT INTO TXREQUESTS (STATUS, CHECKRIDE_ID, EQTYPE, CREATED, RATING_ONLY, ID) "
            		+ "VALUES (?, ?, ?, ?, ?, ?)");
            txreq.setDate(new java.util.Date());
         } else {
            prepareStatement("UPDATE TXREQUESTS SET STATUS=?, CHECKRIDE_ID=?, EQTYPE=?, CREATED=?, " +
            		"RATING_ONLY=? WHERE (ID=?)");
         }
         
         // Update the prepared statement
         _ps.setInt(1, txreq.getStatus());
         _ps.setInt(2, txreq.getCheckRideID());
         _ps.setString(3, txreq.getEquipmentType());
         _ps.setTimestamp(4, createTimestamp(txreq.getDate()));
         _ps.setBoolean(5, txreq.getRatingOnly());
         _ps.setInt(6, txreq.getID());
         
         // Execute the update
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Deletes a Transfer Request from the database.
    * @param pilotID the Pilot's database ID
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(int pilotID) throws DAOException {
      try {
         prepareStatement("DELETE FROM TXREQUESTS WHERE (ID=?)");
         _ps.setInt(1, pilotID);
         executeUpdate(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}