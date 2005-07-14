package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.CollectionUtils;

/**
 * A DAO to get Pilot object(s) from the database for Water Cooler operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotCooler extends PilotReadDAO {

    /**
     * Creates the DAO from a JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetPilotCooler(Connection c) {
        super(c);
    }

    /**
     * Returns a Map of Pilots who posted in a Water Cooler message thread.
     * @param id the thread ID
     * @return a Map of pilots, indexed by the pilot code
     * @throws DAOException if a JDBC error occurs
     */
    public Map getPilotsByCoolerThread(int id) throws DAOException {
        // Init the prepared statement
        try {
            prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), " +
                    "MAX(F.DATE), S.ID FROM common.COOLER_POSTS CP, PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND " +
					"(F.STATUS=?)) LEFT JOIN SIGNATURES S ON (P.ID=S.ID) WHERE (CP.AUTHOR_ID=P.ID) AND (CP.THREAD_ID=?) " +
                    "GROUP BY P.ID");
            _ps.setInt(1, FlightReport.OK);
            _ps.setInt(2, id);
            
            // Convert the list of results into a searchable map
            List results = execute();
            _cache.addAll(results);
            return CollectionUtils.createMap(results, "ID");
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}