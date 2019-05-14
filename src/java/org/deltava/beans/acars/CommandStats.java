// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store ACARS Server Command statistics.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class CommandStats implements java.io.Serializable, Comparable<CommandStats>, ViewEntry {

   private final String _name;
   private int _count;
   private int _successCount;
   
   private long _totalTime;
   private long _totalBackEndTime;
   private long _maxTime;
   private long _maxBackEndTime;
   
   /**
    * Creates a new Web Site Command statistics bean.
    * @param name the command ID
    * @throws NullPointerException if name is null
    * @see CommandStats#getName()
    */
   public CommandStats(String name) {
      super();
      _name = name;
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
    * @see CommandStats#getSuccessCount()
    */
   public int getCount() {
      return _count;
   }
   
   /**
    * Returns the number of times the Command was <i>successfully</i> executed.
    * @return the invocation count
    * @see CommandStats#getCount()
    */
   public int getSuccessCount() {
      return _successCount;
   }
   
   /**
    * Returns the average Command execution time.
    * @return the average execution time, in milliseconds
    * @see CommandStats#getAvgBackEndTime()
    * @see CommandStats#getMaxTime()
    * @see CommandStats#getMaxBackEndTime()
    */
   public int getAvgTime() {
      return (_successCount == 0) ? 0 : (int) _totalTime / _successCount;
   }
   
   /**
    * Returns the average back-end data source use time.
    * @return the average use time, in milliseconds
    * @see CommandStats#getAvgTime()
    * @see CommandStats#getMaxBackEndTime()
    * @see CommandStats#getMaxTime()
    */
   public int getAvgBackEndTime() {
      return (_successCount == 0) ? 0 : (int) _totalBackEndTime / _successCount;
   }
   
   /**
    * Returns the maximum Command execution time.
    * @return the maximum execution time, in millseconds
    * @see CommandStats#getMaxBackEndTime()
    * @see CommandStats#getAvgTime()
    * @see CommandStats#getAvgBackEndTime()
    */
   public long getMaxTime() {
      return _maxTime;
   }
   
   /**
    * Returns the miaxmum back-end data source use time. 
    * @return the maximum use time, in millseconds
    * @see CommandStats#getMaxTime()
    * @see CommandStats#getAvgTime()
    * @see CommandStats#getAvgBackEndTime()
    */
   public long getMaxBackEndTime() {
      return _maxBackEndTime;
   }
   
   /**
    * Increments the execution count for this Command.
    * @see CommandStats#getCount()
    */
   public synchronized void increment() {
      _count++;
   }
   
   /**
    * Incrementss the number of times this Command was <i>successfully</i> executed.
    * @param totalTime the total execution time in milliseconds
    * @param backEndTime the total back-end execution time in milliseconds
    * @see CommandStats#getSuccessCount()
    */
   public synchronized void success(long totalTime, long backEndTime) {
      _successCount++;
      _totalTime += totalTime;
      _totalBackEndTime += backEndTime;
      _maxTime = Math.max(totalTime, _maxTime);
      _maxBackEndTime = Math.max(backEndTime, _maxBackEndTime);
   }
   
   @Override
   public int compareTo(CommandStats cse2) {
      int tmpResult = Integer.compare(_count, cse2._count);
      return (tmpResult == 0) ? _name.compareTo(cse2._name) : tmpResult;
   }

   @Override
   public String getRowClassName() {
	   return (_count == _successCount) ? null : "warn";
   }
}