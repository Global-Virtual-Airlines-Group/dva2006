// Copyright 2008, 2011, 2012, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.ProgramMetrics;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load equipment program metrics.
 * @author Luke
 * @version 9.0
 * @since 2.1
 */

public class GetProgramStatistics extends DAO {
	
	private static final Cache<ProgramMetrics> _cache = CacheManager.get(ProgramMetrics.class, "ProgramStats");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetProgramStatistics(Connection c) {
		super(c);
	}
	
	/**
	 * Loads statistics about an equipment program.
	 * @param eqType the equipment program bean
	 * @return a PerformanceMetrics bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public ProgramMetrics getMetrics(EquipmentType eqType) throws DAOException {
		
		// Check the cache
		ProgramMetrics pm = _cache.get(eqType.getName());
		if (pm != null)
			return pm;
		
		pm = new ProgramMetrics(eqType);
		try (PreparedStatement ps = prepareWithoutLimits("SELECT ID, RANKING, STATUS, CREATED FROM PILOTS WHERE (EQTYPE=?)")) {
			ps.setString(1, eqType.getName());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Pilot p = new Pilot("x", "x");
					p.setID(rs.getInt(1));
					p.setEquipmentType(eqType.getName());
					p.setRank(Rank.values()[rs.getInt(2)]);
					p.setStatus(rs.getInt(3));
					p.setCreatedOn(toInstant(rs.getTimestamp(4)));
					pm.addPilot(p);
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		_cache.add(pm);
		return pm;
	}
}