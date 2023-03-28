// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2016, 2019, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.News;
import org.deltava.beans.Notice;

/**
 * A Data Access Object to read System News entries.
 * @author Luke
 * @version 10.6
 * @since 1.0
 */

public class GetNews extends DAO {

	/**
	 * Initializes the DAO with a given JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetNews(Connection c) {
		super(c);
	}

	/**
	 * Returns a System News entry with a specific ID.
	 * @param id the database ID of the entry
	 * @return the System News entry
	 * @throws DAOException if a JDBC error occurs
	 */
	public News getNews(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT N.*, NI.X, NI.Y, NI.TYPE FROM NEWS N LEFT JOIN NEWS_IMGS NI ON (N.ID=NI.ID) WHERE (N.ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return executeNews(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the latest System News entries.
	 * @return a List of News beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<News> getNews() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT N.*, NI.X, NI.Y, NI.TYPE FROM NEWS N LEFT JOIN NEWS_IMGS NI ON (N.ID=NI.ID) ORDER BY N.DATE DESC")) {
			return executeNews(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a Notice to Airmen (NOTAM) with a specific ID.
	 * @param id the database ID of the entry
	 * @return the Notice to Airmen entry
	 * @throws DAOException if a JDBC error occurs
	 */
	public Notice getNOTAM(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT N.*, NI.X, NI.Y, NI.TYPE FROM NOTAMS N LEFT JOIN NOTAM_IMGS NI ON (N.ID=NI.ID) WHERE (N.ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return executeNOTAM(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the latest Notices to Airmen (NOTAMs).
	 * @return a List of Notice beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Notice> getNOTAMs() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT N.*, NI.X, NI.Y, NI.TYPE FROM NOTAMS N LEFT JOIN NOTAM_IMGS NI ON (N.ID=NI.ID) ORDER BY N.EFFDATE DESC")) {
			return executeNOTAM(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the latest active Notices to Airmen (NOTAMs).
	 * @return a List of Notice beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Notice> getActiveNOTAMs() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT N.*, NI.X, NI.Y, NI.TYPE FROM NOTAMS N LEFT JOIN NOTAM_IMGS NI ON (N.ID=NI.ID) WHERE (N.ACTIVE=?) ORDER BY N.EFFDATE DESC")) {
			ps.setBoolean(1, true);
			return executeNOTAM(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to iterate through the result set.
	 */
	private static List<News> executeNews(PreparedStatement ps) throws SQLException {
		List<News> results = new ArrayList<News>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				News n = new News(rs.getString(5), rs.getString(6));
				n.setID(rs.getInt(1));
				n.setAuthorID(rs.getInt(2));
				n.setDate(expandDate(rs.getDate(3)));
				n.setIsHTML(rs.getBoolean(4));
				n.setWidth(rs.getInt(7));
				if (n.getWidth() > 0) {
					n.setHeight(rs.getInt(8));
					n.setFormat(News.ImageFormat.values()[rs.getInt(9)]);
				}
				
				results.add(n);
			}
		}

		return results;
	}
	
	private static List<Notice> executeNOTAM(PreparedStatement ps) throws SQLException {
		List<Notice> results = new ArrayList<Notice>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Notice n = new Notice(rs.getString(4), rs.getString(5));
				n.setActive(rs.getBoolean(6));
				n.setID(rs.getInt(1));
				n.setAuthorID(rs.getInt(2));
				n.setDate(expandDate(rs.getDate(3)));
				n.setIsHTML(rs.getBoolean(7));
				n.setWidth(rs.getInt(8));
				if (n.getWidth() > 0) {
					n.setHeight(rs.getInt(9));
					n.setFormat(News.ImageFormat.values()[rs.getInt(10)]);
				}
				
				results.add(n);
			}
		}
		
		return results;
	}
}