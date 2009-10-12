// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to geo-locate IP addresses.
 * @author Luke
 * @version 2.6
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
		NetworkUtils.NetworkType netType = NetworkUtils.getNetworkType(NetworkUtils.convertIP(rawAddr));
		IPAddressInfo result = _cache.get(NetworkUtils.format(NetworkUtils.convertIP(rawAddr)));
		if (result != null)
			return result;
		
		try {
			prepareStatementWithoutLimits("SELECT (SELECT ic.ip_cidr FROM geoip.ip_group_country ic WHERE (ic.ip_start <= ct.ip_start) "
					+ "ORDER BY ic.ip_start DESC LIMIT 1) as cidr, ct.country_code, c.name, r.name, ct.city, ct.latitude, ct.longitude "
					+ "FROM geoip.countries c, geoip.ip_group_city ct LEFT JOIN geoip.regions r ON ((r.country_code=ct.country_code) "
					+ "AND (r.code=ct.region_code)) WHERE (ct.country_code=c.code) AND (ct.ip_start <= INET_ATON(?)) AND "
					+ "(ct.ip_start >= (INET_ATON(?) & ?)) ORDER BY ct.ip_start DESC LIMIT 1");
			_ps.setString(1, addr);
			_ps.setString(2, addr);
			_ps.setLong(3, netType.getMask());
			
			// Do the query
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				result = new IPAddressInfo(addr);
				result.setBlock(new IPBlock(rs.getString(1)));
				result.setCountryCode(rs.getString(2));
				result.setCountry(rs.getString(3));
				result.setRegion(rs.getString(4));
				result.setCity(rs.getString(5));
				result.setLocation(rs.getDouble(6), rs.getDouble(7));
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