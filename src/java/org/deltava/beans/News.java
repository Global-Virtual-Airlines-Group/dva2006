// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.Date;

/**
 * A class for storing System News entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class News extends DatabaseBean implements AuthoredBean {

    private Date _date;
    private String _subject;
    private String _body;

    private String _author;
    private int _authorID;

    /**
     * Create a new System News entry object, with the Date defaulting to the current date/time
     * @param sbj This entry's title
     * @param aN This entry's author's name
     * @param body This entry's content
     * @throws NullPointerException if sbj is null
     */
    public News(String sbj, String aN, String body) {
        super();
        _date = new Date();
        setSubject(sbj);
        setBody(body);
        _author = aN;
    }

    /**
     * Return the date/entry this entry was created
     * @return The date/time this System News entry was created
     */
    public Date getDate() {
        return _date;
    }

    /**
     * Return this entry's Title
     * @return The Title of this System News entry
     * @see News#setSubject(String)
     */
    public String getSubject() {
        return _subject;
    }

    /**
     * Return the name of this entry's author
     * @return The Name of the author
     */
    public String getAuthorName() {
        return _author;
    }

    /**
     * Return the database row ID of this entry's author. <i>This typically will only be called by a DAO </i>
     * @return The primary key of the entry in the <b>PILOTS </b> table in the database that corresponds to
     * this System News entry's author.
     * @see News#setAuthorID(int)
     */
    public int getAuthorID() {
        return _authorID;
    }

    /**
     * Return this entry's Content.
     * @return The Content of this System News entry
     * @see News#setBody(String)
     */
    public String getBody() {
        return _body;
    }

    /**
     * Compare two System News entries by comparing their date.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
        News n2 = (News) o2;
        return (_date.compareTo(n2.getDate()) * -1);
    }

    /**
     * Update the database row ID of this entry's author. <i>This typically will only be called by a DAO </i>
     * @param id The primary key of the entry in the <b>PILOTS </b> table in the database that corresponds to
     * this System News entry's author.
     * @throws IllegalArgumentException if the database ID is zero or negative
     * @see News#getAuthorID()
     */
    public void setAuthorID(int id) {
       validateID(_authorID, id);
        _authorID = id;
    }

    /**
     * Update the date of this entry.
     * @param d The new date/time for this System News entry
     * @see News#getDate()
     */
    public void setDate(Date d) {
        _date = d;
    }
    
    /**
     * Updates this subject of this entry.
     * @param subj the new subject
     * @throws NullPointerException if subj is null
     * @see News#getSubject()
     */
    public void setSubject(String subj) {
       _subject = subj.trim();
    }
    
    /**
     * Updates this entry's body content
     * @param body the new content
     * @see News#getBody()
     */
    public void setBody(String body) {
       _body = body;
    }
}