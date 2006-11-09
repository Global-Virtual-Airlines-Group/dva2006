// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * A class for storing Staff Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Staff extends DatabaseBean {

    private String _firstName;
    private String _lastName;
    
    private String _title;
    private String _area;
    private String _body;
    private String _eMail;
    
    private int _sortOrder;
    
    /**
     * Create a new Staff Profile with a given first and last name.
     * @param fName the Staff Member's first (given) name
     * @param lName the Staff Member's last (family) name
     * @throws NullPointerException if either fName or lName are null
     */
    public Staff(String fName, String lName) {
        super();
        _firstName = fName.trim();
        _lastName = lName.trim();
    }
    
    /**
     * Returns the Staff Member's first (given) name. 
     * @return the member's first name
     */
    public String getFirstName() {
        return _firstName;
    }
    
    /**
     * Returns the Staff Member's last (family) name.
     * @return the member's last name
     */
    public String getLastName() {
        return _lastName;
    }
    
    /**
     * Returns the Staff Member's e-mail address.
     * @return the member's e-mail address
     */
    public String getEMail() {
        return _eMail;
    }
    
    /**
     * Returns the Staff Member's title.
     * @return the title
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Returns the Staff Member's functional area.
     * @return the area
     */
    public String getArea() {
    	return _area;
    }
    
    /**
     * Returns the Staff Member's biograpy.
     * @return the biography
     */
    public String getBody() {
        return _body;
    }

    /**
     * Get the sort order for this Staff Member profile.
     * @return the sort order value
     */
    public int getSortOrder() {
        return _sortOrder;
    }
    
    /**
     * Updates the Staff Member's title.
     * @param title the new title
     */
    public void setTitle(String title) {
        _title = title;
    }
    
    /**
     * Updates the Staff Member's functional area.
     * @param area the area
     */
    public void setArea(String area) {
    	_area = area;
    }
    
    /**
     * Updates the Staff Member's e-mail addres.
     * @param eMail the new e-mail address
     */
    public void setEMail(String eMail) {
        _eMail = eMail;
    }
    
    /**
     * Sets the Staff Member's biography.
     * @param body the new biography
     */
    public void setBody(String body) {
        _body = body;
    }
    
    /**
     * Sets the new sort order for this Staff Member profile.
     * @param sortOrder the new sort order
     * @throws IllegalArgumentException if the sort Order is zero or negative
     */
    public void setSortOrder(int sortOrder) {
        if (sortOrder <= 0)
            throw new IllegalArgumentException("Sort Order cannot be zero or negative");
        
        _sortOrder = sortOrder;
    }
    
    /**
     * Compare two Staff Memebr profiles. Check the sort order, then the last name.
     * @throws ClassCastException if o2 is not a Staff profile
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o2) {
        Staff s2 = (Staff) o2;
        int tmpResult = new Integer(_sortOrder).compareTo(new Integer(s2.getSortOrder()));
        return (tmpResult == 0) ? _lastName.compareTo(s2.getLastName()) : tmpResult; 
    }
}