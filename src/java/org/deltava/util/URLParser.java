// Copyright 2004, 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

/**
 * A utility class to parse URLs.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class URLParser {

    private final LinkedList<String> _elements = new LinkedList<String>();
    private final String _ext;
    
    /**
     * Initializes the object and parses the URL.
     * @param rawURL the URL to parse
     */
    public URLParser(String rawURL) {
        super();
        int extpos = rawURL.lastIndexOf('.');
        _ext = (extpos == -1) ? "" : rawURL.substring(extpos + 1);
        
        // Parse the directories
        String rURL = (extpos != -1) ? rawURL.substring(0, extpos) : rawURL; 
        StringTokenizer tokens = new StringTokenizer(rURL, "/");
        while (tokens.hasMoreTokens())
            _elements.add(tokens.nextToken());
    }

    /**
     * Returns the file extension.
     * @return the extension
     */
    public String getExtension() {
        return _ext;
    }
    
    /**
     * Returns the file name and extension.
     * @return the filename + extension
     */
    public String getFileName() {
        StringBuilder buf = new StringBuilder(getName());
        buf.append('.');
        buf.append(_ext);
        return buf.toString();
    }
    
    /**
     * Returns the file name, minus extension.
     * @return the file name
     */
    public String getName() {
        return _elements.getLast();
    }
    
    /**
     * Returns the path entries.
     * @return a LinkedList of path entries
     */
    public LinkedList<String> getPath() {
    	return (_elements.size() == 1) ? new LinkedList<String>() : 
    		new LinkedList<String>(_elements.subList(0, _elements.size() - 1));
    }
    
    /**
     * Returns the first directory element.
     * @return the first element , or an empty string if not present
     */
    public String getFirstPath() {
    	return (_elements.size() == 1) ? "" : _elements.getFirst(); 
    }
    
    /**
     * Returns the last directory element.
     * @return the last element before the file name, or an empty string if not present
     */
    public String getLastPath() {
        return (_elements.size() == 1) ? "" : _elements.get(_elements.size() - 2);
    }
    
    /**
     * Returns if the URL contains a particular path entry.
     * @param path the path entry to search for
     * @return TRUE if the URL contains the path, otherwise FALSE
     */
    public boolean containsPath(String path) {
        return _elements.contains(path);
    }
    
    /**
     * Returns the number of elements in the URL.
     * @return the number of elements
     */
    public int size() {
        return _elements.size();
    }
}