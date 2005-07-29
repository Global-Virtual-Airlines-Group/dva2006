// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.jdbc;

import java.sql.Connection;

/**
 * An interface for objects that JDBC Connections can be returned to. This interface is used as a means
 * of exposing Connection Pools to let connections be released, but not exposing all Pool behavior.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Recycler {

   /**
    * Releases a JDBC Connection.
    * @param c the Connection to release
    * @return the amount of time the connection was used for, in millseconds
    */
   public long release(Connection c);
}