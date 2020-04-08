// Copyright 2005, 2007, 2009, 2015, 201, 20196 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

/**
 * A class for storing Applicant entries.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class Applicant extends Person {
	
	private static final long serialVersionUID = 749820483432058282L;

    private int _pilotID;
    private String _legacyURL;
    private boolean _legacyVerified;
    private boolean _hasCaptcha;
    
    private ApplicantStatus _status;
    private Simulator _simVersion;
    
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
     * Returns the Applicant's status.
     * @return an ApplicantStatus enum
     */
    public ApplicantStatus getStatus() {
    	return _status;
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
     * Returns whether the CAPTCHA has been verified.
     * @return TRUE if CAPTCHA verified, otherwise FALSE
     * @see Applicant#setHasCAPTCHA(boolean)
     */
    public boolean getHasCAPTCHA() {
    	return _hasCaptcha;
    }
    
	/**
	 * Returns the Simulator preferred by this Applicant.
	 * @return the Simulator
	 * @see Applicant#setSimVersion(Simulator)
	 */
	public Simulator getSimVersion() {
		return _simVersion;
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
	 * Set the Simulator used by this Applicant.
	 * @param s the Simulator
	 * @see Applicant#getSimVersion()
	 */
    public void setSimVersion(Simulator s) {
    	_simVersion = s;
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
          if (_status != ApplicantStatus.APPROVED)
             throw new IllegalStateException("Applicant not Approved");
          
          _pilotID = pilotID;
       }
    }
    
    /**
     * Updates the Applicant's status.
     * @param s an ApplicantStatus
     */
    public void setStatus(ApplicantStatus s) {
    	_status = s;
    }
    
    /**
     * Updates whether this Applicant has passed tha CAPTCHA check.
     * @param hasCaptchaVerified TRUE if CAPTCHA verified, othersie FALSE
     */
    public void setHasCAPTCHA(boolean hasCaptchaVerified) {
    	_hasCaptcha = hasCaptchaVerified;
    }
    
    @Override
    public void addRole(String roleName) {
        throw new UnsupportedOperationException("Applicants cannot be added to Security Roles");
    }
    
    @Override
    public boolean isInRole(String roleName) {
        return (Role.APPLICANT.getName().equals(roleName) || "*".equals(roleName));
    }
    
    @Override
    public Collection<String> getRoles() {
    	return Collections.singleton(Role.APPLICANT.getName());
    }
    
    /**
     * Returns the stage equipment type choices. 
     * @return a Map of choices, keyed by stage
     */
    public Map<Long, String> getTypeChoices() {
    	return new LinkedHashMap<Long, String>(_typeChoices);
    }
    
    @Override
    public String getRowClassName() {
    	return _status.getRowClassName();
    }
}