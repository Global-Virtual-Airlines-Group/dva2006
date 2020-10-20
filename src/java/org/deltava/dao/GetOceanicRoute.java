// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.navdata.*;
import org.deltava.beans.navdata.OceanicTrackInfo.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object for Oceanic Routes.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class GetOceanicRoute extends GetNavAirway {
	
	private static class ConcordeNAT extends OceanicTrack implements ExpiringCacheable {
		private final Direction _d;

		ConcordeNAT(String track, Direction d) {
			super(Type.NAT, track);
			_d = d;
		}

		@Override
		public final boolean isFixed() {
			return true;
		}
		
		@Override
		public final Direction getDirection() {
			return _d;
		}

		@Override
		public Instant getExpiryDate() {
			return Instant.MAX;
		}
	}
	
    /**
     * Initializes the DAO with the specified JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetOceanicRoute(Connection c) {
        super(c);
    }

    /**
     * Returns a list of oceanic routes.
     * @return a List of OceanicRoutes
     * @throws DAOException if a JDBC error occurs
     */
    public List<OceanicNOTAM> getOceanic() throws DAOException {
    	try (PreparedStatement ps = prepare("SELECT * FROM common.OCEANIC ORDER BY VALID_DATE DESC")) {
            return execute(ps);
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }

    /**
     * Returns a specific Oceanic Route.
     * @param routeType the route Type
     * @param vd the validity date
     * @return the OceanicRoute
     * @throws DAOException if a JDBC error occurs
     */
    public OceanicNOTAM get(Type routeType, Instant vd) throws DAOException {
    	try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.OCEANIC WHERE (ROUTETYPE=?) AND (VALID_DATE=DATE(?)) LIMIT 1")) {
    		ps.setInt(1, routeType.ordinal());
    		ps.setTimestamp(2, createTimestamp(vd));
    		return execute(ps).stream().findFirst().orElse(null);
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /*
     * Helper method to load Oceanic Route data.
     */
    private static List<OceanicNOTAM> execute(PreparedStatement ps) throws SQLException {
    	List<OceanicNOTAM> results = new ArrayList<OceanicNOTAM>();
        try (ResultSet rs = ps.executeQuery()) {
        	while (rs.next()) {
        		OceanicTrackInfo.Type rType = OceanicTrackInfo.Type.values()[rs.getInt(1)];
        		OceanicNOTAM or = new OceanicNOTAM(rType, expandDate(rs.getDate(2)));
        		or.setSource(rs.getString(3));
        		or.setRoute(rs.getString(4));
        		results.add(or);
        	}
        }	
        	
        return results;
    }
    
    /**
     * Returns Concorde-specific North Atlantic Tracks.
     * @return a Collection of ConcordeNAT beans
     * @throws DAOException if a JDBC error occurs
     */
    public List<OceanicTrack> loadConcordeNATs() throws DAOException {
    	final Direction[] CONC_NAT_DIRS = {Direction.WEST, Direction.EAST, Direction.ALL, Direction.ALL};
    	final List<List<String>> CONC_NAT_WPS = List.of(List.of("SM15W","SM20W","SM30W","SM40W","SM50W","SM53W","SM60W","SM65W","SM67W"), List.of("SN67W","SN65W","SN60W","SN52W","SN50W","SN40W","SN30W","SN20W","SN15W"), 
    			List.of("SO15W","SO20W","SO30W","SO40W","SO50W","SO52W","SO60W"), List.of("4720N", "4524N", "4230N", "3440N"));
    	
    	final Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
    	List<OceanicTrack> results = new ArrayList<OceanicTrack>(); int idx = 0;
    	for(String natID : List.of("SM", "SN", "SO", "SP")) {
    		CacheableList<Airway> aws = _aCache.get(natID);
    		if (aws == null) {
    			List<String> wps = CONC_NAT_WPS.get(idx);
        		ConcordeNAT rt = new ConcordeNAT(natID, CONC_NAT_DIRS[idx]);
        		rt.setDate(today);
        		for (String wpCode : wps) {
        			NavigationDataMap ndm = get(wpCode);
        			if (!ndm.isEmpty())
        				rt.addWaypoint(ndm.get(wpCode));
        		}
        		
        		// Add to cache
        		results.add(rt);
        		aws = new CacheableList<Airway>(natID);
        		aws.add(rt);
        		_aCache.add(aws);	
    		} else
    			results.add((OceanicTrack) aws.get(0));
    		
    		idx++;
    	}
    	
    	return results;
    }
    
    /**
     * Returns the dates for which oceanic track waypoints are available.
     * @param routeType the route type
     * @return a Collection of {@link java.util.Date} objects
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<Instant> getOceanicTrackDates(Type routeType) throws DAOException {
    	try (PreparedStatement ps = prepare("SELECT DISTINCT VALID_DATE FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) ORDER BY VALID_DATE DESC")) {
    		ps.setInt(1, routeType.ordinal());
    		Collection<Instant> results = new LinkedHashSet<Instant>();
    		try (ResultSet rs = ps.executeQuery()) {
    			while (rs.next())
    				results.add(Instant.ofEpochMilli(rs.getDate(1).getTime()));
    		}
    		
    		return results;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /**
     * Returns all of the oceanic route waypoints for a particular date.
     * @param routeType the route type
     * @param dt the date
     * @return a {@link DailyOceanicTracks} bean
     * @throws DAOException if a JDBC error occurs
     */
    public DailyOceanicTracks getOceanicTracks(Type routeType, Instant dt) throws DAOException {
    	
    	// Build the SQL statement
    	StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) AND ");
    	if (dt == null)
    		sqlBuf.append("(VALID_DATE = (SELECT MAX(VALID_DATE) FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?)))");
    	else
    		sqlBuf.append("(VALID_DATE=DATE(?))");
    	
    	sqlBuf.append(" ORDER BY TRACK, SEQ");
    	
    	try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
    		ps.setInt(1, routeType.ordinal());
    		if (dt != null)
    			ps.setTimestamp(2, createTimestamp(dt));
    		else
    			ps.setInt(2, routeType.ordinal());
    		
    		// Execute the query
    		OceanicTrack trk = null;
    		Collection<OceanicTrack> tmpResults = new ArrayList<OceanicTrack>();
    		try (ResultSet rs = ps.executeQuery()) {
    			while (rs.next()) {
    				String newTrack = rs.getString(3);
    				if ((trk == null) || (!newTrack.equals(trk.getTrack()))) {
    					trk = new OceanicTrack(routeType, newTrack);
    					trk.setDate(rs.getTimestamp(2).toInstant());
    					tmpResults.add(trk);
    				}
    			
    				// Get the waypoint placeholder - we'll populate later
    				Intersection np = new Intersection(rs.getString(5), rs.getDouble(6), rs.getDouble(7));
    				trk.addWaypoint(np);
    			}
    		}
    		
    		// Now populate the waypoint data using a different prepared statement
    		DailyOceanicTracks results = new DailyOceanicTracks(routeType, dt);
    		for (OceanicTrack ot : tmpResults) {
    			OceanicTrack nt = new OceanicTrack(ot.getType(), ot.getTrack());
    			for (NavigationDataBean np : ot.getWaypoints()) {
    				NavigationDataMap ndmap = get(np.getCode());
    				NavigationDataBean nd = ndmap.get(np.getCode(), np);
    				if (nd != null) {
						try {
							NavigationDataBean nd2 = (NavigationDataBean) nd.clone();
							if (StringUtils.isEmpty(nd2.getAirway()))
								nd2.setAirway(nt.getCode());
							
							nt.addWaypoint(nd2);
						} catch (CloneNotSupportedException cnse) {
							// 	empty
						}
    				}
    			}
    			
    			results.addTrack(nt);
    		}
    		
    		return results;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
}