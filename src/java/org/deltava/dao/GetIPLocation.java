// Copyright 2009, 2010, 2011, 2012, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.net.*;
import java.sql.*;

import org.deltava.beans.schedule.Country;
import org.deltava.beans.system.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to geo-locate IP addresses.
 * @author Luke
 * @version 6.0
 * @since 2.5
 */

public class GetIPLocation extends DAO {
	
	private static final Cache<IPBlock> _cache = CacheManager.get(IPBlock.class, "IPInfo");
	@SuppressWarnings("rawtypes")
	private static final Cache<CacheWrapper> _blockCache = CacheManager.get(CacheWrapper.class, "IPBlock"); 

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetIPLocation(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves Geolocation data for a particular IP address.
	 * @param addr the IP address
	 * @return information about this IP address, or null if none found
	 * @throws DAOException if a JDBC error occurs
	 */
	public IPBlock get(String addr) throws DAOException {
		try {
			InetAddress a = InetAddress.getByName(addr);
			return (a instanceof Inet4Address) ? getIP4(a) : getIP6(a);
		} catch (UnknownHostException uhe) {
			throw new DAOException(uhe);
		}
	}	
		
	private IPBlock getIP4(InetAddress addr) throws DAOException {		
		
		// Check the cache
		IPBlock result = null;
		CacheWrapper<?> id = _blockCache.get(addr.getHostAddress());
		if (id != null) {
			result = _cache.get(id.getValue());
			if (result != null)
				return result;
		}
		
		try {
			prepareStatementWithoutLimits("SELECT L.*, R.NAME, INET_NTOA(B.BLOCK_START), INET_NTOA(B.BLOCK_END), "
				+ "32-LOG2(B.BLOCK_END-B.BLOCK_START+1) FROM geoip.BLOCKS B LEFT JOIN geoip.LOCATIONS L ON (L.ID=B.ID) "
				+ "LEFT JOIN geoip.FIPS_REGIONS R ON ((L.COUNTRY=R.COUNTRY) AND (L.REGION=R.REGION)) WHERE "
				+ "(B.BLOCK_START <= INET_ATON(?)) ORDER BY B.BLOCK_START DESC LIMIT 1");
			_ps.setString(1, addr.getHostAddress());
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					result = new IP4Block(rs.getInt(1), rs.getString(9), rs.getString(10), rs.getInt(11));
					result.setCountry(Country.get(rs.getString(2)));
					result.setRegion(rs.getString(8));
					result.setCity(rs.getString(4));
					result.setLocation(rs.getDouble(6), rs.getDouble(7));
					_cache.add(result);
					_blockCache.add(new CacheWrapper<Object>(addr.getHostAddress(), result.cacheKey()));
				}
			}
			
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	private IPBlock getIP6(InetAddress addr) throws DAOException {

		// Check the cache
		IPBlock result = null;
		CacheWrapper<?> id = _blockCache.get(addr.getHostAddress());
		if (id != null) {
			result = _cache.get(id.getValue());
			if (result != null)
				return result;
		}
		
		try {
			prepareStatementWithoutLimits("SELECT ID, INET6_NTOA(BLOCK_START), INET6_NTOA(BLOCK_END), "
				+ "BITS, COUNTRY, LAT, LNG FROM geoip.BLOCKS6 WHERE (BLOCK_START <= INET6_ATON(?)) "
				+ "ORDER BY BLOCK_START DESC LIMIT 1");
			_ps.setString(1, addr.getHostAddress());			
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					result = new IP6Block(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
					result.setCountry(Country.get(rs.getString(5)));
					result.setLocation(rs.getDouble(6), rs.getDouble(7));
					_cache.add(result);
					_blockCache.add(new CacheWrapper<Object>(addr.getHostAddress(), result.cacheKey()));					
				}
			}
			
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}