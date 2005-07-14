package org.deltava.beans.event;

import java.io.Serializable;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.schedule.Airport;

/**
 * A class to hold Online Event pilot signups. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Signup implements Serializable {

    private int _eventID;
    private int _pilotID;
    private String _eqType;
    private String _remarks;
    
    private Airport _airportD;
    private Airport _airportA;
    
    public Signup(int eventID, int pilotID) {
       super();
       setEventID(eventID);
       setPilotID(pilotID);
    }
    
    public int getEventID() {
        return _eventID;
    }
    
    public String getEquipmentType() {
        return _eqType;
    }
    
    public Airport getAirportA() {
       return _airportA;
    }
    
    public Airport getAirportD() {
       return _airportD;
    }
    
    public int getPilotID() {
        return _pilotID;
    }
    
    public String getRemarks() {
        return _remarks;
    }
    
    public void setEventID(int id) {
        DatabaseBean.validateID(_eventID, id);
        _eventID = id;
    }
    
    public void setPilotID(int id) {
       DatabaseBean.validateID(_pilotID, id);
       _pilotID = id;
    }
    
    public void setAirportA(Airport a) {
       _airportA = a;
    }
    
    public void setAirportD(Airport a) {
       _airportD = a;
    }
    
    public void setEquipmentType(String eqType) {
        _eqType = eqType;
    }
    
    public void setRemarks(String remarks) {
        _remarks = remarks;
    }
}