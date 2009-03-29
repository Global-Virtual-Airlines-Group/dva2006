// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An interface to store version info constants.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public interface VersionInfo {

    public static final int MAJOR = 2;
    public static final int MINOR = 5;
    
    public static final int BUILD = 235;
    // public static final int RELEASE_CANDIDATE = 1;
    public static final boolean FINAL = false;
    
    public static final String TXT_COPYRIGHT = "Copyright 2004 - 2009 Global Virtual Airlines Group. All Rights Reserved.";
    public static final String HTML_COPYRIGHT = "Copyright &copy; 2004 - 2009 <a rel=\"external\" class=\"small\" href=\"http://www.gvagroup.org/\">Global Virtual Airlines Group</a>. All Rights Reserved.";
    
    public static final String APPNAME = "(Golgotha v" + String.valueOf(MAJOR) + "." + String.valueOf(MINOR) + ")";
    public static final String USERAGENT = "Golgotha/" + String.valueOf(MAJOR) + "." + String.valueOf(MINOR) + "-b" + String.valueOf(BUILD);
}