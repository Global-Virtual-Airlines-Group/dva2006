package org.deltava.beans.fleet;

/**
 * A bean to store information about Manuals.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Manual extends FleetEntry {
   
   private boolean _isNewsletter;

    /**
     * Creates a new Manual bean.
     * @param fName the file name of the manual
     */
    public Manual(String fName) {
        super(fName);
    }

    /**
     * Returns this manual's version number. Manuals only have a major version number.
     * @see FleetEntry#getVersion()
     */
    public String getVersion() {
        return String.valueOf(getMajorVersion());
    }
    
    /**
     * Returns if this Manual is an issue of the Newsletter.
     * @return TRUE if this is a Newsletter, otherwise FALSE
     */
    public boolean getIsNewsletter() {
       return _isNewsletter;
    }

    /**
     * Marks this manual as an issue of the Newsletter.
     * @param isNews TRUE if this is a Newsletter, otherwise FALSE
     */
    public void setIsNewsletter(boolean isNews) {
       _isNewsletter = isNews;
    }
    
    public final void setVersion(int major) {
        super.setVersion(major, 0, 0);
    }

    public final void setVersion(int major, int minor, int subVersion) {
        super.setVersion(major, 0, 0);
    }
    
    /**
     * Returns the CSS row class name if disaplayed in a view table.
     * @return the CSS class name
     */
    public String getRowClassName() {
       String tmpClass = super.getRowClassName();
       if (tmpClass == null)
          return _isNewsletter ? "opt2" : null;
       
       return tmpClass;
    }
}