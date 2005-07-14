// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.mail;

import java.util.*;
import java.io.UnsupportedEncodingException;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.deltava.beans.EMailAddress;

import org.deltava.util.system.SystemData;

/**
 * A class to wrap e-mail message data and send it via the JavaMail API.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class Envelope {
	
	private String _subject;
	private String _msgBody;
	private InternetAddress _msgFrom;
	
	private DataSource _attach;

	/**
	 * Creates a new message envelope with a given message body and subject.
	 * @param msgBody the message text 
	 */
	public Envelope(String msgBody, String subj) {
		super();
		_subject = subj.trim();
		_msgBody = msgBody;
	}
	
	/**
	 * Creates a new message envelope from an existing Message.
	 * @param msg the message (will be formatted by this call)
	 */
	public Envelope(Message msg) {
		this(null, msg.getSubject());
		msg.format();
		_msgBody = msg.getBody();
	}

	/**
	 * Attaches a file to the message.
	 * @param ds a DataSource pointing to the file attachment data.
	 */
	public void setAttachment(DataSource ds) {
		_attach = ds;
	}
	
	/**
	 * Sets the originator of this e-mail message.
	 * @param userAddr the user's address
	 * @param userName the user's name
	 * @see Envelope#setFrom(EMailAddress)
	 */
	public void setFrom(String userAddr, String userName) {
		try {
			_msgFrom = new InternetAddress(userAddr, userName);
		} catch (UnsupportedEncodingException uue) { }
	}
	
	/**
	 * Sets the originator of this e-mail message.
	 * @param p the user's name/address
	 * @see Envelope#setFrom(String, String)
	 */
	public void setFrom(EMailAddress p) {
		setFrom(p.getEmail(), p.getName());
	}
	
	/**
	 * Sends this mesage to a user.
	 * @param addr the user name/address
	 * @throws MessagingException if a JavaMail error occurs
	 * @see Envelope#send(String, String)
	 */
	public void send(EMailAddress addr) throws MessagingException {
		send(addr.getName(), addr.getEmail());
	}

	/**
	 * Sends this mesage to a user.
	 * @param userName the user name
	 * @param userAddr the user's e-mail address
	 * @throws MessagingException if a JavaMail error occurs
	 * @see Envelope#send(EMailAddress)
	 */
	public void send(String userName, String userAddr) throws MessagingException {
		
		// Generate a session to the STMP server
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", SystemData.get("smtp.server"));
		Session s = Session.getInstance(props);
		
		// Create the message
		MimeMessage msg = new MimeMessage(s);
		msg.setFrom(_msgFrom);
		msg.addHeader("Errors-to", SystemData.get("smtp.errors-to"));
		msg.setSubject(_subject);
		try {
			msg.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(userAddr, userName));
		} catch (UnsupportedEncodingException uee) {
			throw new MessagingException(uee.getMessage()); 
		}
		
		// Create the message content
		Multipart mp = new MimeMultipart();
		
		// Add message body
		MimeBodyPart body = new MimeBodyPart();
		body.setText(_msgBody);
		mp.addBodyPart(body);
		
		// If we have an attachment, add it
		if (_attach != null) {
			MimeBodyPart fa = new MimeBodyPart();
			fa.setDataHandler(new DataHandler(_attach));
			fa.setFileName(_attach.getName());
			mp.addBodyPart(fa);
		}
		
		// Set the message content
		msg.setContent(mp);
		
		// Set the sent-date and crank it out
		msg.setSentDate(new Date());
		Transport.send(msg);
	}
}