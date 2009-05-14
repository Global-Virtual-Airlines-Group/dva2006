// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.commands.CommandContext;

import org.deltava.util.system.SystemData;

/**
 * A Context object that allows fetching of connections from the connection pool.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public abstract class ConnectionContext {

	private static final Logger log = Logger.getLogger(ConnectionContext.class);
	
	private final ConnectionPool _pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
	private Connection _con;
	private boolean _autoCommit;
	
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
            throw new ConnectionPoolException("No Connection Pool defined", false);

        // Check if a connection has already been reserved
        if (_con != null)
            throw new IllegalStateException("Connection already reserved");

        _con = _pool.getConnection();
        return _con;
    }

    /**
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
     * @see CommandContext#commitTX()
     * @see CommandContext#rollbackTX()
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
     * @see CommandContext#startTX()
     * @see CommandContext#rollbackTX()
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
     * @see CommandContext#startTX()
     * @see CommandContext#commitTX()
     */
    public void rollbackTX() {
       try {
    	   if (_con != null) {
    		   if (!_con.getAutoCommit())
    			   _con.rollback();
    		   _con.setAutoCommit(_autoCommit);
    	   }
       } catch (Exception e) {
    	   log.error("Error rolling back transaction - " + e.getMessage(), e);
       }
    }
    
    /**
     * Returns a JDBC Connection to the connection pool.
     */
    public long release() {
        if ((_pool == null) || (_con == null))
            return 0;

        long timeUsed = _pool.release(_con);
        _con = null;
        return timeUsed;
    }
}