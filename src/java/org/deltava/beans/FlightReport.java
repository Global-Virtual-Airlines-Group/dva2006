package org.deltava.beans;

import java.util.*;

import org.deltava.beans.schedule.Airline;

/**
 * A class for dealing with PIREP data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightReport extends Flight implements Comparable, ViewEntry {

   public static final int DRAFT = 0;
   public static final int SUBMITTED = 1;
   public static final int HOLD = 2;
   public static final int OK = 3;
   public static final int REJECTED = 4;

   /**
    * Valid PIREP statuses
    */
   public static final String STATUS[] = { "Draft", "Submitted", "Hold", "OK", "Rejected" };
   private static final String[] ROW_CLASSES = { "opt2", "opt1", "warn", null, "err" };

   /**
    * Valid Flight Simulator version strings
    */
   public static final String FSVERSION[] = { "Unknown", "FS98", "FS2000", "FS2002", "FS2004" };

   /**
    * Valid Flight Simulator version values
    */
   public static final int FSVERSION_CODE[] = { 0, 98, 2000, 2002, 2004 };
   
   /**
    * Flight flown without Equipment Type Rating.
    */
   public static final int ATTR_NOTRATED = 0x01;
   
   /**
    * Flight flown on VATSIM network.
    */
   public static final int ATTR_VATSIM = 0x02;
   
   /**
    * Flight flown on IVAO network.
    */
   public static final int ATTR_IVAO = 0x04;
   
   /**
    * Flight flown on FPI network.
    */
   public static final int ATTR_FPI = 0x08;
   
   /**
    * Flight logged using ACARS.
    */
   public static final int ATTR_ACARS = 0x10;
   
   /**
    * Flight flown using unknown route pair.
    */
   public static final int ATTR_ROUTEWARN = 0x20;
   
   /**
    * Flight flown with unusually high or low logged hous.
    */
   public static final int ATTR_TIMEWARN = 0x40;
   
   /**
    * Flight flown as a Check Ride.
    */
   public static final int ATTR_CHECKRIDE = 0x80;
   
   /**
    * Attribute mask for VATSIM/IVAO/FPI online flights.
    */
   public static final int ATTR_ONLINE_MASK = 0x0E;

   public static final String DBID_PILOT = "$PILOT$";
   public static final String DBID_DISPOSAL = "$DISPOSALPILOTID$";
   public static final String DBID_ASSIGN = "$ASSIGN$";
   public static final String DBID_EVENT = "$EVENT$";
   public static final String DBID_ACARS = "$ACARS$";

   private Date _date;
   private Date _createdOn;
   private Date _submittedOn;
   private Date _disposedOn;
   private int _length;
   private int _status = FlightReport.DRAFT;
   private int _fsVersion;
   private int _attr;
   private String _remarks;

   private String _firstName;
   private String _lastName;
   private String _rank;

   private Set _captEQType = new TreeSet();

   // Stores Integers pointing to other database IDs, see PIREPConstants
   private Map _dbIds = new HashMap();

   /**
    * Creates a new Flight Report object with a given flight.
    * @param a the Airline
    * @param flightNumber the Flight Number
    * @param leg the Leg Number
    * @throws NullPointerException if the Airline Code is null
    * @throws IllegalArgumentException if the Flight Report is zero or negative
    * @throws IllegalArgumentException if the Leg is less than 1 or greater than 5
    * @see Flight#setAirline(Airline)
    * @see Flight#setFlightNumber(int)
    * @see Flight#setLeg(int)
    */
   public FlightReport(Airline a, int flightNumber, int leg) {
      super(a, flightNumber, leg);
   }

   /**
    * Creates a new Flight Report from an existing Flight entry (like an Assignment or ScheduleEntry).
    * @param f the existing Flight
    */
   public FlightReport(Flight f) {
      super(f.getAirline(), f.getFlightNumber(), f.getLeg());
      setEquipmentType(f.getEquipmentType());
      setAirportD(f.getAirportD());
      setAirportA(f.getAirportA());
   }

   /**
    * Return's the Pilot's First (given) name. <i>This is not guaranteed to be set. </i>
    * @return the Pilot's first name
    * @see FlightReport#setFirstName(String)
    */
   public String getFirstName() {
      return _firstName;
   }

   /**
    * Returns the Pilot's Last (family) name. <i>This is not guaranteed to be set. </i>
    * @return the Pilot's last name
    * @see FlightReport#setLastName(String)
    */
   public String getLastName() {
      return _lastName;
   }

   /**
    * Returns the Pilot's rank at the time of the Flight.
    * @return the Pilot's rank
    * @see FlightReport#getRank()
    */
   public String getRank() {
      return _rank;
   }

   /**
    * Returns the length of the fllight <i>in hours multiplied by ten</i>. This is done to avoid rounding errors when
    * using a floating point number.
    * @return the length of the flight <i>in hours multiplied by ten</i>
    * @see FlightReport#setLength(int)
    * @see ACARSFlightReport#getLength()
    */
   public int getLength() {
      return _length;
   }

   /**
    * Returns the remarks for this Flight Report.
    * @return the remarks
    * @see FlightReport#setRemarks(String)
    */
   public String getRemarks() {
      return _remarks;
   }

   /**
    * Returns if this flight counts towards promotion in a particular equipment type program.
    * @return the equipment type program name(s), or an empty Collection if this flight does not count
    */
   public Collection getCaptEQType() {
      return _captEQType;
   }

   /**
    * Returns the date this flight was flown.
    * @return the date of this flight; the time component is undefined
    * @see FlightReport#setDate(Date)
    */
   public Date getDate() {
      return _date;
   }

   /**
    * The date/time this Flight Report was created.
    * @return the creation date/time of this PIREP.
    * @see FlightReport#setCreatedOn(Date)
    */
   public Date getCreatedOn() {
      return _createdOn;
   }

   /**
    * The date/time this Flight Report was submitted for approval.
    * @return the submission date/time of this PIREP, null if never submitted.
    * @see FlightReport#setSubmittedOn(Date)
    */
   public Date getSubmittedOn() {
      return _submittedOn;
   }

   /**
    * The date/time this Flight Report was disposed on. (approved or rejected)
    * @return the approval/rejected date/time of this PIREP, null if not disposed of
    * @see FlightReport#setDisposedOn(Date)
    */
   public Date getDisposedOn() {
      return _disposedOn;
   }

   /**
    * Sets the database row ID of a relatied database row. This row may be present in the <i>PILOTS </i>, <i>ASSIGNMENTS
    * </i>, <i>ACARS_PIREPS </i> or <i>EVENTS </i> table. <i>This is typically used by a DAO </i>
    * @param idType the datbase row ID type
    * @return the database row ID, or 0 if not found
    * @throws NullPointerException if idType is null
    * @see FlightReport#setDatabaseID(String, int)
    */
   public int getDatabaseID(String idType) {
      Integer dbID = (Integer) _dbIds.get(idType);
      return (dbID == null) ? 0 : dbID.intValue();
   }

   /**
    * The Flight Simulator version used for this Flight.
    * @return the version number
    * @see FlightReport#setFSVersion(int)
    * @see FlightReport#setFSVersion(String)
    */
   public int getFSVersion() {
      return _fsVersion;
   }

   /**
    * Returns the attributes for this Flight Report.
    * @return a bit-mask containing this Flight Report's attributes
    * @see FlightReport#setAttributes(int)
    * @see FlightReport#setAttribute(int, boolean)
    */
   public int getAttributes() {
      return _attr;
   }

   /**
    * Returns the status of this Flight Report.
    * @return the status of this PIREP
    * @see FlightReport#setStatus(int)
    * @see FlightReport#setStatus(String)
    */
   public int getStatus() {
      return _status;
   }

   public String getStatusName() {
      return STATUS[getStatus()];
   }

   /**
    * Returns the presence of a particular flight attribute.
    * @param attrMask the attribute to check
    * @return TRUE if the attribute is present
    * @see FlightReport#getAttributes()
    * @see FlightReport#setAttributes(int)
    * @see FlightReport#setAttribute(int, boolean)
    */
   public boolean hasAttribute(int attrMask) {
      return ((getAttributes() & attrMask) != 0);
   }

   /**
    * Updates the first name of the Pilot filing this report.
    * @param fn the first Name
    * @see FlightReport#getFirstName()
    * @see Person#getFirstName()
    */
   public void setFirstName(String fn) {
      _firstName = fn;
   }

   /**
    * Updates the last name of the Pilot filing this report.
    * @param ln the last name
    * @see FlightReport#getLastName()
    * @see Person#getLastName()
    */
   public void setLastName(String ln) {
      _lastName = ln;
   }

   /**
    * Updates the rank of the Pilot filing this report.
    * @param rank the rank
    * @see FlightReport#getRank()
    * @see Person#getRank()
    */
   public void setRank(String rank) {
      _rank = rank;
   }

   /**
    * Sets the remarks for this Flight Report.
    * @param remarks the remarks
    * @see FlightReport#getRemarks()
    */
   public void setRemarks(String remarks) {
      _remarks = remarks;
   }

   /**
    * Sets if this Flight counts towards promotion in a particular equipment program.
    * @param eqTypes a Collection of equipment program names
    */
   public void setCaptEQType(Collection eqTypes) {
	   _captEQType.clear();
      _captEQType.addAll(eqTypes);
   }

   /**
    * Sets the Flight Leg. Overrides the superclass implementation to check for zero values.
    * @param leg the Flight Leg
    * @throws IllegalArgumentException if leg is zero or negative
    */
   public final void setLeg(int leg) {
      if (leg == 0) throw new IllegalArgumentException("Flight Leg cannot be zero or negative");

      super.setLeg(leg);
   }

   /**
    * Sets the length of this Flight, in <i>hours multiplied by 10</i>.
    * @param length the length of the flight, in <i>hours multiplied by 10</i>.
    * @throws IllegalArgumentException if length < 0 or > 180
    */
   public void setLength(int length) {
      if ((length < 0) || (length > 180))
            throw new IllegalArgumentException("Flight Length cannot be negative or > 18 hours - " + length);

      _length = length;
   }

   /**
    * Sets the status code of this Flight Report.
    * @param status the new Status Code for this Flight Report
    * @throws IllegalArgumentException if the status code is negative or invalid
    * @see FlightReport#setStatus(String)
    * @see FlightReport#getStatus()
    */
   public void setStatus(int status) {
      if ((status < 0) || (status >= FlightReport.STATUS.length))
            throw new IllegalArgumentException("Invalid PIREP status - " + status);

      _status = status;
   }

   /**
    * Sets the status of this Flight Report.
    * @param status the new Status for this Flight Report
    * @throws IllegalArgumentException if the status code is not in Flight.STATUS
    * @see FlightReport#setStatus(int)
    * @see FlightReport#getStatus()
    */
   public void setStatus(String status) {
      for (int x = 0; x < FlightReport.STATUS.length; x++) {
         if (FlightReport.STATUS[x].equals(status)) {
            setStatus(x);
            return;
         }
      }

      throw new IllegalArgumentException("Invalid PIREP status - " + status);
   }

   /**
    * Sets the attributes for this Flight Report.
    * @param attrs the new attributes
    * @see FlightReport#setAttribute(int, boolean)
    * @see FlightReport#getAttributes()
    */
   public void setAttributes(int attrs) {
      _attr = attrs;
   }

   /**
    * Set/Clear a particular attribute for this Flight Report.
    * @param attrMask the Attribute Mask to set
    * @param isSet TRUE if attribute should be set, FALSE if attribute should be cleared
    * @see FlightReport#setAttributes(int)
    * @see FlightReport#getAttributes()
    */
   public void setAttribute(int attrMask, boolean isSet) {
      _attr = (isSet) ? (_attr | attrMask) : (_attr & (~attrMask));
   }

   /**
    * Set the Flight Simulator version used for this flight.
    * @param version the Flight Simulator version code as found in FlightConstants.FSVERSION_CODE[]
    * @throws IllegalArgumentException if the version code cannot be found
    * @see FlightReport#setFSVersion(String)
    * @see FlightReport#getFSVersion()
    * @see FlightReport#FSVERSION_CODE
    */
   public void setFSVersion(int version) {
      for (int x = 0; x < FlightReport.FSVERSION_CODE.length; x++) {
         if (version == FlightReport.FSVERSION_CODE[x]) {
            _fsVersion = version;
            return;
         }
      }

      throw new IllegalArgumentException("Invalid Flight Simulator version - " + version);
   }

   /**
    * Set the Flight Simulator version used for this flight.
    * @param version the Flight Simulator version as found in FlightReport.FSVERSION[]
    * @throws IllegalArgumentException if the version cannot be found
    * @see FlightReport#setFSVersion(int)
    * @see FlightReport#getFSVersion()
    */
   public void setFSVersion(String version) {
      for (int x = 0; x < FlightReport.FSVERSION.length; x++) {
         if (FlightReport.FSVERSION[x].equals(version)) {
            _fsVersion = FlightReport.FSVERSION_CODE[x];
            return;
         }
      }

      throw new IllegalArgumentException("Invalid Flight Simulator version - " + version);
   }

   /**
    * Updates the date/time this Flight Report was created on.
    * @param cd when this Flight Report was created
    * @see FlightReport#getCreatedOn()
    */
   public void setCreatedOn(Date cd) {
      _createdOn = cd;
   }

   /**
    * Updates the date that this Flight was flown on.
    * @param dt when this flight was flown. The time component is undefined.
    * @see FlightReport#getDate()
    */
   public void setDate(Date dt) {
      _date = dt;
   }

   /**
    * Updates the date/time this Flight Report was submitted on.
    * @param sd this Flight Report was submitted for approval.
    * @see FlightReport#getSubmittedOn()
    */
   public void setSubmittedOn(Date sd) {
      _submittedOn = sd;
   }

   /**
    * Updates the date/time this Flight Report was approved or rejected on.
    * @param dd when this Flight Report was approved or rejected.
    * @see FlightReport#getDisposedOn()
    */
   public void setDisposedOn(Date dd) {
      _disposedOn = dd;
   }

   /**
    * Sets the database row ID of a relatied database row. This row may be present in the <i>PILOTS </i>, <i>ASSIGNMENTS
    * </i>, <i>ACARS_PIREPS </i> or <i>EVENTS </i> table. <i>This is typically used by a DAO </i>
    * @param idType the row ID type
    * @param id the database row ID
    * @throws NullPointerException if the idType is null
    * @throws IllegalArgumentException if the id is negative
    * @see FlightReport#getDatabaseID(String)
    */
   public void setDatabaseID(String idType, int id) {
      if (idType == null) {
         throw new NullPointerException("Database ID type cannot be null");
      } else if (id < 0) {
         throw new IllegalArgumentException(idType + " Datbase ID cannot be negative");
      }

      _dbIds.put(idType, new Integer(id));
   }

   /**
    * Compare two Flight Reports by comparing their creation date/time.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(FlightReport fr2) {
      return _createdOn.compareTo(fr2.getCreatedOn());
   }

   /**
    * Selects a table row class based upon the Flight Report status.
    * @return the row CSS class name
    */
   public String getRowClassName() {
      return hasAttribute(ATTR_CHECKRIDE) ? "opt3" : ROW_CLASSES[_status];
   }
}