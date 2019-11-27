// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.Instant;

import org.deltava.util.cache.*;

import org.gvagroup.tile.TileAddress;

/**
 * A Data Access Object to retrieve image data from the database.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetImage extends DAO {
	
	private static final Cache<CacheableBlob> _imgCache = CacheManager.get(CacheableBlob.class, "Image");
	private static final Cache<CacheableLong> _sigCache = CacheManager.get(CacheableLong.class,  "Signature");
	private static final Cache<CacheableBlob> _trackCache = CacheManager.get(CacheableBlob.class, "Tracks");

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
    	try (PreparedStatement ps = prepareWithoutLimits(sql)) {
            ps.setInt(1, id);
            byte[] imgBuffer = null;
            try (ResultSet rs = ps.executeQuery()) {
            	if (rs.next()) {
            		imgBuffer = rs.getBytes(1);
            		if (rs.getMetaData().getColumnCount() > 1) {
            			Timestamp lastMod = rs.getTimestamp(2);
            			_sigCache.add(new CacheableLong(Integer.valueOf(id), lastMod.getTime()));
            		}
            	}
            }
            
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
    public java.time.Instant getSigModified(int id, String dbName) throws DAOException {
    	CacheableLong lastMod = _sigCache.get(Integer.valueOf(id));
    	if (lastMod != null)
    		return Instant.ofEpochMilli(lastMod.getValue());
    	
    	StringBuilder sqlBuf = new StringBuilder("SELECT MODIFIED FROM ");
    	sqlBuf.append(formatDBName(dbName));
    	sqlBuf.append(".SIGNATURES WHERE (ID=?) LIMIT 1");
    	
    	java.time.Instant dt = null;
    	try {
    		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
    			ps.setInt(1, id);
    			try (ResultSet rs = ps.executeQuery()) {
    				if (rs.next())
    					dt = toInstant(rs.getTimestamp(1));
    			}
    		}
    		
    		// Update the cache and return
    		if (dt != null)
    			_sigCache.add(new CacheableLong(Integer.valueOf(id), dt.toEpochMilli()));
    		
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
    	String cacheKey = "gallery-" + dbName + "-" + String.valueOf(id);
    	CacheableBlob imgData = _imgCache.get(cacheKey);
    	if (imgData != null)
    		return imgData.getData();
    	
    	StringBuilder sqlBuf = new StringBuilder("SELECT IMG FROM ");
    	sqlBuf.append(formatDBName(dbName));
    	sqlBuf.append(".GALLERY WHERE (ID=?) LIMIT 1");
        byte[] data = execute(id, sqlBuf.toString());
        if (data != null)
        	_imgCache.add(new CacheableBlob(cacheKey, data));
        
        return data;
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
     * Returns an ACARS track tile.
     * @param addr the TileAddress
     * @return the PNG tile data, or null if not found
     * @throws DAOException if a JDBC error occurs
     */
    public byte[] getTile(TileAddress addr) throws DAOException {
    	String cacheKey = "track-" + addr.getName();
    	CacheableBlob img = _trackCache.get(cacheKey);
    	if (img != null)
    		return img.getData();
    	
    	try (PreparedStatement ps = prepareWithoutLimits("SELECT IMG FROM acars.TRACKS WHERE (X=?) AND (Y=?) AND (Z=?) LIMIT 1")) {
    		ps.setInt(1, addr.getX());
    		ps.setInt(2, addr.getY());
    		ps.setInt(3, addr.getLevel());
    		
    		byte[] results = null;
    		try (ResultSet rs = ps.executeQuery()) {
    			if (rs.next())
    				results = rs.getBytes(1);
    		}

    		_trackCache.add(new CacheableBlob(cacheKey, results));
    		return results;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /**
     * Returns if a Water Cooler signature image is officially approved.
     * @param id the Pilot's database ID
     * @return TRUE if approved, otherwise FALSE
     * @throws DAOException if a JDBC error occurs
     */
    public boolean isSignatureAuthorized(int id) throws DAOException {
    	try (PreparedStatement ps = prepareWithoutLimits("SELECT ISAPPROVED FROM SIGNATURES WHERE (ID=?) LIMIT 1")) {
    		ps.setInt(1, id);
    		try (ResultSet rs = ps.executeQuery()) {
    			return rs.next() && rs.getBoolean(1);
    		}
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
}