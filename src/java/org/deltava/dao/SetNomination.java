// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.hr.*;

/**
 * A Data Access Object to write Senior Captain Nominations to the database.
 * @author Luke
 * @version 3.4
 * @since 3.3
 */

public class SetNomination extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetNomination(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Nomination to the database.
	 * @param n the Nomination bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(Nomination n) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO NOMINATIONS (SCORE, STATUS, QUARTER, ID) VALUES(?, ?, ?, ?)");
			_ps.setInt(1, n.getScore());
			_ps.setInt(2, n.getStatus().ordinal());
			_ps.setInt(3, new Quarter(n.getCreatedOn()).getYearQuarter());
			_ps.setInt(4, n.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates a Nomination in the database.
	 * @param n the Nomination bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Nomination n) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE NOMINATIONS SET SCORE=?, STATUS=? WHERE (QUARTER=?) AND (ID=?)");
			_ps.setInt(1, n.getScore());
			_ps.setInt(2, n.getStatus().ordinal());
			_ps.setInt(3, new Quarter(n.getCreatedOn()).getYearQuarter());
			_ps.setInt(4, n.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a NominationComment to the database.
	 * @param id the Nomination database ID
	 * @param nc the NominationComment bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(int id, NominationComment nc) throws DAOException {
		try {
			prepareStatement("REPLACE INTO NOMINATION_COMMENTS (ID, QUARTER, AUTHOR, SUPPORT, CREATED, BODY) "
				+ "VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, id);
			_ps.setInt(2, new Quarter(nc.getCreatedOn()).getYearQuarter());
			_ps.setInt(3, nc.getID());
			_ps.setBoolean(4, nc.getSupport());
			_ps.setTimestamp(5, createTimestamp(nc.getCreatedOn()));
			_ps.setString(6, nc.getBody());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Moves a nomination forward into the current Quarter.
	 * @param n the Nomination bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void adjustToCurrentQuarter(Nomination n) throws DAOException {
		try {
			prepareStatement("UPDATE NOMINATIONS SET QUARTER=? WHERE (ID=?) AND (QUARTER=?)");
			_ps.setInt(1, new Quarter().getYearQuarter());
			_ps.setInt(2, n.getID());
			_ps.setInt(3, new Quarter(n.getCreatedOn()).getYearQuarter());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}