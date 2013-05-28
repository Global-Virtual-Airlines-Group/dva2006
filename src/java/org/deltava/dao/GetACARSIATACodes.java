// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.IATACodes;
import org.deltava.beans.flight.FlightReport;

/**
 * A Data Access Object to fetch IATA codes used by aircraft.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

public class GetACARSIATACodes extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSIATACodes(Connection c) {
		super(c);
	}

	/**
	 * Returns all IATA codes for all equipment types.
	 * @return a Map of Collections of IATA codes, keyed by equipment type name
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, IATACodes> getAll(String db) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.EQTYPE, AP.CODE, COUNT(AP.ID) AS CNT FROM ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".PIREPS P, ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".ACARS_PIREPS AP WHERE (P.ID=AP.ID) AND (P.STATUS=?) AND (LENGTH(AP.CODE)>2) GROUP BY P.EQTYPE, "
				+ "AP.CODE HAVING (CNT>5) ORDER BY P.EQTYPE, CNT DESC"); 
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, FlightReport.OK);
			
			Map<String, IATACodes> results = new LinkedHashMap<String, IATACodes>();
			try (ResultSet rs = _ps.executeQuery()) {
				int max = 0; IATACodes c = null;
				while (rs.next()) {
					String code = rs.getString(1);
					if ((c == null) || !code.equals(c.getEquipmentType())) {
						max = 0;
						if (c != null)
							results.put(c.getEquipmentType(), c);
						
						c = new IATACodes(code);
					}
					
					int cnt = rs.getInt(3);
					max = Math.max(max, cnt);
					if (cnt > (max / 5))
						c.put(rs.getString(2), Integer.valueOf(cnt));
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}