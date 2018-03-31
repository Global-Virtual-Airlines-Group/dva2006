// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.comparators.ScheduleEntryComparator;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to search the Flight Schedule.
 * @author Luke
 * @version 8.2
 * @since 1.0
 */

public class GetScheduleSearch extends GetSchedule {
	
	private static enum ParamTypes {
		BASE, HAVING, ALL
	}
	
	private static class SearchParams {
		private final String _sql;
		private final List<String> _params = new ArrayList<String>();
		private final List<String> _endParams = new ArrayList<String>();
		
		SearchParams(String sql, Collection<String> params) {
			_sql = sql;
			_params.addAll(params);
		}
		
		public void addEndParameter(String param) {
			_endParams.add(param);
		}
		
		public String getSQL() {
			return _sql;
		}
		
		public List<String> getParams(ParamTypes t) {
			switch (t) {
			case ALL:
				List<String> result = new ArrayList<String>(_params);
				result.addAll(_endParams);
				return result;
				
			case HAVING:
				return _endParams;
				
			default:
				return _params;
			}
		}
	}

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetScheduleSearch(Connection c) {
		super(c);
	}

	/*
	 * Helper method to build search parameters.
	 */
	private static SearchParams build(ScheduleSearchCriteria criteria) {
		Collection<String> conditions = new LinkedHashSet<String>();
		List<String> params = new ArrayList<String>();
		
		// Add airline
		if (criteria.getAirline() != null) {
			conditions.add("S.AIRLINE=?");
			params.add(criteria.getAirline().getCode());
		}
			
		// Add flight number
		if (criteria.getFlightNumber() != 0) {
			conditions.add("S.FLIGHT=?");
			params.add(String.valueOf(criteria.getFlightNumber()));
		}
			
		// Add leg
		if (criteria.getLeg() != 0) {
			conditions.add("S.LEG=?");
			params.add(String.valueOf(criteria.getLeg()));
		}
		
		// Add departure airport
		if (criteria.getAirportD() != null) {
			conditions.add("S.AIRPORT_D=?");
			params.add(criteria.getAirportD().getIATA());
		}
		
		// Add arrival airport
		if (criteria.getAirportA() != null) {
			conditions.add("S.AIRPORT_A=?");
			params.add(criteria.getAirportA().getIATA());
		}
			
		// Set distance criteria +/- 150 miles
		if (criteria.getDistance() != 0) {
			conditions.add("S.DISTANCE >= ?");
			conditions.add("S.DISTANCE <= ?");
			params.add(String.valueOf(criteria.getDistance() - criteria.getDistanceRange()));
			params.add(String.valueOf(criteria.getDistance() + criteria.getDistanceRange()));
		}

		// Set flight time criteria +/- 1 hour
		if (criteria.getLength() != 0) {
			conditions.add("S.FLIGHT_TIME >= ?");
			conditions.add("S.FLIGHT_TIME <= ?");
			params.add(String.valueOf((criteria.getLength() / 10.0) - 1));
			params.add(String.valueOf((criteria.getLength() / 10.0) + 1));
		}

		// Set departure/arrival time criteria +/- 2 hours
		if (criteria.getHourD() != -1) {
			conditions.add("S.TIME_D >= ?");
			conditions.add("S.TIME_D <= ?");
			params.add(StringUtils.format(criteria.getHourD() - 1, "00") + ":00\'");
			params.add(StringUtils.format(criteria.getHourD() + 1, "00") + ":00\'");
		}

		if (criteria.getHourA() != -1) {
			conditions.add("S.TIME_A >= ?");
			conditions.add("S.TIME_A <= ?");
			params.add(StringUtils.format(criteria.getHourA() - 1, "00") + ":00\'");
			params.add(StringUtils.format(criteria.getHourA() + 1, "00") + ":00\'");
		}
		
		// Check whether to include Flight Academy flights
		if (!criteria.getIncludeAcademy()) {
			conditions.add("S.ACADEMY=?");
			params.add("0");
		}
		
		// Check to include unvisited airports only
		if (criteria.getPilotID() > 0) {
			if (criteria.getNotVisitedD()) {
				conditions.add("S.AIRPORT_D NOT IN (SELECT DISTINCT AIRPORT_D FROM PIREPS WHERE (PILOT_ID=?))");
				params.add(String.valueOf(criteria.getPilotID()));
				conditions.add("S.AIRPORT_D NOT IN (SELECT DISTINCT AIRPORT_A FROM PIREPS WHERE (PILOT_ID=?))");
				params.add(String.valueOf(criteria.getPilotID()));
			}
			
			if (criteria.getNotVisitedA()) {
				conditions.add("S.AIRPORT_A NOT IN (SELECT DISTINCT AIRPORT_D FROM PIREPS WHERE (PILOT_ID=?))");
				params.add(String.valueOf(criteria.getPilotID()));
				conditions.add("S.AIRPORT_A NOT IN (SELECT DISTINCT AIRPORT_A FROM PIREPS WHERE (PILOT_ID=?))");
				params.add(String.valueOf(criteria.getPilotID()));
			}
		}
		
		// Concat the criteria into a string so we can reuse
		StringBuilder cndBuf = new StringBuilder();
		if (!conditions.isEmpty()) {
			cndBuf.append('(');
			cndBuf.append(StringUtils.listConcat(conditions, ") AND ("));
			cndBuf.append(')');
		}
			
		if (!CollectionUtils.isEmpty(criteria.getEquipmentTypes())) {
			if (!conditions.isEmpty())
				cndBuf.append(" AND (");
			
			for (Iterator<String> i = criteria.getEquipmentTypes().iterator(); i.hasNext(); ) {
				String eqType = i.next();
				cndBuf.append("(S.EQTYPE=?)");
				params.add(eqType);
				if (i.hasNext())
					cndBuf.append(" OR ");
			}

			if (!conditions.isEmpty())
				cndBuf.append(')');
		}
		
		// Add to end params since these are included in a HAVING clause
		SearchParams result = new SearchParams(cndBuf.toString(), params);
		if (criteria.getRouteLegs() > -1)
			result.addEndParameter(String.valueOf(criteria.getRouteLegs()));
		if (criteria.getLastFlownInterval() > -1)
			result.addEndParameter(String.valueOf(criteria.getLastFlownInterval()));
		
		return result;
	}

	/**
	 * Searches the Schedule database for flights matching particular criteria.
	 * @param ssc the search criteria. Null properties are ignored
	 * @return a List of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ScheduleEntry> search(ScheduleSearchCriteria ssc) throws DAOException {
		return (ssc.getFlightsPerRoute() > 0) ? searchRoutePairs(ssc) : searchRoutes(ssc);
	}

	private List<ScheduleEntry> searchRoutes(ScheduleSearchCriteria ssc) throws DAOException {
		if (ssc.getDispatchOnly())
			ssc.setCheckDispatchRoutes(true);
		
		// Load all of the flights that match the criteria
		SearchParams spm = build(ssc);
		Collection<String> havingParams = new ArrayList<String>();
		if (ssc.getDispatchOnly())
			havingParams.add(" (RCNT>0)");
		if (ssc.getRouteLegs() > -1)
			havingParams.add(" (FCNT<=?)");
		if (ssc.getLastFlownInterval() > -1)
			havingParams.add(" (LF > DATE_SUB(CURDATE(), INTERVAL ? DAY))");
		
		StringBuilder buf = new StringBuilder("SELECT S.*, COUNT(R.ID) AS RCNT, IFNULL(FSR.CNT,0) AS FCNT, MAX(FSR.LASTFLIGHT) AS LF FROM ");
		buf.append(formatDBName(ssc.getDBName()));
		buf.append(".SCHEDULE S LEFT JOIN acars.ROUTES R ON ((S.AIRPORT_D=R.AIRPORT_D) AND (S.AIRPORT_A=R.AIRPORT_A) AND (R.ACTIVE=1)) LEFT JOIN ");
		buf.append(formatDBName(ssc.getDBName()));
		buf.append(".FLIGHTSTATS_ROUTES FSR ON ((FSR.PILOT_ID=?) AND (FSR.AIRPORT_D=S.AIRPORT_D) AND (FSR.AIRPORT_A=S.AIRPORT_A)) WHERE ");
		buf.append(spm.getSQL());
		buf.append(" GROUP BY S.AIRLINE, S.FLIGHT, S.LEG ");
		if (havingParams.size() > 0)
			buf.append(" HAVING");
		for (Iterator<String> i = havingParams.iterator(); i.hasNext(); ) {
			buf.append(i.next());
			if (i.hasNext())
				buf.append(" AND");
		}
		
		buf.append(" ORDER BY ");
		buf.append(ssc.getSortBy());
		
		try {
			prepareStatement(buf.toString()); 
			_ps.setInt(1, ssc.getPilotID()); int ofs = 1;
			for (String p : spm.getParams(ParamTypes.ALL))
				_ps.setString(++ofs, p);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	private List<ScheduleEntry> searchRoutePairs(ScheduleSearchCriteria ssc) throws DAOException {
		if (ssc.getDispatchOnly())
			ssc.setCheckDispatchRoutes(true);
		
		// Get route pairs
		SearchParams spm = build(ssc);
		Collection<String> havingParams = new ArrayList<String>();
		Collection<RoutePair> rts = new LinkedHashSet<RoutePair>();
		if (ssc.getDispatchOnly())
			havingParams.add(" (RCNT>0)");
		if (ssc.getRouteLegs() > -1)
			havingParams.add(" (FCNT<=?)");
		if (ssc.getLastFlownInterval() > -1)
			havingParams.add(" (LF > DATE_SUB(CURDATE(), INTERVAL ? DAY))");
		
		// Load the route pairs that match the criteria
		String db = formatDBName(ssc.getDBName());
		StringBuilder buf = new StringBuilder("SELECT DISTINCT S.AIRPORT_D, S.AIRPORT_A, COUNT(R.ID) AS RCNT, IFNULL(FSR.CNT,0) AS FCNT, MAX(FSR.LASTFLIGHT) AS LF FROM ");
		buf.append(db);
		buf.append(".SCHEDULE S LEFT JOIN acars.ROUTES R ON ((S.AIRPORT_D=R.AIRPORT_D) AND (S.AIRPORT_A=R.AIRPORT_A) AND (R.ACTIVE=1)) LEFT JOIN ");
		buf.append(db);		
		buf.append(".FLIGHTSTATS_ROUTES FSR ON ((FSR.PILOT_ID=?) AND (FSR.AIRPORT_D=S.AIRPORT_D) AND (FSR.AIRPORT_A=S.AIRPORT_A)) WHERE ");		
		buf.append(spm.getSQL());
		buf.append(" GROUP BY S.AIRPORT_D, S.AIRPORT_A");
		if (havingParams.size() > 0)
			buf.append(" HAVING ");
		for (Iterator<String> i = havingParams.iterator(); i.hasNext(); ) {
			buf.append(i.next());
			if (i.hasNext())
				buf.append(" AND ");
		}
		
		buf.append(" ORDER BY ");
		buf.append(ssc.getSortBy());
		
		try {
			prepareStatement(buf.toString());
			_ps.setInt(1, ssc.getPilotID()); int ofs = 1;
			for (String p : spm.getParams(ParamTypes.ALL))
				_ps.setString(++ofs, p);
			
			// Execute the query
			Airline al = SystemData.getAirline(SystemData.get("airline.code"));
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					RoutePair fr = new ScheduleRoute(al, SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2)));
					rts.add(fr);
				}
			}
		
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Load all of the flights that match each route pair and the criteria
		buf = new StringBuilder("SELECT S.*, COUNT(R.ID) AS RCNT, IFNULL(FSR.CNT,0) AS FCNT, MAX(FSR.LASTFLIGHT) AS LF FROM ");
		buf.append(db);
		buf.append(".SCHEDULE S LEFT JOIN acars.ROUTES R ON ((S.AIRPORT_D=R.AIRPORT_D) AND (S.AIRPORT_A=R.AIRPORT_A) AND (R.ACTIVE=1)) LEFT JOIN ");
		buf.append(db);
		buf.append(".FLIGHTSTATS_ROUTES FSR ON ((FSR.PILOT_ID=?) AND (FSR.AIRPORT_D=S.AIRPORT_D) AND (FSR.AIRPORT_A=S.AIRPORT_A)) WHERE ");
		buf.append(spm.getSQL());
		buf.append(" AND (S.AIRPORT_D=?) AND (S.AIRPORT_A=?) GROUP BY S.AIRLINE, S.FLIGHT, S.LEG");
		if (havingParams.size() > 0)
			buf.append(" HAVING ");
		for (Iterator<String> i = havingParams.iterator(); i.hasNext(); ) {
			buf.append(i.next());
			if (i.hasNext())
				buf.append(" AND ");
		}

		buf.append(" LIMIT ");
		buf.append(ssc.getFlightsPerRoute());

		// Prepare the satement and execute the query
		Map<RoutePair, List<ScheduleEntry>> entries = new LinkedHashMap<>();
		try {
			for (RoutePair fr : rts) {
				prepareStatementWithoutLimits(buf.toString());
				_ps.setInt(1, ssc.getPilotID()); int ofs = 1;
				for (String p : spm.getParams(ParamTypes.BASE))
					_ps.setString(++ofs, p);
					
				_ps.setString(++ofs, fr.getAirportD().getIATA());
				_ps.setString(++ofs, fr.getAirportA().getIATA());
				for (String p : spm.getParams(ParamTypes.HAVING))
					_ps.setString(++ofs, p);
			
				entries.put(fr, execute());
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Iterate through each collection of route pairs, adding flights until we hit the max
		List<ScheduleEntry> results = new ArrayList<ScheduleEntry>(_queryMax + 2);
		for (Iterator<List<ScheduleEntry>> i = entries.values().iterator(); i.hasNext() && (results.size() < _queryMax); ) {
			List<ScheduleEntry> entryList = i.next();
			int flightsAdded = -1;
			for (Iterator<ScheduleEntry> ei = entryList.iterator(); ei.hasNext() && (flightsAdded < ssc.getFlightsPerRoute()); flightsAdded++)
				results.add(ei.next());
		}

		// Do a sort - check for descending sort
		String sort = ssc.getSortBy().replace(" DESC", "");
		int sortType = StringUtils.arrayIndexOf(ScheduleSearchCriteria.SORT_CODES, sort, 0);
		if (sortType > 0) {
			Comparator<ScheduleEntry> cmp = new ScheduleEntryComparator(sortType);
			results.sort(ssc.getSortBy().endsWith("DESC") ? cmp.reversed() : cmp);
		} else
			Collections.shuffle(results);

		return results;
	}
}