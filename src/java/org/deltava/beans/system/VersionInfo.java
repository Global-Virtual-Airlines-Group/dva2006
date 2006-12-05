// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
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
    
    public static final int BUILD = 118;
    // public static final int RELEASE_CANDIDATE = 49;
    public static final boolean FINAL = false;
    
    public static final String TXT_COPYRIGHT = "Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.";
    public static final String HTML_COPYRIGHT = "Copyright &copy; 2004, 2005, 2006 <a rel=\"external\" class=\"small\" href=\"http://www.gvagroup.org/\">Global Virtual Airlines Group</a>. All Rights Reserved.";
    
    public static final String APPNAME = "(Golgotha v" + String.valueOf(MAJOR) + "." + String.valueOf(MINOR) + ")";
}