// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.jdbc;

/**
 * An exception thrown when a Connection Pool is full. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ConnectionPoolFullException extends ConnectionPoolException {

   public ConnectionPoolFullException() {
      super("Pool Full");
   }
}