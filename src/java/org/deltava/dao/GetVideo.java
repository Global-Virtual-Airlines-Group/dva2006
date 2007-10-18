// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.io.InputStream;

/**
 * A Data Access Object to allow downloads of Flight Video data. This is a special Data Access Object in that it does
 * not download all binary data into an in-memory array and then close the resultset;  this one keeps the result set
 * open until explicitly closed, allowing streaming of data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetVideo extends DAO {
   
   private ResultSet _rs;
   
   /**
    * Initializes the Data Access object.
    * @param c the JDBC connection to use
    */
   public GetVideo(Connection c) {
      super(c);
   }
   
   /**
    * Returns Check Ride video data as an input stream. The result set remains open until the {@link GetVideo#close}
    * method is called.
    * @param id the Check Ride database ID
    * @return an input stream to the video data
    * @throws DAOException if a JDBC error occurs
    */
   public InputStream getVideoStream(int id) throws DAOException {
      try {
         prepareStatement("SELECT DATA FROM exams.VIDEOS WHERE (ID=?)");
         _ps.setInt(1, id);
         
         // Execute the query and return an input stream to the binary data
         _rs = _ps.executeQuery();
         return _rs.next() ? _rs.getBinaryStream(1) : null;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Close the result set.
    */
   public void close() {
      try {
         _rs.close();
         _ps.close();
      } catch (Exception e) { } 
   }
}