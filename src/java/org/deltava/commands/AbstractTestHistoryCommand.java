// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.EquipmentType;
import org.deltava.beans.testing.TestingHistoryHelper;

import org.deltava.dao.*;

/**
 * A class to support Web Site Commands use a {@link TestingHistoryHelper} object to determine
 * what examinations/transfers a Pilot is eligible for.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractTestHistoryCommand extends AbstractCommand {

   protected TestingHistoryHelper _testHistory;
   
   /**
    * Populates the Testing History Helper by calling the proper DAOs in the right order.
    * @param p the Pilot bean
    * @param c the JDBC connection to use
    * @throws DAOException if a JDBC error occurs
    */
   protected final void initTestHistory(Pilot p, Connection c) throws DAOException {
      
      // Load the PIREP beans if they haven't been loaded already
      if (!p.isPopulated()) {
         GetFlightReports frdao = new GetFlightReports(c);
         p.setFlights(frdao.getByPilot(p.getID(), null));
      }
      
      // Get the Pilot's equipment program
      GetEquipmentType eqdao = new GetEquipmentType(c);
      EquipmentType eq = eqdao.get(p.getEquipmentType());
      
      // Get the Pilot's examinations and check rides, and initialize the helper
      GetExam exdao = new GetExam(c);
      _testHistory = new TestingHistoryHelper(p, eq, exdao.getExams(p.getID()));
   }
}