// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to store saved ACARS text messages.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TextMessage implements java.io.Serializable, Comparable, AuthoredBean, ACARSLogEntry {

   private long _id;
   private long _conID;
   private Date _date;
   private String _msg;
   private int _authorID;
   private int _recipientID;

   /**
    * Creates a new Text Message bean.
    * @param id the message ID
    * @param msg the text message
    * @throws IllegalArgumentException if id is zero or negative
    * @throws NullPointerException if msg is null
    * @see TextMessage#getID()
    * @see TextMessage#getMessage()
    */
   public TextMessage(long id, String msg) {
      super();
      setID(id);
      setMessage(msg);
   }

   /**
    * Returns the ACARS message ID.
    * @return the message ID
    * @see TextMessage#setID(long)
    * @see TextMessage#getConnectionID()
    */
   public long getID() {
      return _id;
   }

   /**
    * Returns the ACARS connection ID.
    * @return the connection ID
    * @see TextMessage#setConnectionID(long)
    * @see TextMessage#getID()
    */
   public long getConnectionID() {
      return _conID;
   }

   /**
    * Returns the date the message was sent.
    * @return the date/time of the message
    * @see TextMessage#setDate(Date)
    */
   public Date getDate() {
      return _date;
   }
   
   public Date getStartTime() {
      return getDate();
   }

   /**
    * Returns the database ID of the message author.
    * @return the database ID
    * @see TextMessage#setAuthorID(int)
    * @see TextMessage#getRecipientID()
    * @see org.deltava.beans.Person#getID()
    */
   public int getAuthorID() {
      return _authorID;
   }
   
   public int getPilotID() {
      return getAuthorID();
   }

   /**
    * Returns the database ID of the message recipient.
    * @return the database ID, or zero if a public message
    * @see TextMessage#setRecipientID(int)
    * @see TextMessage#getAuthorID()
    * @see org.deltava.beans.Person#getID()
    */
   public int getRecipientID() {
      return _recipientID;
   }

   /**
    * Returns the message text.
    * @return the message text
    * @see TextMessage#setMessage(String)
    */
   public String getMessage() {
      return _msg;
   }

   /**
    * Updates the database ID of this message.
    * @param id the database ID
    * @throws IllegalArgumentException if id is zero or negative
    * @see TextMessage#getID()
    * @see TextMessage#setConnectionID(long)
    */
   public void setID(long id) {
      if (id < 1)
         throw new IllegalArgumentException("Invalid Message ID - " + id);

      _id = id;
   }

   /**
    * Updates the ACARS connection ID of this message.
    * @param id the connection ID
    * @throws IllegalArgumentException if id is zero or negative
    * @see TextMessage#getConnectionID()
    * @see TextMessage#setID(long)
    */
   public void setConnectionID(long id) {
      if (id < 0)
         throw new IllegalArgumentException("Invalid Connection ID - " + id);

      _conID = id;
   }

   /**
    * Updates the date of the message.
    * @param dt the date/time the message was sent
    * @see TextMessage#getDate()
    */
   public void setDate(Date dt) {
      _date = dt;
   }

   /**
    * Updates the Author of the message.
    * @param id the Author's database ID
    * @throws IllegalArgumentException if id is zero or negative
    * @see TextMessage#getAuthorID()
    * @see TextMessage#setRecipientID(int)
    */
   public void setAuthorID(int id) {
      DatabaseBean.validateID(_authorID, id);
      _authorID = id;
   }

   /**
    * Updates the Recipient of the message.
    * @param id the Recipient's database ID, or zero for a public message
    * @throws IllegalArgumentException if id is negative
    * @see TextMessage#getRecipientID()
    * @see TextMessage#setAuthorID(int)
    */
   public void setRecipientID(int id) {
      if (id > 0)
         DatabaseBean.validateID(_recipientID, id);
      
      _recipientID = id;
   }

   /**
    * Updates the message text.
    * @param msg the message text
    * @throws NullPointerException if msg is null
    * @see TextMessage#getMessage()
    */
   public void setMessage(String msg) {
      _msg = msg.trim();
   }
   
   /**
    * Compares two messages by comparing their dates.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      TextMessage msg2 = (TextMessage) o2;
      return _date.compareTo(msg2.getDate());
   }
}