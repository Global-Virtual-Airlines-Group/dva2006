// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

/**
 * A bean to store Web Site Command statistics.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class CommandStatsEntry implements java.io.Serializable, Comparable<CommandStatsEntry> {

   private String _name;
   private int _count;
   private int _successCount;
   
   private int _avgTime;
   private int _avgBackEndTime;
   private int _maxTime;
   private int _maxBackEndTime;
   
   /**
    * Creates a new Web Site Command statistics bean.
    * @param name the command ID
    * @throws NullPointerException if name is null
    * @see CommandStatsEntry#getName()
    */
   public CommandStatsEntry(String name) {
      super();
      _name = name.trim();
   }

   /**
    * Returns the Command ID.
    * @return the command ID
    */
   public String getName() {
      return _name;
   }
   
   /**
    * Returns the number of times the Command was executed.
    * @return the invocation count
    * @see CommandStatsEntry#setCount(int)
    * @see CommandStatsEntry#getSuccessCount()
    */
   public int getCount() {
      return _count;
   }
   
   /**
    * Returns the number of times the Command was <i>successfully</i> executed.
    * @return the invocation count
    * @see CommandStatsEntry#setSuccessCount(int)
    * @see CommandStatsEntry#getCount()
    */
   public int getSuccessCount() {
      return _successCount;
   }
   
   /**
    * Returns the average Command execution time.
    * @return the average execution time, in milliseconds
    * @see CommandStatsEntry#setAvgTime(int)
    * @see CommandStatsEntry#getAvgBackEndTime()
    * @see CommandStatsEntry#getMaxTime()
    * @see CommandStatsEntry#getMaxBackEndTime()
    */
   public int getAvgTime() {
      return _avgTime;
   }
   
   /**
    * Returns the average back-end data source use time.
    * @return the average use time, in milliseconds
    * @see CommandStatsEntry#setAvgBackEndTime(int)
    * @see CommandStatsEntry#getAvgTime()
    * @see CommandStatsEntry#getMaxBackEndTime()
    * @see CommandStatsEntry#getMaxTime()
    */
   public int getAvgBackEndTime() {
      return _avgBackEndTime;
   }
   
   /**
    * Returns the maximum Command execution time.
    * @return the maximum execution time, in millseconds
    * @see CommandStatsEntry#setMaxTime(int)
    * @see CommandStatsEntry#getMaxBackEndTime()
    * @see CommandStatsEntry#getAvgTime()
    * @see CommandStatsEntry#getAvgBackEndTime()
    */
   public int getMaxTime() {
      return _maxTime;
   }
   
   /**
    * Returns the miaxmum back-end data source use time. 
    * @return the maximum use time, in millseconds
    * @see CommandStatsEntry#setMaxBackEndTime(int)
    * @see CommandStatsEntry#getMaxTime()
    * @see CommandStatsEntry#getAvgTime()
    * @see CommandStatsEntry#getAvgBackEndTime()
    */
   public int getMaxBackEndTime() {
      return _maxBackEndTime;
   }
   
   /**
    * Updates the execution count for this Command.
    * @param count the number of times this Command was invoked
    * @throws IllegalArgumentException if count is negative
    * @see CommandStatsEntry#getCount()
    * @see CommandStatsEntry#setSuccessCount(int)
    */
   public void setCount(int count) {
      if (count < 0)
         throw new IllegalArgumentException("Invalid invocation count - " + count);
      
      _count = count;
   }
   
   /**
    * Updates the number of times this Command was <i>successfully</i> executed.
    * @param count the successful invocation count
    * @throws IllegalArgumentException if count is negative
    * @see CommandStatsEntry#getSuccessCount()
    * @see CommandStatsEntry#setCount(int)
    */
   public void setSuccessCount(int count) {
      if (count < 0)
         throw new IllegalArgumentException("Invalid invocation count - " + count);
      
      _successCount = count;
   }
   
   /**
    * Updates the average execution time of this Command.
    * @param time the execution time in milliseconds
    * @throws IllegalArgumentException if time is negative
    * @see CommandStatsEntry#getAvgTime()
    * @see CommandStatsEntry#setAvgBackEndTime(int)
    * @see CommandStatsEntry#setMaxTime(int)
    * @see CommandStatsEntry#setMaxBackEndTime(int)
    */
   public void setAvgTime(int time) {
      if (time < 0)
         throw new IllegalArgumentException("Invalid time - " + time);
      
      _avgTime = time;
   }
   
   /**
    * Updates the average back-end data source usage for this Command.
    * @param time the average usage in milliseconds
    * @throws IllegalArgumentException if time is negative
    * @see CommandStatsEntry#getAvgBackEndTime()
    * @see CommandStatsEntry#setAvgTime(int)
    * @see CommandStatsEntry#setMaxTime(int)
    * @see CommandStatsEntry#setMaxBackEndTime(int)
    */
   public void setAvgBackEndTime(int time) {
      if (time < 0)
         throw new IllegalArgumentException("Invalid time - " + time);
      
      _avgBackEndTime = time;
   }
   
   /**
    * Updates the maximum execution time for this Command.
    * @param time the maximum execution time in millseconds
    * @throws IllegalArgumentException if time is negative
    * @see CommandStatsEntry#getMaxTime()
    * @see CommandStatsEntry#setMaxBackEndTime(int)
    * @see CommandStatsEntry#setAvgTime(int)
    * @see CommandStatsEntry#setAvgBackEndTime(int)
    */
   public void setMaxTime(int time) {
      if (time < 0)
         throw new IllegalArgumentException("Invalid time - " + time);
      
      _maxTime = time;
   }
   
   /**
    * Updates the maximum back-end data source usage time for this Command.
    * @param time the maximum usage time in millseconds
    * @throws IllegalArgumentException if time is negative
    * @see CommandStatsEntry#getMaxBackEndTime()
    * @see CommandStatsEntry#setMaxTime(int)
    * @see CommandStatsEntry#setAvgTime(int)
    * @see CommandStatsEntry#setAvgBackEndTime(int)
    */
   public void setMaxBackEndTime(int time) {
      if (time < 0)
         throw new IllegalArgumentException("Invalid time - " + time);
      
      _maxBackEndTime = time;
   }
   
   /**
    * Compares two statistics beans by comparing their command names.
    */
   public int compareTo(CommandStatsEntry cse2) {
      return _name.compareTo(cse2._name);
   }
}