package org.deltava.dao;

import java.sql.*;

/**
 * A DAO to get the size of a table.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class GetCount extends DAO {

    private static final String SQL = "SELECT COUNT(*) FROM ";
    
    /**
     * Initializes the DAO with the specified JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetCount(Connection c) {
        super(c);
    }
    
    /**
     * Executes the DAO command.
     * @param tableName the name of the table to query
     * @return the number of rows in the table
     * @throws DAOException if a JDBC error occurs
     */
    public int execute(String tableName) throws DAOException {
        try {
            prepareStatement(SQL + tableName);
            ResultSet rs = _ps.executeQuery();
            
            if (!rs.next())
                return 0;
            
            return rs.getInt(1);
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}