// Copyright 2010, 2011, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.Geometry;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.FIR;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.Tuple;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load FIR data.
 * @author Luke
 * @version 7.0
 * @since 3.2
 */

public class GetFIR extends DAO {
	
	private static final Cache<FIR> _cache = CacheManager.get(FIR.class, "FIRs");
	private static final Cache<CacheableString> _idCache = CacheManager.get(CacheableString.class, "FIRIDs");
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFIR(Connection c) {
		super(c);
	}

	/**
	 * Retrieves FIR data.
	 * @param id the FIR ID.
	 * @param isOceanic TRUE if an Oceanic FIR, otherwise FALSE
	 * @return an FIR bean, or null if not found
	 * @throws DAOException if a JDBC or WKT error occurs
	 */
	public FIR get(String id, boolean isOceanic) throws DAOException {
		
		// Get the ID
		CacheableString idCacheKey = _idCache.get(id);
		if ((idCacheKey != null) && (!idCacheKey.getValue().equals(id)))
			return get(idCacheKey.getValue(), isOceanic);

		// Build the cache key
		String cacheKey = id.trim().toUpperCase();
		if (isOceanic)
			cacheKey += " Oceanic";
		
		// Check the cache
		FIR fir = _cache.get(cacheKey);
		if (fir != null)
			return fir;
		
		try {
			prepareStatementWithoutLimits("SELECT F.ID, F.NAME, F.OCEANIC, ST_AsWKT(F.DATA) FROM common.FIR F LEFT JOIN common.FIRALIAS FA "
				+ "ON (F.ID=FA.ID) WHERE (F.ID=?) OR (FA.ALIAS=?) ORDER BY IF(F.OCEANIC=?, 0, 1) LIMIT 1");
			_ps.setString(1, id);
			_ps.setString(2, id);
			_ps.setBoolean(3, isOceanic);
			
			// Execute the query
			Geometry geo = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					WKTReader wr = new WKTReader();
					fir = new FIR(rs.getString(1));
					fir.setName(rs.getString(2));
					fir.setOceanic(rs.getBoolean(3));
					fir.setAux(fir.getName().contains(" Aux"));
					geo = wr.read(rs.getString(4));
				}
			}
				
			_ps.close();
			if ((fir == null) || (geo == null))
				return null;
			
			// Parse border coordinates
			final FIR f = fir;
			Arrays.asList(geo.getCoordinates()).forEach(c -> f.addBorderPoint(new GeoPosition(c.x, c.y)));
				
			// Load aliases
			prepareStatementWithoutLimits("SELECT ALIAS FROM common.FIRALIAS WHERE (ID=?) AND (OCEANIC=?)");
			_ps.setString(1, fir.getID());
			_ps.setBoolean(2, fir.isOceanic());				
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					fir.addAlias(rs.getString(1));
					_idCache.add(new CacheableString(id, rs.getString(1)));
				}
			}
				
			// Clean up and add to cache
			_ps.close();
			_cache.add(fir);
			return fir;
		} catch (SQLException | ParseException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all FIRs.
	 * @return a Collection of FIR beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FIR> getAll() throws DAOException {
		Collection<Tuple<String, Boolean>> IDs = new ArrayList<Tuple<String,Boolean>>(400);
		try {
			prepareStatementWithoutLimits("SELECT ID, OCEANIC FROM common.FIR ORDER BY ID");
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					IDs.add(Tuple.create(rs.getString(1), Boolean.valueOf(rs.getBoolean(2))));
			}
			
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		Collection<FIR> results = new LinkedHashSet<FIR>();
		for (Tuple<String, Boolean> id : IDs) {
			FIR f = get(id.getLeft(), id.getRight().booleanValue());
			if (f != null)
				results.add(f);
		}
		
		return results;
	}

	/**
	 * Deternines the FIR for a given point.
	 * @param loc the point
	 * @return an FIR object, or null if unknown
	 * @throws DAOException if a JDBC error occurs
	 */
	public FIR search(GeoLocation loc) throws DAOException {
		String pt = formatLocation(loc);
		//String pt = "POINT(" + String.valueOf(loc.getLatitude()) + " " + String.valueOf(loc.getLongitude()) + ")";
		try {
			prepareStatementWithoutLimits("SELECT ID, OCEANIC FROM common.FIR WHERE ST_Contains(data, ST_PointFromText(?,?))");
			_ps.setString(1, pt);
			_ps.setInt(2, GEO_SRID);
			
			String id = null; boolean isOceanic = false;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					id = rs.getString(1);
					isOceanic = rs.getBoolean(2);
				}
			}
			
			_ps.close();
			if (id != null)
				return get(id, isOceanic);
			
			prepareStatementWithoutLimits("SELECT ID, OCEANIC FROM common.FIR WHERE ST_Intersects(data, ST_PointFromText(?,?))");
			_ps.setString(1, pt);
			_ps.setInt(2, GEO_SRID);
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					id = rs.getString(1);
					isOceanic = rs.getBoolean(2);
				}
			}

			_ps.close();
			return (id == null) ? null : get(id, isOceanic);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}