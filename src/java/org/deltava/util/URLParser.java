package org.deltava.util;

import java.util.*;

/**
 * A utility class to parse URLs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class URLParser {

    private LinkedList _elements = new LinkedList();
    private String _ext;
    
    /**
     * Initializes the object and parses the URL.
     * @param rawURL the URL to parse
     */
    public URLParser(String rawURL) {
        super();
        int extpos = rawURL.lastIndexOf('.');
        _ext = (extpos == -1) ? "" : rawURL.substring(extpos + 1);
        
        // Parse the directories
        if (extpos != -1)
            rawURL = rawURL.substring(0, extpos);
        
        StringTokenizer tokens = new StringTokenizer(rawURL, "/");
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
        return (String) _elements.getLast();
    }
    
    /**
     * Returns the last directory element.
     * @return the last element before the file name, or an empty string if not present
     */
    public String getLastPath() {
        return (_elements.size() == 1) ? "" : (String) _elements.get(_elements.size() - 2);
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