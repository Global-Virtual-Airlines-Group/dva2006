// Copyright 2010, 2011, 2016, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.hr.*;

/**
 * A Data Access Object to load Senior Captain nominations.
 * @author Luke
 * @version 9.0
 * @since 3.3
 */

public class GetNominations extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetNominations(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the nominations a user has made in the current quarter.
	 * @param authorID the author's database ID
	 * @return a Collection of Nomination beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Nomination> getByAuthor(int authorID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT N.ID, N.QUARTER, N.SCORE, N.STATUS, MIN(NC.CREATED), COUNT(NC.AUTHOR), SUM(IF(NC.AUTHOR=?,1,0)) AS MINE FROM NOMINATIONS N LEFT JOIN "
			+ "NOMINATION_COMMENTS NC ON (N.ID=NC.ID) AND (N.QUARTER=NC.QUARTER) WHERE (N.QUARTER=?) GROUP BY N.ID HAVING (MINE>0) ORDER BY N.SCORE DESC, N.ID")) { 
			ps.setInt(1, authorID);
			ps.setInt(2, new Quarter().getYearQuarter());
			List<Nomination> results = execute(ps);
			for (Nomination n : results)
				loadComments(n);
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all nominations in a particular status.
	 * @param status the Nomination status
	 * @param q the Quarter
	 * @return a List of Nomination beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Nomination> getByStatus(Nomination.Status status, Quarter q) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT N.ID, N.QUARTER, N.SCORE, N.STATUS, MIN(NC.CREATED), COUNT(NC.AUTHOR) FROM NOMINATIONS N LEFT JOIN NOMINATION_COMMENTS NC "
			+ "ON (N.ID=NC.ID) AND (N.QUARTER=NC.QUARTER) WHERE (N.STATUS=?) ");
		if (q != null)
			sqlBuf.append("AND (N.QUARTER=?) ");
		sqlBuf.append("GROUP BY N.ID ORDER BY N.SCORE DESC, N.ID");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, status.ordinal());
			if (q != null)
				ps.setInt(2, q.getYearQuarter());
			
			List<Nomination> results = execute(ps);
			for (Nomination n : results)
				loadComments(n);
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all nominations for pilots in a particular equipment program in the current Quarter.
	 * @param eqType the equipment program
	 * @return a List of Nomination beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Nomination> getByEQType(String eqType) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT N.ID, N.QUARTER, N.SCORE, N.STATUS, MIN(NC.CREATED), COUNT(NC.AUTHOR) FROM PILOTS P, NOMINATIONS N LEFT JOIN NOMINATION_COMMENTS NC "
			+ "ON (N.ID=NC.ID) AND (N.QUARTER=NC.QUARTER) WHERE (N.ID=P.ID) AND (P.EQTYPE=?) AND (N.QUARTER=?) GROUP BY N.ID ORDER BY N.SCORE DESC, N.ID")) {
			ps.setString(1, eqType);
			ps.setInt(2, new Quarter().getYearQuarter());
			List<Nomination> results = execute(ps);
			for (Nomination n : results)
				loadComments(n);
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}			
	
	/**
	 * Returns the most recent Nomination for a Pilot.
	 * @param id the Pilot's database ID
	 * @return a Nomination bean, or null if none found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Nomination get(int id) throws DAOException {
		return get(id, null);
	}

	/**
	 * Loads a Nomination for a Pilot.
	 * @param id the Pilot's database ID
	 * @param q the Quarter
	 * @return a Nomination bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Nomination get(int id, Quarter q) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT N.*, MIN(NC.CREATED) AS CREATED FROM NOMINATIONS N, NOMINATION_COMMENTS NC WHERE (N.ID=?) AND (N.ID=NC.ID) AND (N.QUARTER=NC.QUARTER)");
		if (q != null)
			buf.append("AND (N.QUARTER=?) ");
		buf.append("GROUP BY N.QUARTER  HAVING (CREATED IS NOT NULL) ORDER BY N.QUARTER DESC LIMIT 1");
		
		try (PreparedStatement ps = prepareWithoutLimits(buf.toString())) {
			ps.setInt(1, id);
			if (q != null)
				ps.setInt(2, q.getYearQuarter());
			
			// Do the query
			Nomination n = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					n = new Nomination(rs.getInt(1));
					n.setQuarter(new Quarter(rs.getInt(2)));
					n.setScore(rs.getInt(3));
					n.setStatus(Nomination.Status.values()[rs.getInt(4)]);
					n.setCreatedOn(rs.getTimestamp(5).toInstant());
				}
			}
				
			if (n != null)
				loadComments(n);
			
			return n;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Nominations for a particular quarter.
	 * @param q the Quarter
	 * @return a List of Nominations
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Nomination> getAll(Quarter q) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT N.ID, N.QUARTER, N.SCORE, N.STATUS, MIN(NC.CREATED), COUNT(NC.AUTHOR) FROM NOMINATIONS N LEFT JOIN NOMINATION_COMMENTS NC ON (N.ID=NC.ID) "
			+ "AND (N.QUARTER=NC.QUARTER) WHERE (N.QUARTER=?) GROUP BY N.ID ORDER BY N.SCORE DESC, N.ID")) {
			ps.setInt(1, q.getYearQuarter());
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse Nomination result sets.
	 */
	private static List<Nomination> execute(PreparedStatement ps) throws SQLException {
		List<Nomination> results = new ArrayList<Nomination>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Nomination n = new Nomination(rs.getInt(1));
				n.setQuarter(new Quarter(rs.getInt(2)));
				n.setScore(rs.getInt(3));
				n.setStatus(Nomination.Status.values()[rs.getInt(4)]);
				n.setCreatedOn(rs.getTimestamp(5).toInstant());
				n.setCommentCount(rs.getInt(6));
				results.add(n);
			}
		}
		
		return results;
	}

	/*
	 * Helper method to load Nomination comments.
	 */
	private void loadComments(Nomination n) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT AUTHOR, SUPPORT, CREATED, BODY FROM NOMINATION_COMMENTS WHERE (ID=?) AND (QUARTER=?)")) {
			ps.setInt(1, n.getID());
			ps.setInt(2, n.getQuarter().getYearQuarter());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					NominationComment nc = new NominationComment(rs.getInt(1), rs.getString(4));
					nc.setSupport(rs.getBoolean(2));
					nc.setCreatedOn(rs.getTimestamp(3).toInstant());
					n.addComment(nc);
				}
			}
		}
	}
}