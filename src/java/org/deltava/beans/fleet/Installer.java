// Copyright 2004, 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.system.AirlineInformation;

/**
 * A bean to store information about Fleet Library installers.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Installer extends FleetEntry implements ComboAlias {

	private final Collection<AirlineInformation> _apps = new TreeSet<AirlineInformation>();
	
	private String _imgName;
    private String _code;
    
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