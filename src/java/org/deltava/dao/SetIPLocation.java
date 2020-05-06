// Copyright 2013, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Collection;

import org.deltava.beans.system.*;

/**
 * A Data Access Object to write IP netblock data.
 * @author Luke
 * @version 9.0
 * @since 5.2
 */

public class SetIPLocation extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetIPLocation(Connection c) {
		super(c);
	}
	
	/**
	 * Writes an IP location entry to the database.
	 * @param loc an IPLocation bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(IPLocation loc) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO geoip.LOCATIONS (ID, COUNTRY, REGION, REGION_NAME, CITY) VALUES (?, ?, ?, ?, ?) AS new(id, c, r, rn, ct) ON DUPLICATE KEY "
				+ "UPDATE COUNTRY=new.c, REGION=new.r, REGION_NAME=new.rn, CITY=new.ct")) {
			ps.setInt(1, loc.getID());
			ps.setString(2, loc.getCountry().getCode());
			ps.setString(3, loc.getRegionCode());
			ps.setString(4, loc.getRegion());
			ps.setString(5, loc.getCityName());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes an IP network block to the database.
	 * @param blocks a Collection of IPBlocks
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Collection<IPBlock> blocks) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO geoip.BLOCKS (ID, BLOCK_START, BLOCK_END, BITS, LOCATION_ID, LAT, LNG, RADIUS) VALUES (?, INET6_ATON(?), INET6_ATON(?), ?, ?, ?, ?, ?) "
			+ "AS new (id, bs, be, bts, loc, lat, lng, rd) ON DUPLICATE KEY UPDATE BLOCK_START=new.bs, BLOCK_END=new.be, BITS=new.bts, LAT=new.lat, LNG=new.lng, RADIUS=new.rd")) {
			for (IPBlock ib : blocks) {
				ps.setInt(1, ib.getID());
				ps.setString(2, ib.getAddress());
				ps.setString(3, ib.getLastAddress());
				ps.setInt(4, ib.getBits());
				ps.setInt(5, Integer.parseInt(ib.getCity()));
				ps.setDouble(6, ib.getLatitude());
				ps.setDouble(7, ib.getLongitude());
				ps.setInt(8, ib.getRadius());
				ps.addBatch();
			}
			
			executeUpdate(ps, 1, blocks.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}