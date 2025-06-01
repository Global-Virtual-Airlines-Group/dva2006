// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.*;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to read browser Reporting API data.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class GetBrowserReports extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetBrowserReports(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves all Reporting API reports from the database.
	 * @return a List of BrowserReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<BrowserReport> getBrowserReports() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM SYS_REPORTS")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns aggregated CSP violation data.
	 * @return a List of CSPViolations beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CSPViolations> getStatistics() throws DAOException {
		List<CSPViolations> results = new ArrayList<CSPViolations>();
		try (PreparedStatement ps = prepare("SELECT DATE(CREATED) AS DT, DIRECTIVE, HOST, GROUP_CONCAT(DISTINCT URL SEPARATOR ?) AS URLS, COUNT(*) AS CNT FROM SYS_REPORTS GROUP BY DT, DIRECTIVE, HOST ORDER BY DT DESC")) {
			ps.setString(1, " ");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CSPViolations cv = new CSPViolations(toInstant(rs.getTimestamp(1)), rs.getString(2));
					cv.setHost(rs.getString(3));
					List<String> urls = StringUtils.split(rs.getString(4), " ");
					urls.forEach(cv::addURL);
					cv.setCount(rs.getInt(5));
					results.add(cv);
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return results;
	}

	/**
	 * Retrieves all Reporting API reports for a particular URL from the database.
	 * @param url the site URL
	 * @return a List of BrowserReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<BrowserReport> getReportsByURL(String url) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM SYS_REPORTS WHERE (LOCATE(?,URL) > 0)")) {
			ps.setString(1, url);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
		
	/*
	 * Helper method to parse BrowserReport result sets.
	 */
	private static List<BrowserReport> execute(PreparedStatement ps) throws SQLException {
		List<BrowserReport> results = new ArrayList<BrowserReport>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				BrowserReport br = new BrowserReport(rs.getInt(2), rs.getString(6));
				br.setID(rs.getInt(1));
				br.setCreatedOn(toInstant(rs.getTimestamp(3)));
				br.setHost(rs.getString(4));
				br.setDirective(rs.getString(5));
				br.setURL(rs.getString(7));
				br.setBody(rs.getString(8));
				results.add(br);
			}
		}
		
		return results;	
	}
}