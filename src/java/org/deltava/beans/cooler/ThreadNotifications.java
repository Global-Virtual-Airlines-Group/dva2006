// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.cooler;

import java.util.*;

import org.deltava.beans.Person;
import org.deltava.beans.DatabaseBean;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store notifications for a Water Cooler message thread.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadNotifications extends DatabaseBean implements Cacheable {

   private Set<Integer> _notifications;
   
   /**
    * Creates a new Thread Notifcation bean.
    * @param id the message thread database ID
    */
   public ThreadNotifications(int id) {
      super();
      setID(id);
      _notifications = new HashSet<Integer>();
   }
   
   /**
    * Adds a user to the notification list.
    * @param id the Person's database ID
    * @see ThreadNotifications#addUser(Person)
    * @see ThreadNotifications#removeUser(int)
    */
   public void addUser(int id) {
      _notifications.add(new Integer(id));
   }
   
   /**
    * Adds a user to the notification list.
    * @param p the Person
    * @see ThreadNotifications#addUser(int)
    * @see ThreadNotifications#removeUser(Person)
    */
   public void addUser(Person p) {
      addUser(p.getID());
   }
   
   /**
    * Removes a user from the notification list.
    * @param id the Person's database ID
    * @see ThreadNotifications#removeUser(Person)
    * @see ThreadNotifications#addUser(int)
    */
   public void removeUser(int id) {
      _notifications.remove(new Integer(id));
   }
   
   /**
    * Removes a user from the notification list.
    * @param p the Person
    * @see ThreadNotifications#removeUser(int)
    * @see ThreadNotifications#addUser(Person)
    */
   public void removeUser(Person p) {
      removeUser(p.getID());
   }
   
   /**
    * Returns the database IDs of all users signed up for notifications.
    * @return a Collection of Integers
    */
   public Collection<Integer> getIDs() {
      return _notifications;
   }
   
   /**
    * Returns wether a particular user is signed up to receive notifications.
    * @param id the Person's database ID
    * @return TRUE if the Person is signed up, otherwise FALSE
    */
   public boolean contains(int id) {
      return _notifications.contains(new Integer(id));
   }
   
   /**
    * Returns the cache key for this bean.
    * @return the message thread ID as an Integer
    */
   public Object cacheKey() {
      return new Integer(getID());
   }
}