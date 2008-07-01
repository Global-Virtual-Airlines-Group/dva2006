// Copyright 2004, 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * A class for storing Staff Profiles.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class Staff extends Pilot {

    private String _title;
    private String _area;
    private String _body;
    
    private int _sortOrder;
    
    /**
     * Create a new Staff Profile with a given first and last name.
     * @param fName the Staff Member's first (given) name
     * @param lName the Staff Member's last (family) name
     * @throws NullPointerException if either fName or lName are null
     */
    public Staff(String fName, String lName) {
        super(fName, lName);
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
     * Sets the Staff Member's biography.
     * @param body the new biography
     */
    public void setBody(String body) {
        _body = body;
    }
    
    /**
     * Sets the new sort order for this Staff Member profile.
     * @param sortOrder the new sort order
     */
    public void setSortOrder(int sortOrder) {
        _sortOrder = Math.max(1, sortOrder);
    }
    
    /**
     * Compare two Staff Memebr profiles. Check the sort order, then the last name.
     * @throws ClassCastException if o2 is not a Staff profile
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o2) {
        Staff s2 = (Staff) o2;
        int tmpResult = Integer.valueOf(_sortOrder).compareTo(Integer.valueOf(s2.getSortOrder()));
        return (tmpResult == 0) ? getLastName().compareTo(s2.getLastName()) : tmpResult; 
    }
}