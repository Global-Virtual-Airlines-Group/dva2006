// Copyright 2010, 2011, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.hr.*;

/**
 * A Data Access Object to write Senior Captain Nominations to the database.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO NOMINATIONS (SCORE, STATUS, QUARTER, ID) VALUES(?, ?, ?, ?)")) {
			ps.setInt(1, n.getScore());
			ps.setInt(2, n.getStatus().ordinal());
			ps.setInt(3, new Quarter(n.getCreatedOn()).getYearQuarter());
			ps.setInt(4, n.getID());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE NOMINATIONS SET SCORE=?, STATUS=? WHERE (QUARTER=?) AND (ID=?)")) {
			ps.setInt(1, n.getScore());
			ps.setInt(2, n.getStatus().ordinal());
			ps.setInt(3, n.getQuarter().getYearQuarter());
			ps.setInt(4, n.getID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Nomination Comment to the database.
	 * @param n the Nomination bean
	 * @param nc the NominationComment bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeComment(Nomination n, NominationComment nc) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO NOMINATION_COMMENTS (ID, QUARTER, AUTHOR, SUPPORT, CREATED, BODY) VALUES (?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, n.getID());
			ps.setInt(2, n.getQuarter().getYearQuarter());
			ps.setInt(3, nc.getID());
			ps.setBoolean(4, nc.getSupport());
			ps.setTimestamp(5, createTimestamp(nc.getCreatedOn()));
			ps.setString(6, nc.getBody());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE NOMINATIONS SET QUARTER=? WHERE (ID=?) AND (QUARTER=?)")) {
			ps.setInt(1, new Quarter().getYearQuarter());
			ps.setInt(2, n.getID());
			ps.setInt(3, new Quarter(n.getCreatedOn()).getYearQuarter());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}