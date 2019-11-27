// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.acars;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import org.deltava.dao.*;

public class GetFlightIDs extends DAO {

	public GetFlightIDs(Connection c) {
		super(c);
	}
	
	public BlockingQueue<Integer> getFlightIDs() throws DAOException {
		return getFlightIDs(new HashSet<String>());
	}

	public BlockingQueue<Integer> getFlightIDs(Collection<String> airports) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT ID FROM acars.FLIGHTS");
		if (airports.size() > 0) {
			buf.append(" WHERE ((AIRPORT_D IN (");
			for (Iterator<String> i = airports.iterator(); i.hasNext(); ) {
				String apCode = i.next();
				buf.append('\'');
				buf.append(apCode);
				buf.append('\'');
				if (i.hasNext())
					buf.append(',');
			}
			
			buf.append(")) OR (AIRPORT_A IN (");
			for (Iterator<String> i = airports.iterator(); i.hasNext(); ) {
				String apCode = i.next();
				buf.append('\'');
				buf.append(apCode);
				buf.append('\'');
				if (i.hasNext())
					buf.append(',');
			}
			
			buf.append(")))");
		}
		
		buf.append(" ORDER BY ID");
		
		try (PreparedStatement ps = prepare(buf.toString())) {
			BlockingQueue<Integer> results = new LinkedBlockingQueue<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
		
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}