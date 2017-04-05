// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.Geometry;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

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
						Arrays.asList(geo.getCoordinates()).forEach(pt -> a.addBorderPoint(new GeoPosition(pt.x, pt.y)));
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
	 * 
	 * @param loc
	 * @return a Collection of Airspace beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airspace> find(GeospaceLocation loc) throws DAOException {
		Collection<Tuple<String, Country>> codes = new ArrayList<Tuple<String, Country>>();
		try {
			prepareStatementWithoutLimits("SELECT ID, COUNTRY FROM common.AIRSPACE WHERE ST_Contains(DATA, ST_PointFromText(?,?)) AND (MIN_ALT<=?) AND (MAX_ALT=>?)");
			_ps.setString(1, formatLocation(loc));
			_ps.setInt(2, GEO_SRID);
			_ps.setInt(3, loc.getAltitude());
			_ps.setInt(4, loc.getAltitude());
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					codes.add(Tuple.create(rs.getString(1), Country.get(rs.getString(2))));
			}
			
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Load results
		Collection<Airspace> results = new ArrayList<Airspace>();
		for (Tuple<String, Country> id : codes)
			results.addAll(get(id.getLeft(), id.getRight()));
		
		return results;
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
		
		// Load results
		Collection<Airspace> results = new ArrayList<Airspace>();
		for (Tuple<String, Country> id : codes)
			results.addAll(get(id.getLeft(), id.getRight()));
		
		return results;
	}
}