// Copyright 2005, 2006, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.help;

import org.deltava.util.cache.Cacheable;

/**
 * A class for storing Online Help entries.
 * @author Luke
 * @version 3.2
 * @since 1.0
 */

public class OnlineHelpEntry implements java.io.Serializable, Cacheable, Comparable<OnlineHelpEntry> {

    private String _title;
    private String _subject;
    private String _body;
    
    /**
     * Creates a new Help Entry with a given title and body
     * @param title The title of this Help Entry
     * @param body The body content of this Help Entry
     * @throws NullPointerException if the title is null
     */
    public OnlineHelpEntry(String title, String body) {
        super();
        _title = title.trim().toUpperCase();
        _body = body;
    }
    
    /**
     * Returns this entry's Title.
     * @return the title of this Help Entry
     */
    public String getTitle() {
        return _title;
    }
    
    /**
     * Returns this entry's Subject.
     * @return the subject of this Help Entry
     */
    public String getSubject() {
    	return _subject;
    }

    /**
     * Returns this entry's Body.
     * @return the body content of this Help Entry
     */
    public String getBody() {
        return _body;
    }
    
    /**
     * Updates this entry's subject.
     * @param subj the subject
     * @throws NullPointerException if subj is null
     */
    public void setSubject(String subj) {
    	_subject = subj.trim(); 
    }
    
    public Object cacheKey() {
    	return _title;
    }
    
    /**
     * Compare entries by doing a comparison on their title.
     */
    public int compareTo(OnlineHelpEntry he2) {
        return _title.compareTo(he2._title);
    }
    
    public int hashCode() {
    	return _title.hashCode();
    }

    /**
     * Checks for equality using the title.
     */
    public boolean equals(Object o2) {
       return (o2 instanceof OnlineHelpEntry) ? (compareTo((OnlineHelpEntry) o2) == 0) : false;
    }
}