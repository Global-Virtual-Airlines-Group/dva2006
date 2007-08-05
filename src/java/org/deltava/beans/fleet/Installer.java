// Copyright 2004, 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;

/**
 * A bean to store information about Fleet Library installers.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Installer extends FleetEntry implements ComboAlias {
	
	public static final String[] FS_NAMES = new String[] {"Microsoft Flight Simulator 2002", "Microsoft Flight Simulator 2004", 
		"Microsoft Flight Simulator X"};
	public static final String[] FS_CODES = new String[] {"FS2002", "FS2004", "FSX"};

	private final Collection<AirlineInformation> _apps = new TreeSet<AirlineInformation>();
	
	private String _imgName;
    private String _code;
    
    private final Collection<String> _fsVersions = new LinkedHashSet<String>();
    
    /**
     * Creates a new Fleet Installer entry for a given file.
     * @param fName the installer filename.
     */
    public Installer(String fName) {
        super(fName);
    }
    
    /**
     * Returns the Airlines whose Fleet Libraries will include this Installer.
     * @return a Collection of AirlineInformation beans
     * @see Installer#addApp(AirlineInformation)
     */
    public Collection<AirlineInformation> getApps() {
    	return _apps;
    }
    
    /**
     * Returns this installer's equipment code.
     * @return the equipment code
     * @see Installer#setCode(String)
     */
    public String getCode() {
        return _code;
    }
    
    /**
     * Returns this installer's label image.
     * @return the image resource name
     * @see Installer#setImage(String)
     */
    public String getImage() {
    	return _imgName;
    }
    
    /**
     * Returns a string representation of the version (MAJOR.MINOR.SUB).
     * @return a version string
     */
    public String getVersion() {
        StringBuilder buf = new StringBuilder(String.valueOf(getMajorVersion()));
        buf.append('.');
        buf.append(String.valueOf(getMinorVersion()));
        buf.append('.');
        buf.append(String.valueOf(getSubVersion()));
        return buf.toString();
    }

    /**
     * Returns a string representation of the version (MAJORMINORSUB).
     * @return a version code
     */
    public String getVersionCode() {
        StringBuilder buf = new StringBuilder(String.valueOf(getMajorVersion()));
        buf.append(String.valueOf(getMinorVersion()));
        buf.append(String.valueOf(getSubVersion()));
        return buf.toString();
    }
    
    /**
     * Adds this Installer to an Airline's Fleet Library.
     * @param info an AirlineInformation bean
     * @see Installer#getApps()
     */
    public void addApp(AirlineInformation info) {
    	if (info != null)
    		_apps.add(info);
    }
    
    /**
     * Adds a Flight Simulator version compatible with this Fleet Installer.
     * @param fsCode a Flight Simulator version code
     * @see Installer#getFSVersions()
     * @see Installer#getFSVersionNames()
     */
    public void addFSVersion(String fsCode) {
    	if (fsCode == null)
    		return;
    	
    	fsCode = fsCode.toUpperCase();
    	if (StringUtils.arrayIndexOf(FS_CODES, fsCode) != -1)
    		_fsVersions.add(fsCode);
    }
    
    /**
     * Sets the Flight Simulator versions compatible with this Fleet Installer.
     * @param fsCodes a comma-delimited list of Flight Simulator version codes
     * @see Installer#getFSVersions()
     * @see Installer#getFSVersionNames()
     */
    public void setFSVersions(String fsCodes) {
    	_fsVersions.clear();
    	for (Iterator<String> i = StringUtils.split(fsCodes, ",").iterator(); i.hasNext(); )
    		addFSVersion(i.next());
    }
    
    /**
     * Returns the Flight Simulator versions compatible with this Installer.
     * @return a Collection of version codes
     * @see Installer#getFSVersionNames()
     * @see Installer#setFSVersions(String)
     */
    public Collection<String> getFSVersions() {
    	return _fsVersions;
    }
    
    /**
     * Returns the Flight Simulator versions compatible with this Installer.
     * @return a Collection of version names
     * @see Installer#getFSVersions()
     * @see Installer#setFSVersions(String)
     */
    public Collection<String> getFSVersionNames() {
    	Collection<String> results = new LinkedHashSet<String>();
    	for (Iterator<String> i = _fsVersions.iterator(); i.hasNext(); ) {
    		int ofs = StringUtils.arrayIndexOf(FS_CODES, i.next());
    		if (ofs != -1)
    			results.add(FS_NAMES[ofs]);
    	}
    	
    	return results;
    }
    
    /**
     * Updates this installer's equipment type code (eg. DC8)
     * @param code the equipment type code (will be converted to uppercase)
     * @see Installer#getCode()
     */
    public void setCode(String code) {
    	if (code != null)
    		_code = code.trim().toUpperCase();
    }
    
    /**
     * Updates this installer's label image name.
     * @param imgName the image resource name
     * @see Installer#getImage
     */
    public void setImage(String imgName) {
    	_imgName = imgName;
    }

    public String getComboName() {
    	return getName();
    }
    
    public String getComboAlias() {
    	return getCode();
    }
}