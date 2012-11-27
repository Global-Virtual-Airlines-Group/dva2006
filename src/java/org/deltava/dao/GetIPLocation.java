// Copyright 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.Country;
import org.deltava.beans.system.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to geo-locate IP addresses.
 * @author Luke
 * @version 5.0
 * @since 2.5
 */

public class GetIPLocation extends DAO {
	
	private static final Cache<IPBlock> _cache = CacheManager.get(IPBlock.class, "IPInfo");

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
		if (StringUtils.isEmpty(addr) || !addr.contains(".")) return null;
		
		// Check the cache
		IPBlock result = _cache.get(addr);
		if (result != null)
			return result;
		
		try {
			prepareStatementWithoutLimits("SELECT L.*, INET_NTOA(B.BLOCK_START), INET_NTOA(B.BLOCK_END), 32-LOG2(B.BLOCK_END-B.BLOCK_START) "
				+ "FROM geoip.BLOCKS B LEFT JOIN geoip.LOCATIONS L ON (L.ID=B.ID) WHERE (B.BLOCK_START <= INET_ATON(?)) ORDER BY "
				+ "B.BLOCK_START DESC LIMIT 1");
			_ps.setString(1, addr);
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					result = new IPBlock(rs.getInt(1), rs.getString(8), rs.getString(9), rs.getInt(10));
					result.setCountry(Country.get(rs.getString(2)));
					result.setRegion(rs.getString(3));
					result.setCity(rs.getString(4));
					result.setLocation(rs.getDouble(6), rs.getDouble(7));
				}
			}
			
			// Add to cache and return
			_ps.close();
			_cache.add(result);
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}