// Copyright 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.simbrief.*;

/**
 * A Data Access Object to load SimBrief briefing package data.
 * @author Luke
 * @version 11.2
 * @since 10.4
 */

public class GetSimBriefPackages extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connectiont o use
	 */
	public GetSimBriefPackages(Connection c) {
		super(c);
	}

	/**
	 * Loads a SimBrief briefing package for a Flight Report.
	 * @param id the Flight Report database ID
	 * @param db the database name
	 * @return a SimBrief package, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public BriefingPackage getSimBrief(int id, String db) throws DAOException {
		
		StringBuilder sqlBuf = new StringBuilder("SELECT SIMBRIEF_ID, XML FROM ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".PIREP_SIMBRIEF WHERE (ID=?) LIMIT 1");
		
		BriefingPackage sbdata = null;
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					sbdata = SimBriefParser.parse(rs.getString(2));
					sbdata.setSimBriefID(rs.getString(1));
				}
			}
			
			return sbdata;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all aircraft tail codes used for flights in a given Equipment Type and Airline.
	 * @param eqType the Equipment Type
	 * @param a the Airline
	 * @param pilotID the Pilot database ID, or zero for all Pilots
	 * @return a List of Airframes, order by descending popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Airframe> getAirframes(String eqType, Airline a, int pilotID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT SB.TAILCODE, MAX(SB.AIRFRAME_ID), COUNT(SB.ID), MAX(SB.CREATED) FROM PIREP_SIMBRIEF SB, PIREPS P WHERE (P.ID=SB.ID) AND (P.PILOT_ID=?) AND (P.EQTYPE=?) AND (P.AIRLINE=?) GROUP BY SB.TAILCODE ORDER BY SB.CREATED DESC")) {
			ps.setInt(1, pilotID);
			ps.setString(2, eqType);
			ps.setString(3, a.getCode());
			
			List<Airframe> results = new ArrayList<Airframe>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Airframe af = new Airframe(rs.getString(1), rs.getString(2));
					af.setUseCount(rs.getInt(3));
					af.setLastUse(toInstant(rs.getTimestamp(4)));
					results.add(af);
				}
			}
			
			// Sort by popularity since order by uses last created date
			Collections.sort(results, Collections.reverseOrder());
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}