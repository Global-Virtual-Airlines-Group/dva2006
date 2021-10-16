// Copyright 2005, 2006, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.time.Instant;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A class to store Water Cooler posts.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class Message extends DatabaseBean implements AuthoredBean, RemoteAddressBean {
    
    private int _threadID;
    private int _authorID;
    private Instant _createdOn;
    private String _msgBody;
    private boolean _contentWarning;
    
    private String _remoteHost;
    private String _remoteAddr;
    
    /**
     * Create a new message object from a particular author.
     * @param authorID the database ID for the author
     * @throws IllegalArgumentException if authorID is zero or negative
     * @see Message#getAuthorID()
     */
    public Message(int authorID) {
        super();
        setAuthorID(authorID);
        _createdOn = Instant.now();
    }
    
    /**
     * Get the message thread ID. <i>This is typically called by a DAO</i>
     * @return the entry corresponding to the primary key in the <i>COOLER_THREADS</i> table in the database.
     * @see Message#getThreadID()
     */
    public int getThreadID() {
        return _threadID;
    }

    /**
     * Returns the date and time this post was written.
     * @return the date/time this post was written
     * @see Message#setCreatedOn(Instant)
     */
    public Instant getCreatedOn() {
        return _createdOn;
    }
    
    /**
     * Returns the author of this message.
     * @return the database ID of the Pilot object for this message's author.
     */
    @Override
    public int getAuthorID() {
        return _authorID;
    }
    
    /**
     * Returns the message body.
     * @return the message body
     * @see Message#setBody(String)
     */
    public String getBody() {
        return _msgBody;
    }
    
    @Override
    public String getRemoteAddr() {
        return _remoteAddr;
    }
    
    @Override
    public String getRemoteHost() {
        return _remoteHost;
    }
    
    /**
     * Returns whether this message contains questionable content.
     * @return TRUE if there is questionable content, otherwise FALSE
     * @see Message#setContentWarning(boolean)
     */
    public boolean getContentWarning() {
    	return _contentWarning;
    }
    
    /**
     * Updates the date/time the message was written.
     * @param dt the date & time this message was created
     * @see Message#getCreatedOn()
     */
    public void setCreatedOn(Instant dt) {
        _createdOn = dt;
    }
    
    /**
     * Updates this message's content.
     * @param body the new content of the message
     */
    public void setBody(String body) {
        _msgBody = StringUtils.isEmpty(body) ? null : body;
    }
  
    /**
     * Updates the thread ID of this message. <i>This is typically called by a DAO</i>
     * @param id the entry corresponding to the primary key in the <i>COOLER_THREADS</i> table in the database.
     * @throws IllegalArgumentException if id is zero or negative
     * @see Message#getThreadID()
     * @see DatabaseBean#validateID(int, int)
     */
    public void setThreadID(int id) {
        validateID(_threadID, id);
        _threadID = id;
    }
    
    /**
     * Updates the IP address from where this message was posted.
     * @param addr the IP address
     * @see Message#getRemoteAddr()
     * @see Message#setRemoteHost(String)
     */
    public void setRemoteAddr(String addr) {
        _remoteAddr = addr;
    }
    
    /**
     * Updates the host name from where this message was posted.
     * @param hostName the host name
     * @see Message#getRemoteHost()
     * @see Message#setRemoteAddr(String)
     */
    public void setRemoteHost(String hostName) {
        _remoteHost = hostName;
    }
    
    /**
     * Toggles the content warning flag for this message.
     * @param isWarn TRUE if the message contains questionable content, otherwise FALSE
     * @see Message#getContentWarning()
     */
    public void setContentWarning(boolean isWarn) {
    	_contentWarning = isWarn;
    }
    
    /**
     * Updates the author of this Message.
     * @param id the author's database ID
     * @throws IllegalArgumentException if id is zero or negative
     * @see Message#getAuthorID()
     */
    @Override
    public void setAuthorID(int id) {
    	validateID(_authorID, id);
    	_authorID = id;
    }
    
    /**
     * Implements default sort ordering by comparing the creation dates.
     * @throws ClassCastException if o2 isn't a Message
     * @see Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Object o2) {
        Message m2 = (Message) o2;
        return _createdOn.compareTo(m2.getCreatedOn());
    }
}