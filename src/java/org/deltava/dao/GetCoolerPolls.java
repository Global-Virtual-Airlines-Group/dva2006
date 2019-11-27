// Copyright 2005, 2006, 2011, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.cooler.*;

/**
 * A Data Access Object to load Water Cooler poll data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetCoolerPolls extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetCoolerPolls(Connection c) {
		super(c);
	}

	/**
	 * Returns poll options for a Water Cooler discussion thread.
	 * @param threadID the Message Thread database ID
	 * @return a Collection of PollOption beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<PollOption> getOptions(int threadID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.*, COUNT(DISTINCT V.PILOT_ID) FROM common.COOLER_POLLS P LEFT JOIN common.COOLER_VOTES V ON (P.ID=V.ID) AND (P.OPT_ID=V.OPT_ID) "
			+ "WHERE (P.ID=?) GROUP BY P.OPT_ID")) {
			ps.setInt(1, threadID);
			
			Collection<PollOption> results = new ArrayList<PollOption>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					PollOption opt = new PollOption(rs.getInt(1), rs.getString(3));
					opt.setOptionID(rs.getInt(2));
					opt.setVotes(rs.getInt(4));
					results.add(opt);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all poll votes for a particular Water Cooler discussion thread.
	 * @param threadID the Message Thread database ID
	 * @return a Collection of PollVote beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<PollVote> getVotes(int threadID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM common.COOLER_VOTES WHERE (ID=?)")) {
			ps.setInt(1, threadID);
			Collection<PollVote> results = new ArrayList<PollVote>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					PollVote vote = new PollVote(rs.getInt(1), rs.getInt(2));
					vote.setOptionID(rs.getInt(3));
					results.add(vote);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}