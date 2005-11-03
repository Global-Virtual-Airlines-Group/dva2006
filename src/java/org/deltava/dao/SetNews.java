// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.News;
import org.deltava.beans.Notice;

/**
 * A Data Access Object to write System News and NOTAMs to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetNews extends DAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetNews(Connection c) {
      super(c);
   }

   /**
    * Writes a System News entry to the database. This can handle inserts and edits.
    * @param n the News entry to write
    * @throws DAOException if a JDBC error occ 
    */
   public void write(News n) throws DAOException {
      try {
         // Prepare the INSERT or UPDATE statement
         if (n.getID() == 0) {
            prepareStatement("INSERT INTO NEWS (PILOT_ID, DATE, SUBJECT, BODY) VALUES (?, ?, ?, ?)");
         } else {
            prepareStatement("UPDATE NEWS SET PILOT_ID=?, DATE=?, SUBJECT=?, BODY=? WHERE (ID=?)");
            _ps.setInt(5, n.getID());
         }
         
         // Set field values
         _ps.setInt(1, n.getAuthorID());
         _ps.setTimestamp(2, createTimestamp(n.getDate()));
         _ps.setString(3, n.getSubject());
         _ps.setString(4, n.getBody());
         
         // Write to the database
         executeUpdate(1);
         
         // Update the object if we're creating
         if (n.getID() == 0)
            n.setID(getNewID());
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Writes a Notice to Airmen (NOTAM) to the database. This can handle inserts and edits.
    * @param n the Notice entry to write
    * @throws DAOException if a JDBC error occurs
    */
   public void write(Notice n) throws DAOException {
      try {
         // prepare the INSERT or UPDATE statement
         if (n.getID() == 0) {
            prepareStatement("INSERT INTO NOTAMS (PILOT_ID, EFFDATE, SUBJECT, BODY, ACTIVE, ISHTML) "
            		+ "VALUES (?, ?, ?, ?, ?, ?)");   
         } else {
            prepareStatement("UPDATE NOTAMS SET PILOT_ID=?, EFFDATE=?, SUBJECT=?, BODY=?, ACTIVE=?, "
            		+ "ISHTML=? WHERE (ID=?)");
            _ps.setInt(7, n.getID());
         }
         
         // Set field values
         _ps.setInt(1, n.getAuthorID());
         _ps.setTimestamp(2, createTimestamp(n.getDate()));
         _ps.setString(3, n.getSubject());
         _ps.setString(4, n.getBody());
         _ps.setBoolean(5, n.getActive());
         _ps.setBoolean(6, n.getIsHTML());
         
         // Write to the database
         executeUpdate(1);
         
         // Update the object if we're creating
         if (n.getID() == 0)
            n.setID(getNewID());
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Deletes a News Entry/NOTAM from the database.
    * @param id the Database ID of the NOTAM or News Entry
    * @param isNOTAM TRUE if deleting a NOTAM, otherwise FALSE
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(int id, boolean isNOTAM) throws DAOException {
      
      // Build SQL statement
      StringBuffer sqlBuf = new StringBuffer("DELETE FROM ");
      sqlBuf.append(isNOTAM ? "NOTAMS" : "NEWS");
      sqlBuf.append(" WHERE (ID=?)");
      
      try {
         prepareStatement(sqlBuf.toString());
         _ps.setInt(1, id);
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}