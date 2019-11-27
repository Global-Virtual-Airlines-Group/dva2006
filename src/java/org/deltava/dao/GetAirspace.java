// Copyright 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 9.0
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
		
		try (PreparedStatement ps = prepare("SELECT ID, NAME, COUNTRY, TYPE, EXCLUSION, MIN_ALT, MAX_ALT, ST_AsWKT(DATA) FROM common.AIRSPACE WHERE (ID=?) AND (COUNTRY=?)")) {
			ps.setString(1, id);
			ps.setString(2, c.getCode());
			results = new CacheableList<Airspace>(key);
			try (ResultSet rs = ps.executeQuery()) {
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT ID, COUNTRY FROM common.AIRSPACE FORCE INDEX (AS_ALT_IDX) WHERE ST_Contains(DATA, ST_PointFromText(?,?)) AND (MIN_ALT<=?) AND (MAX_ALT>=?) AND (EXCLUSION=?) ORDER BY TYPE")) {
			ps.setString(1, formatLocation(loc));
			ps.setInt(2, WGS84_SRID);
			ps.setBoolean(3, false);
			ps.setInt(4, loc.getAltitude());
			ps.setInt(5, loc.getAltitude());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					codes.add(Tuple.create(rs.getString(1), Country.get(rs.getString(2))));
			}
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT ID, COUNTRY FROM common.AIRSPACE WHERE (TYPE<=?)")) {
			ps.setInt(1, AirspaceType.R.ordinal());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					codes.add(Tuple.create(rs.getString(1), Country.get(rs.getString(2))));
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return load(codes);
	}
	
	/*
	 * Helper method to load airspace from code collections.
	 */
	private List<Airspace> load(Collection<Tuple<String, Country>> codes) throws DAOException {
		List<Airspace> results = new ArrayList<Airspace>(codes.size() + 2);
		for (Tuple<String, Country> id : codes)
			results.addAll(get(id.getLeft(), id.getRight()));
		
		return results;
	}
}