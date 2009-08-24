// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.util.cache.CacheableLong;

/**
 * A Data Access Object to retrieve image data from the database.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetImage extends PilotSignatureDAO {

    /**
     * Initializes the DAO from a JDBC connection.
     * @param c the JDBC connection
     */
    public GetImage(Connection c) {
        super(c);
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
            prepareStatementWithoutLimits(sql);
            _ps.setInt(1, id);

            // Execute the query, if not found return null
            ResultSet rs = _ps.executeQuery();
            if (!rs.next())
                return null;
            
            // Get the image data
            ResultSetMetaData md = rs.getMetaData();
            byte[] imgBuffer = rs.getBytes(1);
            if (md.getColumnCount() > 1) {
            	Timestamp lastMod = rs.getTimestamp(2);
            	_sigCache.add(new CacheableLong(Integer.valueOf(id), lastMod.getTime()));
            }

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
    	StringBuilder sqlBuf = new StringBuilder("SELECT WC_SIG, MODIFIED FROM ");
    	sqlBuf.append(formatDBName(dbName));
    	sqlBuf.append(".SIGNATURES WHERE (ID=?) LIMIT 1");
        return execute(id, sqlBuf.toString());
    }
    
    /**
     * Returns the last modification date of a Pilot signature.
     * @param id the Pilot's database ID
     * @param dbName the database containing the signature
     * @return the signature last modified date/time, or null if not found
     * @throws DAOException if a JDBC error occurs
     */
    public java.util.Date getSigModified(int id, String dbName) throws DAOException {
    	CacheableLong lastMod = _sigCache.get(new Integer(id));
    	if (lastMod != null)
    		return new Date(lastMod.getValue());
    	
    	StringBuilder sqlBuf = new StringBuilder("SELECT MODIFIED FROM ");
    	sqlBuf.append(formatDBName(dbName));
    	sqlBuf.append(".SIGNATURES WHERE (ID=?) LIMIT 1");
    	
    	try {
    		prepareStatement(sqlBuf.toString());
    		_ps.setInt(1, id);
    		ResultSet rs = _ps.executeQuery();
    		java.util.Date dt = rs.next() ? rs.getTimestamp(1) : null;
    		rs.close();
    		_ps.close();
    		
    		// Update the cache and return
    		if (dt != null)
    			_sigCache.add(new CacheableLong(new Integer(id), dt.getTime()));
    		
    		return dt;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /**
     * Returns an Online Event banner image.
     * @param id the Event database ID
     * @return the banner image data
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getEventBanner(int id) throws DAOException {
    	return execute(id, "SELECT IMG FROM events.BANNERS WHERE (ID=?) LIMIT 1");
    }
    
    /**
     * Returns a Chart.
     * @param id the chart ID
     * @return the chart image data
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getChart(int id) throws DAOException {
        return execute(id, "SELECT IMG FROM common.CHARTIMGS WHERE (ID=?) LIMIT 1");
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
    	sqlBuf.append(".GALLERY WHERE (ID=?) LIMIT 1");
        return execute(id, sqlBuf.toString());
    }

    /**
     * Returns a Testing Center resource image.
     * @param id the question ID
     * @return the question image data
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getExamResource(int id) throws DAOException {
    	return execute(id, "SELECT IMG FROM exams.QUESTIONIMGS WHERE (ID=?) LIMIT 1");
    }
    
    /**
     * Returns if a Water Cooler signature image is officially approved.
     * @param id the Pilot's database ID
     * @return TRUE if approved, otherwise FALSE
     * @throws DAOException if a JDBC error occurs
     */
    public boolean isSignatureAuthorized(int id) throws DAOException {
    	try {
    		prepareStatementWithoutLimits("SELECT ISAPPROVED FROM SIGNATURES WHERE (ID=?) LIMIT 1");
    		_ps.setInt(1, id);
    		
    		// Execute the query
    		ResultSet rs = _ps.executeQuery();
    		boolean isOK = rs.next() ? rs.getBoolean(1) : false;
    		
    		// Clean up and return
    		rs.close();
    		_ps.close();
    		return isOK;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
}