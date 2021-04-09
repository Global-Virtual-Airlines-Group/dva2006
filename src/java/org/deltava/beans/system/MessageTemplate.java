// Copyright 2005, 2006, 2009, 2010, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.cache.Cacheable;

/**
 * A class for storing E-Mail message templates.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class MessageTemplate implements Comparable<MessageTemplate>, Auditable, Cacheable, ViewEntry {

    private final String _name;
    private String _subject;
    private String _desc;
    private String _msgBody;
    private boolean _isHTML;
    
    private final Collection<NotifyActionType> _actionTypes = new LinkedHashSet<NotifyActionType>();
    private String _notifyCtxObj;
    private int _notifyTTL;
    
    /**
     * Creates a new Message Template with a given name and subject.
     * @param name the name of the template
     * @throws NullPointerException if either name or subject are null
     * @see MessageTemplate#getName()
     */
    public MessageTemplate(String name) {
        super();
        _name = name.trim().toUpperCase();
    }

    /**
     * Returns the name of the template.
     * @return the template name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the subject of the generated e-mail message.
     * @return the e-mail message subject
     * @see MessageTemplate#setSubject(String)
     */
    public String getSubject() {
        return _subject;
    }
    
    /**
     * Returns a user-readable description of this Message template.
     * @return the template description
     * @see MessageTemplate#setDescription(String)
     */
    public String getDescription() {
        return _desc;
    }
    
    /**
     * Returns the body of the message template.
     * @return the message template body. This can contain macros to be parsed by MessageContext.
     * @see MessageTemplate#setBody(String)
     */
    public String getBody() {
        return _msgBody;
    }
    
    /**
     * Returns if the message should be sent as HTML or plain text.
     * @return TRUE if the message is formatted as HTML, otherwise FALSE
     * @see MessageTemplate#setIsHTML(boolean)
     */
    public boolean getIsHTML() {
    	return _isHTML;
    }
    
    /**
     * Returns the action types to embed in the push notification.
     * @return a Collection of NotifyActionType enums
     */
    public Collection<NotifyActionType> getActionTypes() {
    	return _actionTypes;
    }
    
    /**
     * Returns the ID of the push notification context object.
     * @return the context object ID or null if none
     */
    public String getNotifyContext() {
    	return _notifyCtxObj;
    }
    
    /**
     * Returns the TTL of the push notification sent.
     * @return the TTL in seconds
     */
    public int getNotificationTTL() {
    	return _notifyTTL;
    }
    
    @Override
    public boolean isCrossApp() {
    	return false;
    }

    /**
     * Updates the description of this message template.
     * @param desc the new template description
     * @throws NullPointerException if desc is null
     * @see MessageTemplate#getDescription()
     */
    public void setDescription(String desc) {
        _desc = desc.trim();
    }

    /**
     * Updates the body of this message template.
     * @param body the new template body
     * @see MessageTemplate#getBody()
     */
    public void setBody(String body) {
        _msgBody = body;
    }
    
    /**
     * Updates the e-mail message subject.
     * @param subj the subject
     * @throws NullPointerException if subj is null
     * @see MessageTemplate#getSubject()
     */
    public void setSubject(String subj) {
    	_subject = subj.trim();
    }
    
    /**
     * Sets whether this message should be sent as HTML or plain text.
     * @param html TRUE if the message is HTML, otherwise FALSE
     * @see MessageTemplate#setIsHTML(boolean)
     */
    public void setIsHTML(boolean html) {
    	_isHTML = html;
    }

    /**
     * Updates the actiont types appended to the push notification.
     * @param types a Collection of NotifyActionType enums
     */
    public void setActionTypes(Collection<NotifyActionType> types) {
    	_actionTypes.clear();
    	_actionTypes.addAll(types);
    }
    
    /**
     * Updates the TTL of the push notification.
     * @param ttl the ttl in seconds
     */
    public void setNotificationTTL(int ttl) {
    	_notifyTTL = Math.max(5, ttl);
    }
    
    /**
     * Updates the ID of the push notification context object.
     * @param id the ID
     */
    public void setNotifyContext(String id) {
    	_notifyCtxObj = id;
    }
    
    @Override
    public boolean equals(Object o) {
    	return (o instanceof MessageTemplate) && (compareTo((MessageTemplate) o) == 0);
    }
    
    @Override
    public int hashCode() {
    	return _name.hashCode();
    }
    
    @Override
    public int compareTo(MessageTemplate mt2) {
    	return _name.compareTo(mt2._name);
    }
    
    @Override
    public Object cacheKey() {
       return _name;
    }
    
    @Override
    public String getAuditID() {
    	return _name;
    }
    
    @Override
    public String getRowClassName() {
    	return _isHTML ? "opt1" : null;
    }
}