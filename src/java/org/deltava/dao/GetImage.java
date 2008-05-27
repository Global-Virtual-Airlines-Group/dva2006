// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

/**
 * A Data Access Object to retrieve image data from the database.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class GetImage extends DAO {

    /**
     * Initializes the DAO from a JDBC connection.
     * @param c the JDBC connection
     */
    public GetImage(Connection c) {
        super(c);
        setQueryMax(1);
    }
    
    /**
     * Executes an arbitrary SQL statement, after preparing it with the image ID
     * @param id the image ID
     * @param sql the SQL prepared statement text
     * @return the image data
     * @throws DAOException if a JDBC error occurs
     */
    private byte[] execute(int id, String sql) throws DAOException {
        try {
            // Prepare the statement
            prepareStatement(sql);
            _ps.setInt(1, id);

            // Execute the query, if not found return null
            ResultSet rs = _ps.executeQuery();
            if (!rs.next())
                return null;
            
            // Get the image data 
            byte[] imgBuffer = rs.getBytes(1);

            // Close the result set
            rs.close();
            _ps.close();
            
            // Return the image data
            return imgBuffer;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }

    /**
     * Returns a signature image for a Pilot.
     * @param id the pilot ID
     * @param dbName the database containing the signature
     * @return the signature image data
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getSignatureImage(int id, String dbName) throws DAOException {
    	StringBuilder sqlBuf = new StringBuilder("SELECT WC_SIG FROM ");
    	sqlBuf.append(formatDBName(dbName));
    	sqlBuf.append(".SIGNATURES WHERE (ID=?)");
        return execute(id, sqlBuf.toString());
    }
    
    /**
     * Returns an Online Event banner image.
     * @param id the Event database ID
     * @return the banner image data
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getEventBanner(int id) throws DAOException {
    	return execute(id, "SELECT IMG FROM events.BANNERS WHERE (ID=?)");
    }
    
    /**
     * Returns a Chart.
     * @param id the chart ID
     * @return the chart image data
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getChart(int id) throws DAOException {
        return execute(id, "SELECT IMG FROM common.CHARTIMGS WHERE (ID=?)");
    }
    
    /**
     * Returns a Picture Gallery image.
     * @param id the gallery image ID
     * @param dbName the database name
     * @return the gallery image data
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getGalleryImage(int id, String dbName) throws DAOException {
    	StringBuilder sqlBuf = new StringBuilder("SELECT IMG FROM ");
    	sqlBuf.append(formatDBName(dbName));
    	sqlBuf.append(".GALLERY WHERE (ID=?)");
        return execute(id, sqlBuf.toString());
    }

    /**
     * Returns a Testing Center resource image.
     * @param id the question ID
     * @return the question image data
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getExamResource(int id) throws DAOException {
    	return execute(id, "SELECT IMG FROM exams.QUESTIONIMGS WHERE (ID=?)");
    }
}