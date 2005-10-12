package org.deltava.beans.system;

/**
 * An interface to store version info constants.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface VersionInfo {

    public static final int MAJOR = 1;
    public static final int MINOR = 0;
    
    public static final int BUILD = 41;
    //public static final int RELEASE_CANDIDATE = 2;
    
    public static final String TXT_COPYRIGHT = "Copyright (c) 2004, 2005 Luke J. Kolin. All Rights Reserved.";
    public static final String HTML_COPYRIGHT = "Copyright &copy; 2004, 2005 Luke J. Kolin. All Rights Reserved.";
    
    public static final String APPNAME = "(Golgotha v" + String.valueOf(MAJOR) + "." + String.valueOf(MINOR) + ")";
}