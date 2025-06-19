// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * A class to store version info constants.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class VersionInfo {

    public static final int MAJOR = 12;
    public static final int MINOR = 0;
    
    public static final int BUILD = 956;
    private static final int HOTFIX = 4;
    
    public static final String TXT_COPYRIGHT = "Copyright 2004 - 2025 Global Virtual Airlines Group. All Rights Reserved.";
    public static final String HTML_COPYRIGHT = "Copyright &copy; 2004 - 2025 <a rel=\"external\" class=\"small\" href=\"https://www.gvagroup.org/\">Global Virtual Airlines Group</a>. All Rights Reserved.";
    
    // static class
    private VersionInfo() {
    	super();
    }
    
    /**
     * Returns the full build number, optionally including the hotfix.
     * @return the full build number
     */
    @SuppressWarnings("unused")
	public static String getFullBuild() {
    	if (HOTFIX < 1) return String.valueOf(BUILD);
    	StringBuilder buf = new StringBuilder();
    	buf.append(BUILD);
    	buf.append('.');
    	buf.append(HOTFIX);
    	return buf.toString();
    }
    
    /**
     * Returns the application name and version.
     * @return the app name/version
     */
    public static String getAppName() {
    	StringBuilder buf = new StringBuilder("(Golgotha v");
    	buf.append(MAJOR).append('.').append(MINOR);
    	buf.append(')');
    	return buf.toString();
    }
    
    /**
     * Returns the application HTTP user agent name.
     * @return the user agent name
     */
    public static String getUserAgent() {
    	return String.format("Golgotha/%d.%d-b%s", Integer.valueOf(MAJOR), Integer.valueOf(MINOR), getFullBuild());
    }
}