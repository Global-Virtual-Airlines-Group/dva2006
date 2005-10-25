// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.mail;

import java.util.*;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * A daemon thread to send e-mail messages in the background. SMTP messages are not designed for critical information;
 * they are designed to fail silently on an error.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MailerDaemon extends Thread {

   private static final Logger log = Logger.getLogger(MailerDaemon.class);

   private Set _queue = new LinkedHashSet();

   /**
    * Creates a new Mailer daemon thread.
    */
   public MailerDaemon() {
      super("Mailer Daemon");
      setDaemon(true);
   }

   /**
    * Queues an SMTP message for mailing by the daemon.
    * @param env the SMTP envelope
    */
   public void push(SMTPEnvelope env) {
      synchronized (_queue) {
         _queue.add(env);
      }
      
      log.info("Queued message for " + env.getRecipients()[0]);
   }

   private void send(Session s, SMTPEnvelope env) {

      // Create the message
      MimeMessage imsg = new MimeMessage(s);
      try {
         imsg.setFrom(new InternetAddress(env.getFrom().getEmail(), env.getFrom().getName()));
         imsg.addHeader("Errors-to", SystemData.get("smtp.errors-to"));
         imsg.setSubject(env.getSubject());
         imsg.setRecipients(javax.mail.Message.RecipientType.TO, env.getRecipients());
         if (env.getCopyTo() != null)
            imsg.addRecipients(javax.mail.Message.RecipientType.CC, env.getCopyTo());
      } catch (Exception e) {
         log.error("Error setting message headers - " + e.getMessage(), e);
         return;
      }

      // Set the message content
      try {
         Multipart mp = new MimeMultipart();

         // Add message body
         MimeBodyPart body = new MimeBodyPart();
         body.setText(env.getBody());
         mp.addBodyPart(body);

         // If we have an attachment, add it
         if (env.getAttachment() != null) {
            MimeBodyPart fa = new MimeBodyPart();
            fa.setDataHandler(new DataHandler(env.getAttachment()));
            fa.setFileName(env.getAttachment().getName());
            mp.addBodyPart(fa);
         }

         imsg.setContent(mp);
         imsg.setSentDate(new Date());
      } catch (MessagingException me) {
         log.error("Error setting message content - " + me.getMessage(), me);
      }

      // Send the message
      try {
         Transport.send(imsg);
         log.info("Sent message to " + env.getFrom().getName() + " <" + env.getFrom().getEmail() + ">");
      } catch (Exception e) {
         log.error("Error sending email to " + env.getFrom().getName(), e);
      }

   }

   /**
    * Executes the Thread.
    */
   public void run() {
      log.info("Starting");
      ThreadUtils.sleep(5000);
      int sleepInterval = SystemData.getInt("smtp.daemon.sleep") * 1000;
      if (sleepInterval == 0)
         sleepInterval = 60000;

      while (!isInterrupted()) {
         log.debug("Checking Queue");

         // Check if the queue has any information
         synchronized (_queue) {
            if (!_queue.isEmpty()) {
               log.info("Processing Queue - " + _queue.size() + " entries");
               
               // Generate a session to the STMP server
               try {
                  Properties props = System.getProperties();
                  props.setProperty("mail.smtp.host", SystemData.get("smtp.server"));
                  Session s = Session.getInstance(props);
                  s.setDebug(SystemData.getBoolean("smtp.testMode"));
                  
                  // Send the messages
                  for (Iterator i = _queue.iterator(); i.hasNext();) {
                     SMTPEnvelope env = (SMTPEnvelope) i.next();
                     send(s, env);
                     i.remove();
                  }
               } catch (Exception e) {
                  log.error("Error connecting to STMP server " + e.getMessage());
               }
            }
         }

         // Sleep for a while
         try {
            Thread.sleep(sleepInterval);
         } catch (InterruptedException ie) {
            log.debug("Interrupted while sleeping");
            interrupt();
         }
      }

      log.info("Stopping");
   }
}