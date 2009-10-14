// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import org.deltava.beans.Person;

/**
 * A Water Cooler Message bean with additional populated fields for indexing
 * by Lucene. 
 * @author Luke
 * @version 2.5
 * @since 2.5
 */

@Deprecated
public class IndexableMessage extends Message {
	
	private Person _author;
	private String _channel;
	private String _subject;

	/**
	 * Creates an indexable Message.
	 * @param msg the message to convert 
	 * @param channel the forum channel the thread is in
	 * @param subj the Message Thread subject
	 * @param author the message Author
	 */
	public IndexableMessage(Message msg, String channel, String subj, Person author) {
		super(msg.getAuthorID());
		setID(msg.getID());
		setCreatedOn(msg.getCreatedOn());
		setContentWarning(msg.getContentWarning());
		setThreadID(msg.getThreadID());
		setRemoteAddr(msg.getRemoteAddr());
		setRemoteHost(msg.getRemoteHost());
		setBody(msg.getBody());
		_channel = channel;
		_author = author;
		_subject = subj;
	}

	/**
	 * Returns the message's Author.
	 * @return the Author bean
	 */
	public Person getAuthor() {
		return _author;
	}
	
	/**
	 * Returns the forum Channel the message's parent Thread is in.
	 * @return the channel name
	 */
	public String getChannel() {
		return _channel;
	}
	
	/**
	 * Returns the Message Thread subject.
	 * @return the subject
	 */
	public String getSubject() {
		return _subject;
	}
}