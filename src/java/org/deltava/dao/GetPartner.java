// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.PartnerInfo;

/**
 * A Data Access Object to load virtual airline partner information. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class GetPartner extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPartner(Connection c) {
		super(c);
	}

	/**
	 * Loads a Partner record from the database.
	 * @param id the database ID
	 * @return a PartnerInfo bean, or null
	 * @throws DAOException if a JDBC error occurs
	 */
	public PartnerInfo get(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT P.*, PI.X, PI.Y, PI.EXT FROM PARTNERS P LEFT JOIN PARTNER_IMGS PI ON (P.ID=PI.ID) WHERE (P.ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads Partner information from the database.
	 * @return a List of PartnerInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<PartnerInfo> getPartners() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.*, PI.X, PI.Y, PI.EXT FROM PARTNERS P LEFT JOIN PARTNER_IMGS PI ON (P.ID=PI.ID) ORDER BY P.PRIORITY DESC, PI.ID")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parser Partner result sets.
	 */
	private static List<PartnerInfo> execute(PreparedStatement ps) throws SQLException {
		List<PartnerInfo> results = new ArrayList<PartnerInfo>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				PartnerInfo pi = new PartnerInfo(rs.getString(3));
				pi.setID(rs.getInt(1));
				pi.setPriority(rs.getInt(2));
				pi.setURL(rs.getString(4));
				pi.setDescription(rs.getString(5));
				pi.setReferCount(rs.getInt(6));
				pi.setLastRefer(toInstant(rs.getTimestamp(7)));
				pi.setWidth(rs.getInt(8));
				pi.setHeight(rs.getInt(9));
				pi.setBannerExtension(rs.getString(10));
				results.add(pi);
			}
		}
		
		return results;
	}
}