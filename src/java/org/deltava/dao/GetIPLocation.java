// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.Country;
import org.deltava.beans.system.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to geo-locate IP addresses.
 * @author Luke
 * @version 3.4
 * @since 2.5
 */

public class GetIPLocation extends DAO implements CachingDAO {
	
	private static final Cache<IPAddressInfo> _cache = new ExpiringCache<IPAddressInfo>(1024, 86400);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetIPLocation(Connection c) {
		super(c);
	}
	
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
	}
	
	/**
	 * Retrieves Geolocation data for a particular IP address.
	 * @param addr the IP address
	 * @return information about this IP address, or null if none found
	 * @throws DAOException if a JDBC error occurs
	 */
	public IPAddressInfo get(String addr) throws DAOException {
		if (StringUtils.isEmpty(addr) || !addr.contains("."))
			return null;
		
		// Check the cache
		long rawAddr = NetworkUtils.pack(addr) & 0xFFFFFF00;
		IPAddressInfo result = _cache.get(NetworkUtils.format(NetworkUtils.convertIP(rawAddr)));
		if (result != null)
			return result;
		
		try {
			prepareStatementWithoutLimits("SELECT (SELECT ip_cidr FROM geoip.ip_group_country WHERE (ip_start <= INET_ATON(?)) "
				+ "ORDER BY ip_start DESC LIMIT 1) AS cidr, (SELECT ip_start FROM geoip.ip_group_city where (ip_start <= INET_ATON(?)) "
				+ "ORDER BY ip_start DESC LIMIT 1) AS ipblock");
			_ps.setString(1, addr);
			_ps.setString(2, addr);
			
			// Do the range start query
			long ip_start = -1;
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				result = new IPAddressInfo(addr);
				result.setBlock(new IPBlock(rs.getString(1)));
				ip_start = rs.getLong(2);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			
			// Load the IP info
			prepareStatementWithoutLimits("SELECT l.country_code, l.city, r.name, l.latitude, l.longitude FROM geoip.ip_group_city ic, "
				+ "geoip.locations l LEFT JOIN geoip.fips_regions r ON (l.region_code=r.code) AND (l.country_code=r.country_code) "
				+ "WHERE (ic.location=l.id) AND (ic.ip_start >= ?) ORDER BY ic.ip_start LIMIT 1");
			_ps.setLong(1, ip_start);
			rs = _ps.executeQuery();
			if (rs.next() && (result != null)) {
				result.setCountry(Country.get(rs.getString(1)));
				result.setCity(rs.getString(2));
				result.setRegion(rs.getString(3));
				result.setLocation(rs.getDouble(4), rs.getDouble(5));
			}
			
			// Clean up
			rs.close();
			_ps.close();
			
			// Add to cache and return
			_cache.add(result);
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}