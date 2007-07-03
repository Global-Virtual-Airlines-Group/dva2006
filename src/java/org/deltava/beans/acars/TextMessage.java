// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to store saved ACARS text messages.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TextMessage implements AuthoredBean, ACARSLogEntry {

   private Date _date;
   private String _msg;
   private int _authorID;
   private int _recipientID;

   /**
    * Creates a new Text Message bean.
    * @throws IllegalArgumentException if id is zero or negative
    * @throws NullPointerException if msg is null
    * @see TextMessage#getDate()
    */
   public TextMessage(Date dt) {
      super();
      _date = dt;
   }

   /**
    * Returns the date the message was sent.
    * @return the date/time of the message
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
    */
   public int compareTo(ACARSLogEntry e2) {
      return _date.compareTo(e2.getStartTime());
   }
}