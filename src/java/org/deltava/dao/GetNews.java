// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.News;
import org.deltava.beans.Notice;

/**
 * A Data Access Object to read System News entries.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT P.FIRSTNAME, P.LASTNAME, N.* FROM NEWS N, PILOTS P WHERE (N.ID=?) AND (N.PILOT_ID=P.ID) LIMIT 1")) {
			ps.setInt(1, id);
			return execute(ps).stream().map(News.class::cast).findFirst().orElse(null);
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
		try (PreparedStatement ps = prepare("SELECT P.FIRSTNAME, P.LASTNAME, N.* FROM NEWS N, PILOTS P WHERE (N.PILOT_ID=P.ID) ORDER BY N.DATE DESC")) {
			return execute(ps);
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT P.FIRSTNAME, P.LASTNAME, N.* FROM NOTAMS N, PILOTS P WHERE (N.ID=?) AND (N.PILOT_ID=P.ID) LIMIT 1")) {
			ps.setInt(1, id);
			return execute(ps).stream().map(Notice.class::cast).findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the latest Notices to Airmen (NOTAMs).
	 * @return a List of Notice beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<News> getNOTAMs() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.FIRSTNAME, P.LASTNAME, N.* FROM NOTAMS N, PILOTS P WHERE (N.PILOT_ID=P.ID) ORDER BY N.EFFDATE DESC")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the latest active Notices to Airmen (NOTAMs).
	 * @return a List of Notice beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<News> getActiveNOTAMs() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.FIRSTNAME, P.LASTNAME, N.* FROM NOTAMS N, PILOTS P WHERE (N.PILOT_ID=P.ID) AND (N.ACTIVE=?) ORDER BY N.EFFDATE DESC")) {
			ps.setBoolean(1, true);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to iterate through the result set.
	 */
	private static List<News> execute(PreparedStatement ps) throws SQLException {
		List<News> results = new ArrayList<News>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean isNOTAM = (rs.getMetaData().getColumnCount() > 7);
			while (rs.next()) {
				String authorName = rs.getString(1) + " " + rs.getString(2);

				News n;
				if (isNOTAM) {
					Notice notam = new Notice(rs.getString(6), authorName, rs.getString(7));
					notam.setActive(rs.getBoolean(8));
					notam.setIsHTML(rs.getBoolean(9));
					n = notam;
				} else
					n = new News(rs.getString(6), authorName, rs.getString(7));

				n.setID(rs.getInt(3));
				n.setAuthorID(rs.getInt(4));
				n.setDate(expandDate(rs.getDate(5)));
				results.add(n);
			}
		}

		return results;
	}
}