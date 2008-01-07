// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.ProgramMetrics;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load equipment program metrics.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class GetProgramStatistics extends DAO {
	
	private static final Cache<ProgramMetrics> _cache = new ExpiringCache<ProgramMetrics>(8, 7200);

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
		try {
			// Load pilot data
			prepareStatementWithoutLimits("SELECT ID, RANK, STATUS, CREATED FROM PILOTS WHERE (EQTYPE=?)");
			_ps.setString(1, eqType.getName());
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Pilot p = new Pilot("x", "x");
				p.setID(rs.getInt(1));
				p.setEquipmentType(eqType.getName());
				p.setRank(rs.getString(2));
				p.setStatus(rs.getInt(3));
				p.setCreatedOn(rs.getTimestamp(4));
				pm.addPilot(p);
			}
			
			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Add to cache and return
		_cache.add(pm);
		return pm;
	}
}