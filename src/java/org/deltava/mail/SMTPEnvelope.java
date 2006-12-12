// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
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

class SMTPEnvelope implements java.io.Serializable, Cloneable, Comparable {

	private EMailAddress _msgFrom;
	private final Collection<Address> _msgTo = new LinkedHashSet<Address>();
	private final Collection<Address> _copyTo = new LinkedHashSet<Address>();
	
	private Date _createdOn = new Date();

	private String _subject;
	private String _body;
	private String _contentType;
	private DataSource _attach;

	/**
	 * Creates a new STMP envelope.
	 * @param from the originating address
	 * @see SMTPEnvelope#getFrom()
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
	 * @see SMTPEnvelope#setBody(String)
	 */
	public String getBody() {
		return _body;
	}
	
	/**
	 * Returns the creation date.
	 * @return the date/time the envelope was created
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}

	/**
	 * Returns the message subject.
	 * @return the subject
	 * @see SMTPEnvelope#setSubject(String)
	 */
	public String getSubject() {
		return _subject;
	}

	/**
	 * Returns the message content type.
	 * @return the MIME type
	 * @see SMTPEnvelope#setContentType(String)
	 */
	public String getContentType() {
		return _contentType;
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
	 * @see SMTPEnvelope#addCopyTo(EMailAddress)
	 * @see SMTPEnvelope#hasRecipients()
	 */
	public Address[] getCopyTo() {
		return _copyTo.isEmpty() ? null : (Address[]) _copyTo.toArray(new InternetAddress[0]);
	}

	/**
	 * Returns the recipient list.
	 * @return an array of Address beans
	 * @see SMTPEnvelope#addRecipient(EMailAddress)
	 * @see SMTPEnvelope#addRecipients(Collection)
	 * @see SMTPEnvelope#setRecipient(EMailAddress)
	 * @see SMTPEnvelope#hasRecipients()
	 */
	public Address[] getRecipients() {
		return _msgTo.toArray(new InternetAddress[0]);
	}

	/**
	 * Returns if the envelope has any recipients.
	 * @return TRUE if receipients or copyTo are not empty, otherwise FALSE
	 * @see SMTPEnvelope#getRecipients()
	 * @see SMTPEnvelope#getCopyTo()
	 */
	public boolean hasRecipients() {
		return !(_msgTo.isEmpty() && _copyTo.isEmpty());
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
	 * @see SMTPEnvelope#getBody()
	 */
	public void setBody(String body) {
		_body = body;
	}

	/**
	 * Sets the message subject.
	 * @param subj the subject
	 * @see SMTPEnvelope#getSubject()
	 */
	public void setSubject(String subj) {
		_subject = subj;
	}

	/**
	 * Sets the message content type.
	 * @param cType the MIME type
	 * @see SMTPEnvelope#getContentType()
	 */
	public void setContentType(String cType) {
		_contentType = cType;
	}

	/**
	 * Adds an address to the recipient list.
	 * @param addr the e-mail address
	 * @see SMTPEnvelope#addRecipients(Collection)
	 * @see SMTPEnvelope#setRecipient(EMailAddress)
	 * @see SMTPEnvelope#getRecipients()
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
	 * @see SMTPEnvelope#addRecipient(EMailAddress)
	 * @see SMTPEnvelope#addRecipients(Collection)
	 * @see SMTPEnvelope#getRecipients()
	 */
	public void setRecipient(EMailAddress addr) {
		_msgTo.clear();
		addRecipient(addr);
	}

	/**
	 * Adds a Collection of addresses to the recipient list.
	 * @param addrs a Collection of EMailAddress beans
	 * @see SMTPEnvelope#addRecipient(EMailAddress)
	 * @see SMTPEnvelope#setRecipient(EMailAddress)
	 * @see SMTPEnvelope#getRecipients()
	 */
	public void addRecipients(Collection<EMailAddress> addrs) {
		for (Iterator<EMailAddress> i = addrs.iterator(); i.hasNext();)
			addRecipient(i.next());
	}

	/**
	 * Adds an address to the copy-to list.
	 * @param addr the e-mail address
	 * @see SMTPEnvelope#getCopyTo()
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
		result._createdOn = new Date(_createdOn.getTime());
		result.setContentType(_contentType);
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
		Address addr = _msgTo.iterator().next();
		return addr.toString();
	}
	
	/**
	 * Compares two envelopes by comparing their creation date/times.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		SMTPEnvelope e2 = (SMTPEnvelope) o;
		return _createdOn.compareTo(e2._createdOn);
	}
}