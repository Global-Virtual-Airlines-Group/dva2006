// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taskman;

import java.sql.Connection;

/**
 * A class to support scheduled Tasks that access the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class DatabaseTask extends Task {
   
   protected Connection _con;
   
   /**
    * Creates a new Database Task.
    * @param name the task name
    */
   protected DatabaseTask(String name, Class loggerClass) {
      super(name, loggerClass);
   }

   /**
    * Sets the JDBC connection that this Task will use.
    * @param c the JDBC connection
    */
   public void setConnection(Connection c) {
      _con = c;
   }
}