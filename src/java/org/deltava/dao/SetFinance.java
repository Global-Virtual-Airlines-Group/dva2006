// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.finance.*;

/**
 * A Data Access Object to write financial data to the database.
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class SetFinance extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetFinance(Connection c) {
		super(c);
	}

	/**
	 * Writes a cost entry to the database.
	 * @param ce the CostEntry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(CostEntry ce) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO finance.COSTS VALUES (?,?,?,?,?)")) {
			ps.setInt(1, ce.getID());
			ps.setTimestamp(2, createTimestamp(ce.getDate()));
			ps.setString(3, ce.getCategory());
			ps.setInt(4, ce.getAmount());
			ps.setString(5, ce.getDescription());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes an AWS cost entry to the database.
	 * @param ce the AWSCostEntry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(AWSCostEntry ce) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO finance.AWS_COSTS VALUES (?,?,?,?,?,?)")) {
			ps.setInt(1, ce.getID());
			ps.setTimestamp(2, createTimestamp(ce.getDate()));
			ps.setString(3, ce.getService());
			ps.setInt(4, ce.getAmount());
			ps.setString(5, ce.getDescription());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a share price/count entry to the database.
	 * @param sp the SharePrice
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(SharePrice sp) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO finance.SHARE_PRICE VALUES (?,?,?,?)")) {
			ps.setInt(1, sp.getYear());
			ps.setInt(2, sp.getPrice());
			ps.setInt(3, sp.getCount());
			ps.setBoolean(4, sp.getActive());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a share purchase entry to the database.
	 * @param sp the SharePurchase
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(SharePurchase sp) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO finance.SHARES VALUES (?,?,?)")) {
			ps.setInt(1, sp.getYear());
			ps.setInt(2, sp.getAuthorID());
			ps.setInt(3, sp.getCount());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}