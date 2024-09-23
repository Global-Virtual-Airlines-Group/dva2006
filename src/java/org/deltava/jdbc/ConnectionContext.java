// Copyright 2007, 2009, 2010, 2016, 2017, 2021, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.dao.DAOException;
import org.deltava.util.system.SystemData;

import org.gvagroup.pool.*;

/**
 * A Context object that allows fetching of connections from a JDBC connection pool.
 * @author Luke
 * @version 11.3
 * @since 1.0
 */

public abstract class ConnectionContext {

	private static final Logger log = LogManager.getLogger(ConnectionContext.class);
	
	private final ConnectionPool<Connection> _pool = SystemData.getJDBCPool();
	private Connection _con;
	private boolean _autoCommit;
	
	private String _dbName;
	
	public static class ConnectionPoolException extends DAOException {
		
		ConnectionPoolException(String msg) {
			super(msg);
			setLogStackDump(false);
		}
		
		ConnectionPoolException(Throwable t) {
			super(t);
			setStackTrace(t.getStackTrace());
		}
	}
	
    /**
     * Reserves a JDBC Connection from the connection pool.
     * @return a JDBC Connection
     * @throws ConnectionPoolException if an error occurs
     * @throws IllegalStateException if a connection has already been reserved by this context
     * @see ConnectionContext#getConnection()
     * @see ConnectionContext#release()
     */
    public Connection getConnection() throws ConnectionPoolException {
        if (_pool == null)
            throw new ConnectionPoolException("No Connection Pool defined");
        if (_con != null)
            return _con;

        try {
			_con = _pool.getConnection();
		} catch (org.gvagroup.pool.ConnectionPoolException cpe) {
			throw new ConnectionPoolException(cpe);
		}
		
        return _con;
    }
    
    /**
     * Returns whether a JDBC connection has been reserved by this context.
     * @return TRUE if a Connection has been reserved, otherwise FALSE
     */
    public boolean hasConnection() {
    	return (_con != null);
    }

    /*
     * Helper method to ensure a connection has been reserved.
     */
    private void checkConnection() {
       if (_con == null)
          throw new IllegalStateException("No JDBC Connection reserved");
    }
    
    /**
     * Returns the default database name.
     * @return the database name
     */
    public String getDB() {
    	return _dbName;
    }
    
    /**
     * Updates the default database name.
     * @param dbName the database name
     */
    public void setDB(String dbName) {
    	_dbName = dbName;
    }
    
    /**
     * Starts a JDBC transaction block, by turning off autoCommit on the reserved Connection.
     * @throws IllegalStateException if no JDBC Connection is reserved
     * @throws TransactionException if a JDBC error occurs
     */
    public void startTX() throws TransactionException {
       checkConnection();
       try {
    	   _autoCommit =_con.getAutoCommit();
          _con.setAutoCommit(false);
       } catch (Exception e) {
          throw new TransactionException(e);
       }
    }

    /**
     * Commits the current JDBC transaction.
     * @throws IllegalStateException if no JDBC Connection is reserved
     * @throws TransactionException if a JDBC error occurs
     */
    public void commitTX() throws TransactionException {
       checkConnection();
       try {
    	   if (!_con.getAutoCommit())
    		   _con.commit();
          _con.setAutoCommit(_autoCommit);
       } catch (Exception e) {
          throw new TransactionException(e);
       }
    }
    
    /**
     * Rolls back the current JDBC transaction. This will consume all exceptions.
     */
    public void rollbackTX() {
       try {
    	   if (_con != null) {
    		   if (!_con.getAutoCommit())
    			   _con.rollback();
    		   _con.setAutoCommit(_autoCommit);
    	   }
       } catch (Exception e) {
   		   log.error("Error rolling back transaction - {}", e.getMessage());
       }
    }
    
    /**
     * Returns a JDBC Connection to the connection pool.
     * @return the time the connection was in use, in milliseconds
     */
    public long release() {
        if ((_pool == null) || (_con == null))
            return 0;

        long timeUsed = _pool.release(_con);
        _con = null;
        return timeUsed;
    }
}