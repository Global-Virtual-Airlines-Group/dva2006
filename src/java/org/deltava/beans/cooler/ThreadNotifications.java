// Copyright 2005, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean to store notifications for a Water Cooler message thread.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class ThreadNotifications extends DatabaseBean {

   private final Collection<Integer> _notifications = new HashSet<Integer>();
   
   /**
    * Creates a new Thread Notifcation bean.
    * @param id the message thread database ID
    */
   public ThreadNotifications(int id) {
      super();
      setID(id);
   }
   
   /**
    * Adds a user to the notification list.
    * @param id the Person's database ID
    * @see ThreadNotifications#addUser(Person)
    * @see ThreadNotifications#removeUser(int)
    */
   public void addUser(int id) {
      _notifications.add(Integer.valueOf(id));
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
      _notifications.remove(Integer.valueOf(id));
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
    * Returns whether a particular user is signed up to receive notifications.
    * @param id the Person's database ID
    * @return TRUE if the Person is signed up, otherwise FALSE
    */
   public boolean contains(int id) {
      return _notifications.contains(Integer.valueOf(id));
   }
}