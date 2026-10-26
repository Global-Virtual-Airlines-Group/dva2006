// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.finance.*;

/**
 * A Data Access Object to load financial data from the database.
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class GetFinance extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFinance(Connection c) {
		super(c);
	}

	/**
	 * Returns all cost leder entries for a given fiscal year.
	 * @param year the year
	 * @return a List of CostEntries, in reverse date order
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CostEntry> getCostLedger(int year) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM finance.COSTS WHERE (YEAR(DATE)=?) ORDER BY DATE DESC")) {
			ps.setInt(1, year);
			
			// Execute the query
			List<CostEntry> results = new ArrayList<CostEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CostEntry ce = new CostEntry(rs.getString(2), toInstant(rs.getTimestamp(1)), rs.getInt(3));
					ce.setDescription(rs.getString(4));
					results.add(ce);
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns AWS hosting costs by month, broken down by service.
	 * @param year the year
	 * @param month the month
	 * @return a List of AWSCostEntry beans, in reverse date order
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AWSCostEntry> getAWSCosts(int year, int month) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM finance.AWS_COSTS WHERE (YEAR(DATE)=?) AND (MONTH(DATE)=?) ORDER BY DATE DESC")) {
			ps.setInt(1, year);
			ps.setInt(2, month);

			// Execute the query
			List<AWSCostEntry> results = new ArrayList<AWSCostEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					AWSCostEntry ce = new AWSCostEntry(toInstant(rs.getTimestamp(1)), rs.getInt(3));
					ce.setService(rs.getString(2));
					ce.setDescription(rs.getString(4));
					results.add(ce);
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a list of Share prices.
	 * @return a Collection of SharePrice beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SharePrice> getSharePrices() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM finance.SHARE_PRICE ORDER BY YEAR DESC")) {
			Collection<SharePrice> results = new ArrayList<SharePrice>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					SharePrice sp = new SharePrice(rs.getInt(1), rs.getInt(2));
					sp.setCount(rs.getInt(3));
					results.add(sp);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all share purhcases for a given fiscal year.
	 * @param year the fiscal year
	 * @return a Collection of SharePurchases
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SharePurchase> getShares(int year) throws DAOException {
		try (PreparedStatement ps = prepare("SELCET * FROM finance.SHARES WHERE (YEAR=?)")) {
			ps.setInt(1, year);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all share purhcases for a given owner.
	 * @param ownerID the owner's database ID
	 * @return a Collection of SharePurchases
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SharePurchase> getSharePurchases(int ownerID) throws DAOException {
		try (PreparedStatement ps = prepare("SELCET * FROM finance.SHARES WHERE (OWNER_ID=?)")) {
			ps.setInt(1, ownerID);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse SharePurchase result sets.
	 */
	private static List<SharePurchase> execute(PreparedStatement ps) throws SQLException {
		List<SharePurchase> results = new ArrayList<SharePurchase>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				SharePurchase sp = new SharePurchase(rs.getInt(1), rs.getInt(2), rs.getInt(3));
				results.add(sp);
			}
		}
		
		return results;
	}
}