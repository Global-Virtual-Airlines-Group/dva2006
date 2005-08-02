// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.mail;

import org.deltava.util.system.SystemData;

/**
 * An e-mail message formatter.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class Message {

   private String _msgBody;
   private MessageContext _ctx;

   /**
    * Creates a new message using a particular Message Context.
    * @param ctx the message context
    */
   public Message(MessageContext ctx) {
      super();
      _ctx = ctx;
   }

   /**
    * Returns the message subject.
    * @return the subject prepended by the Airline Name
    */
   public String getSubject() {
      StringBuffer buf = new StringBuffer(SystemData.get("airline.name"));
      buf.append(' ');
      buf.append(_ctx.getTemplate().getSubject());
      return buf.toString();
   }

   /**
    * Returns the formatted message body text.
    * @return the body text, or null if {@link Message#format()}has not been called yet
    */
   public String getBody() {
      return _msgBody;
   }

   /**
    * Formats the message by replacing arguments in the message template with values from the message context.
    * @throws IllegalStateException if no message context or template exists
    */
   public void format() {

      // Check that the message context has been set
      if ((_ctx == null) || (_ctx.getTemplate() == null))
         throw new IllegalStateException("Message Context not set");

      // Load the Message template
      StringBuffer buf = new StringBuffer(_ctx.getTemplate().getBody());

      // Parse the message template with data from the MessageContext
      int spos = buf.indexOf("${");
      while (spos != -1) {
         int epos = buf.indexOf("}", spos);

         // Only format if the end token can be found
         if (epos > spos) {
            String token = buf.substring(spos + 2, epos);
            buf.replace(spos, epos + 1, _ctx.execute(token));
            spos = buf.indexOf("${");
         } else {
            spos = buf.indexOf("${", spos);
         }
      }

      // Save the message body
      _msgBody = buf.toString();
   }
}