// Copyright 2005, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

/**
 * A class for storing Applicant entries.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class Applicant extends Person {
	
    public static final int PENDING = 0;
    public static final int APPROVED = 1;
    public static final int REJECTED = 2;
    
    /**
     * Valid applicant statuses
     */
    public static final String[] STATUS = {"Pending", "Approved", "Rejected"}; 
    
    /**
     * The only security role an Applicant can belong to 
     */
    public static final String ROLE = "Applicant";
    
	/**
	 * Valid Flight Simulator version strings.
	 */
	public static final String FSVERSION[] = { "Unknown/Other", "X-Plane", "FS2002", "FS2004", "FS X" };

	/**
	 * Valid Flight Simulator version values.
	 */
	public static final int FSVERSION_CODE[] = { 0, 100, 2002, 2004, 2006 };
    
    private int _pilotID;
    private String _legacyURL;
    private boolean _legacyVerified;
    
    private int _simVersion;
    
    private String _registerHostName;
    private String _registerAddress;
    private String _comments;
    private String _hrComments;
    
    private final Map<Long, String> _typeChoices = new TreeMap<Long, String>();
    
    /**
     * Create an Applicant object with a given first and last name, converted to "proper case".
     * @param firstName the Applicant's first (given) name
     * @param lastName the Applicant's last (family) name
     * @throws NullPointerException if either name is null
     * @see org.deltava.util.StringUtils#properCase(String)
     */
    public Applicant(String firstName, String lastName) {
        super(firstName, lastName);
    }
    
    /**
     * Returns the status name.
     * @return &quot;Applicant&quot;
     */
    public String getStatusName() {
    	return ROLE;
    }

    /**
     * Returns the URL for legacy hours verification.
     * @return the URL for legacy hours verification, null if not available
     * @see Applicant#setLegacyURL(String)
     */
    public String getLegacyURL() {
        return _legacyURL;
    }
    
    /**
     * Returns legacy hours verification status.
     * @return TRUE if legacy hours have been verified
     * @see Applicant#setLegacyVerified(boolean)
     */
    public boolean getLegacyVerified() {
        return _legacyVerified;
    }
    
    /**
     * Returns the Database ID of this Applicant's Pilot record.
     * @return the Database ID if hired as a Pilot
     * @see Applicant#setPilotID(int)
     */
    public int getPilotID() {
       return _pilotID;
    }
    
    /**
     * Returns any additional Applicant comments.
     * @return the comments
     * @see Applicant#setComments(String)
     * @see Applicant#getHRComments()
     */
    public String getComments() {
    	return _comments;
    }
    
    /**
     * Returns any HR comments.
     * @return the comments
     * @see Applicant#setHRComments(String)
     * @see Applicant#getComments()
     */
    public String getHRComments() {
    	return _hrComments;
    }
    
    /**
     * Returns the host name this Applicant registered from.
     * @return the host name
     * @see Applicant#setRegisterHostName(String)
     * @see Applicant#getRegisterAddress()
     */
    public String getRegisterHostName() {
       return _registerHostName;
    }
    
    /**
     * Returns the address this Applicant registered from.
     * @return the IP address
     * @see Applicant#setRegisterAddress(String)
     * @see Applicant#getRegisterHostName()
     */
    public String getRegisterAddress() {
    	return _registerAddress;
    }
    
	/**
	 * The Flight Simulator version preferred by this Applicant.
	 * @return the version number
	 * @see Applicant#getSimVersionCode()
	 * @see Applicant#setSimVersion(int)
	 * @see Applicant#setSimVersion(String)
	 */
	public int getSimVersion() {
		return _simVersion;
	}

	/**
	 * The Flight Simulator version code preferred by this Applicant.
	 * @return the version code
	 * @see Applicant#getSimVersion()
	 * @see Applicant#setSimVersion(String)
	 * @see Applicant#setSimVersion(int)
	 */
	public String getSimVersionCode() {
		for (int x = 0; x < FSVERSION_CODE.length; x++) {
			if (_simVersion == FSVERSION_CODE[x])
				return FSVERSION[x];
		}

		return FSVERSION[0];
	}
    
    /**
     * Sets the URL for legacy hours verification
     * @param url the URL for legacy hours verification
     * @see Applicant#getLegacyURL()
     */
    public void setLegacyURL(String url) {
        _legacyURL = url;
    }
  
    /**
     * Sets the legacy hours verification flag.
     * @param verified TRUE if hours have been verified, FALSE otherwise
     * @see Applicant#getLegacyVerified()
     */
    public void setLegacyVerified(boolean verified) {
        _legacyVerified = verified;
    }
    
    /**
     * Sets the preferred equipment program for a stage.
     * @param stage the stage number
     * @param eqName the equipment program
     */
    public void setTypeChoice(int stage, String eqName) {
    	if (eqName == null)
    		_typeChoices.remove(Long.valueOf(stage));
    	else
    		_typeChoices.put(Long.valueOf(stage), eqName);
    }
    
	/**
	 * Set the Flight Simulator version used by this Applicant.
	 * @param version the Flight Simulator version as found in {@link Applicant#FSVERSION}
	 * @throws IllegalArgumentException if the version cannot be found
	 * @see Applicant#setSimVersion(String)
	 * @see Applicant#getSimVersion()
	 */
    public void setSimVersion(int version) {
		for (int x = 0; x < FSVERSION_CODE.length; x++) {
			if (version == FSVERSION_CODE[x]) {
				_simVersion = version;
				return;
			}
		}

		throw new IllegalArgumentException("Invalid Flight Simulator version - " + version);
    }
    
	/**
	 * Set the Flight Simulator version used by this Applicant.
	 * @param version the Flight Simulator version as found in {@link Applicant#FSVERSION}
	 * @throws IllegalArgumentException if the version cannot be found
	 * @see Applicant#setSimVersion(int)
	 * @see Applicant#getSimVersion()
	 */
	public void setSimVersion(String version) {
		for (int x = 0; x < FSVERSION.length; x++) {
			if (FSVERSION[x].equals(version)) {
				_simVersion = FSVERSION_CODE[x];
				return;
			}
		}

		throw new IllegalArgumentException("Invalid Flight Simulator version - " + version);
	}
    
    /**
     * Sets the host name this Applicant registered from.
     * @param hostName the host name
     * @see Applicant#getRegisterHostName()
     * @see Applicant#setRegisterAddress(String)
     */
    public void setRegisterHostName(String hostName) {
       _registerHostName = hostName;
    }
    
    /**
     * Sets the remote address this Applicant registered from.
     * @param addr the IP address
     * @see Applicant#getRegisterAddress()
     * @see Applicant#setRegisterHostName(String)
     */
    public void setRegisterAddress(String addr) {
    	_registerAddress = addr;
    }
    
    /**
     * Updates the Applicant comments.
     * @param comments the comments
     * @see Applicant#getComments()
     * @see Applicant#setHRComments(String)
     */
    public void setComments(String comments) {
    	_comments = comments;
    }
    
    /**
     * Updates the HR comments.
     * @param comments the comments
     * @see Applicant#getHRComments()
     * @see Applicant#setComments(String)
     */
    public void setHRComments(String comments) {
    	_hrComments = comments;
    }
    
    /**
     * Updates the Database ID of this Applicant's Pilot record.
     * @param pilotID the database ID, if hired as a Pilot
     * @throws IllegalArgumentException if pilotID is negative
     * @throws IllegalStateException if the Applicant has not been Approved
     * @see Applicant#getPilotID()
     */
    public void setPilotID(int pilotID) {
       if (pilotID != 0) {
          validateID(_pilotID, pilotID);
          if (getStatus() != APPROVED)
             throw new IllegalStateException("Applicant not Approved");
          
          _pilotID = pilotID;
       }
    }
    
    /**
     * Sets the Applicant's status.
     * @param status the status code
     * @throws IllegalArgumentException if the status code is negative or invalid
     * @see Person#setStatus(int)
     * @see Person#getStatus()
     */
    public final void setStatus(int status) {
       if (status >= Applicant.STATUS.length)
          throw new IllegalArgumentException("Invalid Applicant Status - " + status);

      super.setStatus(status);
    }
    
    /**
     * Add a security role to an Applicant
     * @param roleName the name of the role
     * @throws UnsupportedOperationException always thrown since applicants cannot have roles
     * @see Applicant#getRoles()
     * @see Applicant#isInRole(String)
     */
    public void addRole(String roleName) {
        throw new UnsupportedOperationException("Applicants cannot be added to Security Roles");
    }
    
    /**
     * Queries this applicant's role. Applicants can only be members of a single role - "Applicants"
     * @return TRUE if the queried role is "Applicant", otherwise FALSE
     * @see Applicant#getRoles()
     */
    public boolean isInRole(String roleName) {
        return (Applicant.ROLE.equals(roleName) || "*".equals(roleName));
    }
    
    /**
     * Returns the security roles this Applicant belongs to. 
     * @return a Collection with a single string - Applicant.ROLE.
     * @see Applicant#isInRole(String) 
     */
    public Collection<String> getRoles() {
    	return Collections.singleton(ROLE);
    }
    
    /**
     * Returns the stage equipment type choices. 
     * @return a Map of choices, keyed by stage
     */
    public Map<Long, String> getTypeChoices() {
    	return new LinkedHashMap<Long, String>(_typeChoices);
    }
    
    /**
     * Selects a table row class based upon the Applicant's status.
     * @return the row CSS class name
     */
    public String getRowClassName() {
    	final String[] ROW_CLASSES = {"opt1", null, "err"};
    	return ROW_CLASSES[getStatus()];
    }
}