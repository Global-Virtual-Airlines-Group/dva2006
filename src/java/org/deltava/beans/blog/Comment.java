// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.blog;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to store blog entry comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Comment extends DatabaseBean implements CalendarEntry {
	
	private Date _date;
	private String _name;
	private String _eMail;
	private String _body;
	
	private String _remoteAddr;
	private String _remoteHost;

	/**
	 * Creates a new blog entry comment.
	 * @param name the author name
	 * @param body the comment body
	 * @throws NullPointerException if name is null
	 * @see Comment#setName(String)
	 */
	public Comment(String name, String body) {
		super();
		setName(name);
		_body = body;
	}
	
	/**
	 * Returns the author name.
	 * @return the author name
	 * @see Comment#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the Comment text.
	 * @return the comment text
	 */
	public String getBody() {
		return _body;
	}
	
	/**
	 * Returns the e-mail address of the author.
	 * @return the author's email address, or null
	 * @see Comment#setEmail(String)
	 */
	public String getEmail() {
		return _eMail;
	}
	
	/**
	 * Returns the Comment creation date.
	 * @return the date/time the comment was created
	 * @see Comment#setDate(Date)
	 */
	public Date getDate() {
		return _date;
	}
	
	/**
	 * Returns the remote IP address of the Comment poster.
	 * @return the IP address
	 * @see Comment#getRemoteHost()
	 * @see Comment#setRemoteAddr(String)
	 */
	public String getRemoteAddr() {
		return _remoteAddr;
	}
	
	/**
	 * Returns the remote host name of the Comment poster.
	 * @return the host name
	 * @see Comment#getRemoteAddr()
	 * @see Comment#setRemoteHost(String)
	 */
	public String getRemoteHost() {
		return _remoteHost;
	}
	
	/**
	 * Updates the author name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see Comment#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the author's e-mail address.
	 * @param addr the e-mail address
	 * @see Comment#getEmail()
	 */
	public void setEmail(String addr) {
		_eMail = addr;
	}
	
	/**
	 * Updates the comment creation date.
	 * @param dt the date/time the comment was created
	 * @see Comment#getDate()
	 */
	public void setDate(Date dt) {
		_date = dt;
	}
	
	/**
	 * Updates the remote IP address of the comment poster.
	 * @param addr the IP address
	 * @see Comment#setRemoteHost(String)
	 * @see Comment#getRemoteAddr()
	 */
	public void setRemoteAddr(String addr) {
		_remoteAddr = addr;
	}
	
	/**
	 * Updates the remote host name of the comment poster.
	 * @param host the host name
	 * @see Comment#setRemoteAddr(String)
	 * @see Comment#getRemoteHost()
	 */
	public void setRemoteHost(String host) {
		_remoteHost = host;
	}

	/**
	 * Compares two Comments by comparing their creation date.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Comment c2 = (Comment) o;
		return _date.compareTo(c2._date);
	}
}