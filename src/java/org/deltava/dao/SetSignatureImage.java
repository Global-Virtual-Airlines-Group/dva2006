// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to write Signature Images.
 * @author Luke
 * @version 2.1
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
	 * @throws DAOException if a JDBC error occurs
	 * @see Pilot#getHasSignature()
	 */
	public void write(Pilot p, int x, int y, String ext) throws DAOException {
		PilotDAO.invalidate(p.getID());
		try {
			prepareStatementWithoutLimits("REPLACE INTO SIGNATURES (ID, WC_SIG, X, Y, EXT) VALUES (?, ?, ?, ?, LCASE(?))");
			_ps.setInt(1, p.getID());
			_ps.setBinaryStream(2, p.getInputStream(), p.getSize());
			_ps.setInt(3, x);
			_ps.setInt(4, y);
			_ps.setString(5, ext);
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

		PilotDAO.invalidate(pilotID);
		try {
			prepareStatementWithoutLimits("DELETE FROM SIGNATURES WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}