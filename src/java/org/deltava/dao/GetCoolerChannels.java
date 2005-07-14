// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.cooler.*;
import org.deltava.beans.schedule.Airline;

import org.deltava.util.CollectionUtils;
import org.deltava.util.RoleUtils;

import org.deltava.util.cache.AgingCache;

/**
 * A Data Access Object to load Water Cooler channel profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetCoolerChannels extends DAO {

    private static AgingCache _cache = new AgingCache(4);

    /**
     * Create this DAO using a JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetCoolerChannels(Connection c) {
        super(c);
    }
    
    /**
     * Helper class to allow displaying of last subject in a channel.
     */
    public class LastPostMessage extends Message {
      
        private String _subject;
        
        LastPostMessage(int authorID) {
            super(authorID);
        }
        
        public String getSubject() {
            return _subject;
        }
        
        public void setSubject(String subj) {
            _subject = subj;
        }
    }

    /**
     * Retrieves a specific Channel profile. This populate the roles and airlines.
     * @param channelName the Channel name
     * @return the Channel profile
     * @throws DAOException if a JDBC error occurs
     */
    public Channel get(String channelName) throws DAOException {
        // Check if we're in the cache
        Channel c = (Channel) _cache.get(channelName);
        if (c != null)
            return c;

        try {
            prepareStatement("SELECT * FROM common.COOLER_CHANNELS WHERE (CHANNEL=?)");
            _ps.setString(1, channelName);

            // Execute the query - if nothing is returned, return null
            ResultSet rs = _ps.executeQuery();
            if (!rs.next())
                return null;

            // Initialize the channel
            c = new Channel(channelName);
            c.setDescription(rs.getString(2));
            c.setActive(rs.getBoolean(3));

            // Clean up the first set of JDBC resources
            rs.close();
            _ps.close();

            // Load the roles and airlines
            prepareStatementWithoutLimits("SELECT INFOTYPE, INFODATA FROM common.COOLER_CHANNELINFO WHERE (CHANNEL=?)");
            _ps.setString(1, channelName);

            // Execute the query
            rs = _ps.executeQuery();
            while (rs.next()) {
                switch (rs.getInt(1)) {
                    case Channel.INFOTYPE_ROLE:
                        c.addRole(rs.getString(2));
                        break;

                    case Channel.INFOTYPE_AIRLINE:
                        c.addAirline(rs.getString(2));
                        break;
                }
            }

            // Clean up
            rs.close();
            _ps.close();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
        
        // Add to cache and return
        _cache.add(c);
        return c;
    }

    /**
     * Retrieves all Channels for a particular airline.
     * @param al the Airline
     * @return a List of channels
     * @throws DAOException if a JDBC error occurs
     */
    public List getChannels(Airline al) throws DAOException {
        
        Map results = new TreeMap();
        try {
            prepareStatement("SELECT C.*, (SELECT T.ID FROM common.COOLER_THREADS T WHERE (T.CHANNEL=C.CHANNEL) "
            		+ "ORDER BY T.LASTUPDATE DESC LIMIT 1) AS LT, SUM(T.POSTS), COUNT(DISTINCT T.ID), SUM(T.VIEWS) FROM "
					+ "common.COOLER_CHANNELS C LEFT JOIN common.COOLER_THREADS T ON (T.CHANNEL=C.CHANNEL) WHERE "
					+ "(C.ACTIVE=?) GROUP BY C.CHANNEL");
            _ps.setBoolean(1, true);
            
            // Execute the query - we store results in a map for now
            ResultSet rs = _ps.executeQuery();

            // Iterate through the results
            while (rs.next()) {
                Channel c = new Channel(rs.getString(1));
                c.setDescription(rs.getString(2));
                c.setActive(rs.getBoolean(3));
                c.setLastThreadID(rs.getInt(4));
                c.setPostCount(rs.getInt(5));
                c.setThreadCount(rs.getInt(6));
                c.setViewCount(rs.getInt(7));

                // Add to the results with the channel name as the key
                results.put(c.getName(), c);
            }

            // Clean up the first set of JDBC resources
            rs.close();
            _ps.close();

            // Load the roles and airlines
            prepareStatementWithoutLimits("SELECT * FROM common.COOLER_CHANNELINFO");

            // Execute the query
            rs = _ps.executeQuery();
            while (rs.next()) {
                Channel c = (Channel) results.get(rs.getString(1));
                if (c != null) {
                    switch (rs.getInt(2)) {
                        case Channel.INFOTYPE_ROLE:
                            c.addRole(rs.getString(3));
                            break;

                        case Channel.INFOTYPE_AIRLINE:
                            c.addAirline(rs.getString(3));
                            break;
                    }
                }
            }

            // Clean up and return the values
            rs.close();
            _ps.close();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
        
        // Parse through the results and strip out from the airline - since it's cheaper to do it here than in the query
        List channels = new ArrayList(results.values());
        for (Iterator i = channels.iterator(); i.hasNext(); ) {
            Channel c = (Channel) i.next();
            if (!c.hasAirline(al.getCode()))
                i.remove();
        }
        
        // Add to cache and return
        _cache.addAll(channels);
        return channels;
    }

    /**
     * Retrieves all Channels for a particular airline and available to users with a given List of roles. If
     * any role in the list is &quot;Admin&quot; then all channels for the Airline will be returned.
     * @param al the Airline
     * @param roles a Collection of role names
     * @return a List of channels
     * @throws DAOException if a JDBC error occurs
     */
    public List getChannels(Airline al, Collection roles) throws DAOException {

        // Check if we are querying for the admin role; in this case return everything
        if (roles.contains("Admin"))
            return getChannels(al);

        List channels = getChannels(al);
        for (Iterator i = channels.iterator(); i.hasNext();) {
            Channel c = (Channel) i.next();
            if (!RoleUtils.hasAccess(roles, c.getRoles()))
                i.remove();
        }

        // Add to cache and return
        _cache.addAll(channels);
        return channels;
    }
    
    /**
     * Returns a Map of the last posts in a group of Channels, indexed by Post ID.
     * @param channels the channels to query
     * @return a Map of Messages, keyed by database ID
     * @throws DAOException if a JDBC error occurs
     */
    public Map getLastPosts(List channels) throws DAOException {
        
        // Build a set of post IDs
        Set idSet = new HashSet();
        for (Iterator i = channels.iterator(); i.hasNext(); ) {
            Channel ch = (Channel) i.next();
            if (ch.getLastThreadID() != 0)
                idSet.add(new Integer(ch.getLastThreadID()));
        }
        
        // If we have no post IDs, then return an empty map
        if (idSet.isEmpty())
            return Collections.EMPTY_MAP;
        
        // Init the prepared statement
        StringBuffer sqlBuf = new StringBuffer("SELECT T.* FROM common.COOLER_THREADS T WHERE (T.ID IN (");
        for (Iterator i = idSet.iterator(); i.hasNext(); ) {
            Integer id = (Integer) i.next();
            sqlBuf.append(id.toString());
            if (i.hasNext())
                sqlBuf.append(',');
        }
        
        // Strip out the trailing comma
        if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
            sqlBuf.setLength(sqlBuf.length() - 1);
        
        // Close and prepare the statement
        List results = new ArrayList();
        sqlBuf.append("))");
        try {
            prepareStatementWithoutLimits(sqlBuf.toString());
            
            // Execute the query and build the results list
            ResultSet rs = _ps.executeQuery();
            while (rs.next()) {
                LastPostMessage msg = new LastPostMessage(rs.getInt(13));
                msg.setThreadID(rs.getInt(1));
                msg.setID(msg.getThreadID());
                msg.setSubject(rs.getString(2));
                msg.setCreatedOn(rs.getTimestamp(12));

                // Add to results
                results.add(msg);
            }

            // Clean up
            rs.close();
            _ps.close();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
        
        // Convert to map and return
        return CollectionUtils.createMap(results, "ID");
    }
}