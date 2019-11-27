// Copyright 2005, 2006, 2008, 2009, 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Pilot;

import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write Signature Images.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetSignatureImage extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetSignatureImage(Connection c) {
		super(c);
	}

	/**
	 * Writes a Signature Image to the Database. The image data should be contained within the Pilot bean.
	 * @param p the Pilot containing the Signature image
	 * @param x the Image width in pixels
	 * @param y the Image height in pixels
	 * @param ext the Image extension
	 * @param isApproved whether the signature has been approved by management
	 * @throws DAOException if a JDBC error occurs
	 * @see Pilot#getHasSignature()
	 */
	public void write(Pilot p, int x, int y, String ext, boolean isApproved) throws DAOException {
		try (PreparedStatement ps = 	prepareWithoutLimits("REPLACE INTO SIGNATURES (ID, WC_SIG, X, Y, EXT, ISAPPROVED, MODIFIED) VALUES (?, ?, ?, ?, LCASE(?), ?, NOW())")) {
			ps.setInt(1, p.getID());
			ps.setBinaryStream(2, p.getInputStream(), p.getSize());
			ps.setInt(3, x);
			ps.setInt(4, y);
			ps.setString(5, ext);
			ps.setBoolean(6, isApproved);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Signature", p.cacheKey());
		}
	}

	/**
	 * Removes a Signature Image from the database.
	 * @param pilotID the Pilot ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int pilotID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM SIGNATURES WHERE (ID=?)")) {
			ps.setInt(1, pilotID);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Signature", Integer.valueOf(pilotID));
		}
	}
}