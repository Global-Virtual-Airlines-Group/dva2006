// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.mail;

import java.util.*;
import java.io.UnsupportedEncodingException;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

import org.deltava.beans.EMailAddress;

import org.deltava.util.system.SystemData;

/**
 * A utility class to send e-mail messages.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Mailer extends Thread {

   private static final Logger log = Logger.getLogger(Mailer.class);

   private EMailAddress _msgFrom;
   private Collection _msgTo = new HashSet();
   private Collection _copyTo = new HashSet();
   
   private MessageContext _ctx;
   private DataSource _attach;

   private static class EMailSender implements EMailAddress {

      private String _name;
      private String _addr;

      EMailSender(String addr, String name) {
         super();
         _name = name;
         _addr = addr;
      }

      public String getEmail() {
         return _addr;
      }

      public String getName() {
         return _name;
      }
      
      public String toString() {
    	  return _name + " (" + _addr + ")";
      }
   }

   /**
    * Initializes the mailer with a source address.
    * @param from the source address
    */
   public Mailer(EMailAddress from) {
      super("SMTP Mailer");
      if (from == null) {
         _msgFrom = new EMailSender(SystemData.get("airline.mail.webmaster"), SystemData.get("airline.name"));
      } else {
         _msgFrom = from;
      }
   }

   /**
    * Utility method to create an e-mail address object.
    * @param addr the recipient address
    * @param name the recipient name
    * @return an EMailAddress object
    */
   public static EMailAddress makeAddress(String addr, String name) {
      return new EMailSender(addr, name);
   }
   
   /**
    * Utility method to create an e-mail address object.
    * @param addr the recipient address
    * @return an EMailAddress object, with the recipient address and name the same
    */
   public static EMailAddress makeAddress(String addr) {
      return makeAddress(addr, addr);
   }
   
   /**
    * Attaches a file to the message.
    * @param ds a DataSource pointing to the file attachment data.
    */
   public void setAttachment(DataSource ds) {
      _attach = ds;
   }

   /**
    * Sets the messaging context used to generate e-mail messages.
    * @param ctx the messaging context
    */
   public void setContext(MessageContext ctx) {
      _ctx = ctx;
   }

   /**
    * Adds an individual address to the recipient list, and sends the message <i>in a new thread</i>. This
    * method will check for a valid e-mail address by comparing the address to {@link EMailAddress#INVALID_ADDR}.
    * @param addr the recipient name/address
    */
   public void send(EMailAddress addr) {
      if ((addr != null) && (!EMailAddress.INVALID_ADDR.equals(addr.getEmail()))) {
         _msgTo.add(addr);
         start();
      }
   }
   
   /**
    * Adds an individual to the CC list of this message.
    * @param addr the recipient name/address
    */
   public void setCC(EMailAddress addr) {
	   try {
	      if (!EMailAddress.INVALID_ADDR.equals(addr.getEmail()))
	         _copyTo.add(new InternetAddress(addr.getEmail(), addr.getName()));
	   } catch (UnsupportedEncodingException uee) {
	   }
   }

   /**
    * Adds a group of addresses to the recipient list, and sends the message <i>in a new thread</i>. This
    * method will check for a valid e-mail address by comparing the address to {@link EMailAddress#INVALID_ADDR}.
    * @param addrs a Collection of recipient names/addresses
    */
   public void send(Collection addrs) {
      for (Iterator i = addrs.iterator(); i.hasNext(); ) {
         EMailAddress addr = (EMailAddress) i.next();
         if (!EMailAddress.INVALID_ADDR.equals(addr.getEmail()))
            _msgTo.add(addr);
      }
         
      // Spawn a new thread
      if (!_msgTo.isEmpty())
         start();
   }

   /**
    * Generates and sends the e-mail message. The recipient object is added to the messaging context under the name
    * &quot;recipient&quot;.
    */
   public void run() {

      // If we're in test mode, send back to the sender only
      boolean isTest = SystemData.getBoolean("smtp.testMode");
      if (isTest) {
         log.warn("STMP Test Mode enabled - sending to " + _msgFrom.getEmail());
         _msgTo.clear();
         _msgTo.add(_msgFrom);
      }

      Session s = null;
      try {
         // Generate a session to the STMP server
         Properties props = System.getProperties();
         props.setProperty("mail.smtp.host", SystemData.get("smtp.server"));
         s = Session.getInstance(props);
         s.setDebug(isTest);
      } catch (Exception e) {
         log.error("Error connecting to STMP server " + e.getMessage(), e);
         return;
      }
      
      // Warn if we have no template
      if (_ctx.getTemplate() == null)
         log.warn("Message Template not loaded");

      // Loop through the recipients
      for (Iterator i = _msgTo.iterator(); i.hasNext();) {
         EMailAddress addr = (EMailAddress) i.next();
         
         // Add the recipient to the messaging context
         _ctx.addData("recipient", addr);

         try {

            // Create the e-mail message
            Message msg = new Message(_ctx);
            msg.format();

            // Create the message
            MimeMessage imsg = new MimeMessage(s);
            imsg.setFrom(new InternetAddress(_msgFrom.getEmail(), _msgFrom.getName()));
            imsg.addHeader("Errors-to", SystemData.get("smtp.errors-to"));
            imsg.setSubject(msg.getSubject() + (isTest ? " (TEST)" : ""));
            try {
               imsg.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(addr.getEmail(), addr
                     .getName()));
            } catch (UnsupportedEncodingException uee) {
               throw new MessagingException(uee.getMessage(), uee);
            }
            
            // Add the copy-to list
            if (!_copyTo.isEmpty()) {
            	Address[] addrs = (Address[]) _copyTo.toArray(new InternetAddress[0]);
            	imsg.addRecipients(javax.mail.Message.RecipientType.CC, addrs);
            }
            
            // Create the message content
            Multipart mp = new MimeMultipart();

            // Add message body
            MimeBodyPart body = new MimeBodyPart();
            body.setText(msg.getBody());
            mp.addBodyPart(body);

            // If we have an attachment, add it
            if (_attach != null) {
               MimeBodyPart fa = new MimeBodyPart();
               fa.setDataHandler(new DataHandler(_attach));
               fa.setFileName(_attach.getName());
               mp.addBodyPart(fa);
            }

            // Set the message content
            imsg.setContent(mp);

            // Set the sent-date and crank it out
            imsg.setSentDate(new Date());
            Transport.send(imsg);
            
            // Log message
            log.info("Sent " + _ctx.getTemplate().getName() + " to " + addr.getName() + " <" + addr.getEmail() + ">");
         } catch (Exception e) {
            log.error("Error sending email to " + addr.getName(), e);
         }
      }
   }
}