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
	 * Reads a cost ledger entry from the database.
	 * @param id the database ID
	 * @return a CostEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CostEntry getEntry(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM finance.COSTS WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			List<CostEntry> results = executeLedger(ps);
			return results.isEmpty() ? null : results.getFirst();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Reads an AWS cost ledger entry from the database.
	 * @param id the database ID
	 * @return an AWSCostEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public AWSCostEntry getAWSEntry(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM finance.AWS_COSTS WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			List<AWSCostEntry> results = executeAWSLedger(ps);
			return results.isEmpty() ? null : results.getFirst();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves the cost categories.
	 * @return a Collection of category names
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Category> getCategories() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM finance.CATEGORIES")) {
			Collection<Category> results = new ArrayList<Category>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(new Category(rs.getString(1), rs.getBoolean(2)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
			return executeLedger(ps);
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
			return executeAWSLedger(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves the Share price for a given fiscal year.
	 * @param year the year
	 * @return a SharePrice, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public SharePrice getSharePrice(int year) throws DAOException {
		return getSharePrices().stream().filter(sp -> (sp.getYear() == year)).findAny().orElse(null);
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
					sp.setActive(rs.getBoolean(4));
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
	 * Helper method to parse AWS ledger result sets.
	 */
	private static List<CostEntry> executeLedger(PreparedStatement ps) throws SQLException {
		List<CostEntry> results = new ArrayList<CostEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				CostEntry ce = new CostEntry(rs.getString(3), toInstant(rs.getTimestamp(2)), rs.getInt(4));
				ce.setID(rs.getInt(1));
				ce.setDescription(rs.getString(5));
				results.add(ce);
			}
		}

		return results;
	}
	
	/*
	 * Helper method to parse AWS cost ledger result sets.
	 */
	private static List<AWSCostEntry> executeAWSLedger(PreparedStatement ps) throws SQLException {
		List<AWSCostEntry> results = new ArrayList<AWSCostEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				AWSCostEntry ce = new AWSCostEntry(toInstant(rs.getTimestamp(2)), rs.getInt(4));
				ce.setID(rs.getInt(1));
				ce.setService(rs.getString(3));
				ce.setDescription(rs.getString(5));
				results.add(ce);
			}
		}

		return results;
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