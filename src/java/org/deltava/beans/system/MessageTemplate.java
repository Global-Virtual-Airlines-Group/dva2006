// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.ViewEntry;
import org.deltava.util.cache.Cacheable;

/**
 * A class for storing E-Mail message templates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageTemplate implements Comparable, Cacheable, ViewEntry {

    private String _name;
    private String _subject;
    
    private String _desc;
    private String _msgBody;
    private boolean _isHTML;
    
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
     * Sets wether this message should be sent as HTML or plain text.
     * @param html TRUE if the message is HTML, otherwise FALSE
     * @see MessageTemplate#setIsHTML(boolean)
     */
    public void setIsHTML(boolean html) {
    	_isHTML = html;
    }
    
    /**
     * Calculates equality by comparing the template names
     * @param mt2 the message template to compare to
     * @return TRUE if the names match, otherwise FALSE
     * @see MessageTemplate#getName()
     */
    public boolean equals(MessageTemplate mt2) {
        return _name.equals(mt2.getName());
    }
    
    /**
     * Returns the template name's hashcode.
     */
    public int hashCode() {
    	return _name.hashCode();
    }
    
    /**
     * Compares two templates by comparing their names.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
    	MessageTemplate mt2 = (MessageTemplate) o2;
    	return _name.compareTo(mt2.getName());
    }
    
    /**
     * Returns the cache key.
     * @return the template name
     */
    public Object cacheKey() {
       return _name;
    }
    
    /**
     * Returns the CSS class name used when displaying in a view table.
     * @return the CSS class name
     */
    public String getRowClassName() {
    	return _isHTML ? "opt1" : null;
    }
}