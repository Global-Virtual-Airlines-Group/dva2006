// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.Geometry;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.util.GeoUtils;
import org.deltava.util.Tuple;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load Airspace boundaries from the database. 
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public class GetAirspace extends DAO {
	
	private static final Cache<CacheableCollection<Airspace>> _cache = CacheManager.getCollection(Airspace.class, "Airspace");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAirspace(Connection c) {
		super(c);
	}

	/**
	 * Loads an Airspace boundaries from the datbase.
	 * @param id the Airspace ID
	 * @param c the Country
	 * @return a Collection of Airspace beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airspace> get(String id, Country c) throws DAOException {
		
		// Check the cache
		String key = id + "!!" + c.getCode();
		CacheableCollection<Airspace> results = _cache.get(key);
		if (results != null)
			return results.clone();
		
		try {
			prepareStatement("SELECT ID, NAME, COUNTRY, TYPE, EXCLUSION, MIN_ALT, MAX_ALT, ST_AsWKT(DATA) FROM common.AIRSPACE WHERE (ID=?) AND (COUNTRY=?)");
			_ps.setString(1, id);
			_ps.setString(2, c.getCode());
			results = new CacheableList<Airspace>(key);
			try (ResultSet rs = _ps.executeQuery()) {
				Geometry geo = null; WKTReader wr = new WKTReader();
				while (rs.next()) {
					Airspace a = new Airspace(rs.getString(1), AirspaceType.values()[rs.getInt(4)]);
					a.setName(rs.getString(2));
					a.setCountry(Country.get(rs.getString(3)));
					a.setExclusion(rs.getBoolean(5));
					a.setMinAltitude(rs.getInt(6));
					a.setMaxAltitude(rs.getInt(7));
					geo = wr.read(rs.getString(8));
					if (geo != null) {
						a.setBorder(GeoUtils.fromGeometry(geo));
						results.add(a);
					}
				}
			}
			
			_ps.close();
			if (!results.isEmpty())
				_cache.add(results);
				
			return results.clone();
		} catch (SQLException | ParseException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Finds whether a particular point in space is contained within an Airspace boundary.
	 * @param loc the GeospaceLocation
	 * @return a Collection of Airspace beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Airspace> find(GeospaceLocation loc) throws DAOException {
		Collection<Tuple<String, Country>> codes = new ArrayList<Tuple<String, Country>>();
		try {
			prepareStatementWithoutLimits("SELECT ID, COUNTRY FROM common.AIRSPACE FORCE INDEX (AS_ALT_IDX) WHERE ST_Contains(DATA, ST_PointFromText(?,?)) AND (MIN_ALT<=?) AND (MAX_ALT>=?) AND (EXCLUSION=?) ORDER BY TYPE");
			_ps.setString(1, formatLocation(loc));
			_ps.setInt(2, GEO_SRID);
			_ps.setBoolean(3, false);
			_ps.setInt(4, loc.getAltitude());
			_ps.setInt(5, loc.getAltitude());
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					codes.add(Tuple.create(rs.getString(1), Country.get(rs.getString(2))));
			}
			
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return load(codes);
	}
	
	/**
	 * Returns all restricted airspace boundaries.
	 * @return a Collection of Airspace beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Airspace> getRestricted() throws DAOException {
		Collection<Tuple<String, Country>> codes = new ArrayList<Tuple<String, Country>>();
		try {
			prepareStatementWithoutLimits("SELECT ID, COUNTRY FROM common.AIRSPACE WHERE (TYPE<=?)");
			_ps.setInt(1, AirspaceType.R.ordinal());
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					codes.add(Tuple.create(rs.getString(1), Country.get(rs.getString(2))));
			}
			
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return load(codes);
	}
	
	/**
	 * Finds restricted airspace close to a particular point.
	 * @param loc the GeospaceLocation
	 * @param distance the distance in miles
	 * @return a Collection of Airspace beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airspace> findRestricted(GeospaceLocation loc, int distance) throws DAOException {
		Collection<Tuple<String, Country>> codes = new ArrayList<Tuple<String, Country>>();
		try {
			prepareStatementWithoutLimits("SELECT ID, COUNTRY FROM common.AIRSPACE FORCE INDEX (AS_TYPE_IDX, AS_ALT_IDX) WHERE (ST_Distance(DATA, ST_PointFromText(?,?)) < ?) AND (TYPE<?) AND "
				+"(MIN_ALT<=?) AND (MAX_ALT>=?) AND (EXCLUSION=?)");
			_ps.setString(1, formatLocation(loc));
			_ps.setInt(2, GEO_SRID);
			_ps.setDouble(3, (distance / GeoLocation.DEGREE_MILES));
			_ps.setInt(4, AirspaceType.R.ordinal());
			_ps.setInt(5, Math.max(0, loc.getAltitude() - 2500));
			_ps.setInt(6, Math.min(60000, loc.getAltitude() + 2500));
			_ps.setBoolean(7, false);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					codes.add(Tuple.create(rs.getString(1), Country.get(rs.getString(2))));
			}			
			
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return load(codes);
	}
	
	/**
	 * Loads all Airspace of a particular Type.
	 * @param t the AirspaceType
	 * @return a Collection of Airspace beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airspace> getByType(AirspaceType t) throws DAOException {
		Collection<Tuple<String, Country>> codes = new ArrayList<Tuple<String, Country>>();
		try {
			prepareStatementWithoutLimits("SELECT ID, COUNTRY FROM common.AIRSPACE WHERE (TYPE=?)");
			_ps.setInt(1, t.ordinal());
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					codes.add(Tuple.create(rs.getString(1), Country.get(rs.getString(2))));
			}
			
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return load(codes);
	}
	
	/*
	 * Helper method to load airpsace from code collections.
	 */
	private List<Airspace> load(Collection<Tuple<String, Country>> codes) throws DAOException {
		List<Airspace> results = new ArrayList<Airspace>();
		for (Tuple<String, Country> id : codes)
			results.addAll(get(id.getLeft(), id.getRight()));
		
		return results;
	}
}