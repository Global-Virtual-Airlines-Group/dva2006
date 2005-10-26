// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.mail;

import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.deltava.beans.EMailAddress;

/**
 * A bean to aggregate SMTP message information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class SMTPEnvelope implements java.io.Serializable, Cloneable {
   
   private EMailAddress _msgFrom;
   private Collection _msgTo = new LinkedHashSet();
   private Collection _copyTo = new LinkedHashSet();
   
   private String _subject;
   private String _body;
   private DataSource _attach;
   
   /**
    * Creates a new STMP envelope.
    * @param from the originating address
    */
   SMTPEnvelope(EMailAddress from) {
      super();
      _msgFrom = from;
   }

   /**
    * Returns the envelope's attachment, if any.
    * @return a Java Activation data source, or null
    * @see SMTPEnvelope#setAttachment(DataSource)
    */
   public DataSource getAttachment() {
      return _attach;
   }
   
   /**
    * Returns the message body.
    * @return the message body
    */
   public String getBody() {
      return _body;
   }
   
   /**
    * Returns the message subject.
    * @return the subject
    */
   public String getSubject() {
      return _subject;
   }
   
   /**
    * Returns the originator of this e-mail message.
    * @return the originating address
    */
   public EMailAddress getFrom() {
      return _msgFrom;
   }
   
   /**
    * Returns the copy-to recipient list.
    * @return an array of Address beans, or null if empty
    */
   public Address[] getCopyTo() {
      return _copyTo.isEmpty() ? null : (Address[]) _copyTo.toArray(new InternetAddress[0]);
   }
   
   /**
    * Returns the recipient list.
    * @return an array of Address beans 
    */
   public Address[] getRecipients() {
      return (Address[]) _msgTo.toArray(new InternetAddress[0]);
   }
   
   /**
    * Adds an attachment to the envelope.
    * @param ds a Java Activation data source
    * @see SMTPEnvelope#getAttachment()
    */
   public void setAttachment(DataSource ds) {
      _attach = ds;
   }
   
   /**
    * Sets the message body.
    * @param body the body text
    */
   public void setBody(String body) {
      _body = body;
   }
   
   /**
    * Sets the message subject.
    * @param subj the subject
    */
   public void setSubject(String subj) {
      _subject = subj;
   }
   
   /**
    * Adds an address to the recipient list.
    * @param addr the e-mail address
    */
   public void addRecipient(EMailAddress addr) {
      if ((addr != null) && (!EMailAddress.INVALID_ADDR.equals(addr.getEmail()))) {
         try {
            _msgTo.add(new InternetAddress(addr.getEmail(), addr.getName()));
         } catch (UnsupportedEncodingException uee) {
         }
      }
   }
   
   /**
    * Clears the recipient list and overwrites it with a single address.
    * @param addr the e-mail address
    */
   public void setRecipient(EMailAddress addr) {
      _msgTo.clear();
      addRecipient(addr);
   }
   
   /**
    * Adds a Collection of addresses to the recipient list.
    * @param addrs a Collection of EMailAddress beans
    */
   public void addRecipients(Collection addrs) {
      for (Iterator i = addrs.iterator(); i.hasNext(); )
         addRecipient((EMailAddress) i.next());
   }
   
   /**
    * Adds an address to the copy-to list
    * @param addr the e-mail address
    */
   public void addCopyTo(EMailAddress addr) {
      if ((addr != null) && (!EMailAddress.INVALID_ADDR.equals(addr.getEmail()))) {
         try {
            _copyTo.add(new InternetAddress(addr.getEmail(), addr.getName()));
         } catch (UnsupportedEncodingException uee) {
         }
      }
   }
   
   /**
    * Clones this SMTP envelope.
    * @return a copy of the envelope
    * @see Cloneable
    */
   public Object clone() {
	   SMTPEnvelope result = new SMTPEnvelope(_msgFrom);
	   result._msgTo.addAll(_msgTo);
	   result._copyTo.addAll(_copyTo);
	   result.setAttachment(_attach);
	   result.setSubject(_subject);
	   result.setBody(_body);
	   return result;
   }
   
   /**
    * Returns a string representation of the envelope.
    * @return the recipient name/address
    */
   public String toString() {
      if (_msgTo.isEmpty())
         return "UNKNOWN";
      
      // Get the first recipient
      Address addr = (Address) _msgTo.iterator().next();
      return addr.toString();
   }
}