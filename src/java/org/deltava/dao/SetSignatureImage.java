// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to write Signature Images.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class SetSignatureImage extends PilotSignatureDAO {

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
		invalidate(p.getID());
		try {
			prepareStatementWithoutLimits("REPLACE INTO SIGNATURES (ID, WC_SIG, X, Y, EXT, ISAPPROVED, "
					+ "MODIFIED) VALUES (?, ?, ?, ?, LCASE(?), ?, NOW())");
			_ps.setInt(1, p.getID());
			_ps.setBinaryStream(2, p.getInputStream(), p.getSize());
			_ps.setInt(3, x);
			_ps.setInt(4, y);
			_ps.setString(5, ext);
			_ps.setBoolean(6, isApproved);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Removes a Signature Image from the database.
	 * @param pilotID the Pilot ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int pilotID) throws DAOException {
		invalidate(pilotID);
		try {
			prepareStatementWithoutLimits("DELETE FROM SIGNATURES WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}