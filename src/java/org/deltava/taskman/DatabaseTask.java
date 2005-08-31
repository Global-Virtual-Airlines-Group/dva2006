// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taskman;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.jdbc.Recycler;

/**
 * A class to support scheduled Tasks that access the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class DatabaseTask extends Task {
   
   private static final Logger log = Logger.getLogger(Task.class);

   protected Connection _con;
   private Recycler _recycler;
   
   /**
    * Creates a new Database Task.
    * @param name the task name
    */
   public DatabaseTask(String name) {
      super(name);
   }

   /**
    * Sets the JDBC connection that this Task will use.
    * @param c the JDBC connection
    */
   public void setConnection(Connection c) {
      _con = c;
   }
   
   /**
    * Sets the recycler to return the JDBC Connection to.
    * @param r the Connection Recycler
    */
   public void setRecycler(Recycler r) {
      _recycler = r;
   }
   
   /**
    * Executes the Task, then returns the JDBC connection.
    */
   public void run() {
      super.run();
      if (_recycler != null) {
         log.debug("Releasing JDBC connection for " + getName());
         _recycler.release(_con);
      }
   }
}