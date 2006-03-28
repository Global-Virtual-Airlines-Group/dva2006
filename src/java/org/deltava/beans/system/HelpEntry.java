// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * A class for storing Online Help entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HelpEntry implements java.io.Serializable, Comparable {

    private String _title;
    private String _subject;
    private String _body;
    
    /**
     * Creates a new Help Entry with a given title and body
     * @param title The title of this Help Entry
     * @param body The body content of this Help Entry
     * @throws NullPointerException if the title is null
     */
    public HelpEntry(String title, String body) {
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
    
    /**
     * Compare entries by doing a comparison on their title.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @see String#compareTo(java.lang.String)
     */
    public int compareTo(Object o2) {
        HelpEntry he2 = (HelpEntry) o2;
        return _title.compareTo(he2.getTitle());
    }

    /**
     * Checks for equality using the title.
     */
    public boolean equals(Object o2) {
       return (o2 instanceof HelpEntry) ? (compareTo(o2) == 0) : false;
    }
}