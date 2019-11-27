// Copyright 2009, 2010, 2011, 2012, 2013, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.net.*;
import java.sql.*;

import org.deltava.beans.schedule.Country;
import org.deltava.beans.system.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to geo-locate IP addresses.
 * @author Luke
 * @version 9.0
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
			
			// Check the cache
			IPBlock result = null;
			CacheWrapper<?> id = _blockCache.get(a.getHostAddress());
			if (id != null) {
				result = _cache.get(id.getValue());
				if (result != null)
					return result;
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("SELECT B.ID, INET6_NTOA(B.BLOCK_START), B.BITS, B.LAT, B.LNG, B.RADIUS, L.COUNTRY, L.REGION, L.CITY FROM geoip.BLOCKS B LEFT JOIN "
				+ "geoip.LOCATIONS L ON (B.LOCATION_ID=L.ID) WHERE (B.BLOCK_START <= INET6_ATON(?)) ORDER BY B.BLOCK_START DESC LIMIT 1")) {
				ps.setString(1, a.getHostAddress());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						result = new IPBlock(rs.getInt(1), rs.getString(2) + "/" + rs.getString(3));
						result.setLocation(rs.getDouble(4), rs.getDouble(5));
						result.setRadius(rs.getInt(6));
						result.setCountry(Country.get(rs.getString(7)));
						result.setRegion(rs.getString(8));
						result.setCity(rs.getString(9));
						_cache.add(result);
						_blockCache.add(new CacheWrapper<Object>(a.getHostAddress(), result.cacheKey()));
					}
				}
			}
			
			return result;
		} catch (UnknownHostException | SQLException ee) {
			throw new DAOException(ee);
		}
	}	
}