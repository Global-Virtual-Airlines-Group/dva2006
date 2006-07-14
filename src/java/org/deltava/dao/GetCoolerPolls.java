// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.cooler.*;

/**
 * A Data Access Object to load Water Cooler poll data.
 * @author Luke
 * @version 1.0
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
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT V.PILOT_ID) FROM common.COOLER_POLLS P "
					+ "LEFT JOIN common.COOLER_VOTES V ON (P.ID=V.ID) AND (P.OPT_ID=V.OPT_ID) WHERE "
					+ "(P.ID=?) GROUP BY P.OPT_ID");
			_ps.setInt(1, threadID);
			
			// Execute the Query
			Collection<PollOption> results = new ArrayList<PollOption>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				PollOption opt = new PollOption(rs.getInt(1), rs.getString(3));
				opt.setOptionID(rs.getInt(2));
				opt.setVotes(rs.getInt(4));
				results.add(opt);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
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
		try {
			prepareStatement("SELECT * FROM common.COOLER_VOTES WHERE (ID=?)");
			_ps.setInt(1, threadID);
			
			// Execute the query
			Collection<PollVote> results = new ArrayList<PollVote>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				PollVote vote = new PollVote(rs.getInt(1), rs.getInt(2));
				vote.setOptionID(rs.getInt(3));
				results.add(vote);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}