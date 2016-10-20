// Copyright 2007, 2009, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.dao.DAOException;
import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.*;

/**
 * A Context object that allows fetching of connections from the connection pool.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public abstract class ConnectionContext {

	private static final Logger log = Logger.getLogger(ConnectionContext.class);
	
	private final ConnectionPool _pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
	private Connection _con;
	private boolean _autoCommit;
	
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

        // Check if a connection has already been reserved
        if (_con != null)
            throw new IllegalStateException("Connection already reserved");

        try {
			_con = _pool.getConnection();
		} catch (org.gvagroup.jdbc.ConnectionPoolException cpe) {
			throw new ConnectionPoolException(cpe);
		}
		
        return _con;
    }

    /*
     * Helper method to ensure a connection has been reserved.
     */
    private void checkConnection() {
       if (_con == null)
          throw new IllegalStateException("No JDBC Connection reserved");
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
   		   log.error("Error rolling back transaction - " + e.getMessage());
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