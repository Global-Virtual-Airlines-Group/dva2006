// Copyright 2005, 2006, 2008 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.deltava.beans.schedule.Airport;
import org.deltava.beans.schedule.ScheduleEntry;

import org.deltava.commands.*;

import org.deltava.dao.GetSchedule;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site command to export Flight Schedule data in CSV format.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class ScheduleExportCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Check our access level
      ScheduleAccessControl access = new ScheduleAccessControl(ctx);
      access.validate();
      if (!access.getCanExport())
         throw securityException("Cannot export Flight Schedule");

      Collection<ScheduleEntry> results = null;
      try {
         // Get the DAO and export the schedule
         GetSchedule dao = new GetSchedule(ctx.getConnection());
         results = dao.export();
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Get the airline code
      String aCode = SystemData.get("airline.code");

      // Set the content type and force Save As
      ctx.getResponse().setBufferSize(40960);
      ctx.getResponse().setContentType("text/csv");
      ctx.getResponse().setHeader("Content-disposition", "attachment; filename=" + aCode.toLowerCase() + "_schedule.csv");

      // Get the airport code type
      boolean doICAO = (ctx.getUser().getAirportCodeType() == Airport.ICAO);

      try {
         // Write the header
         PrintWriter out = ctx.getResponse().getWriter();
         out.println("; " + aCode + " Flight Schedule - exported on " + StringUtils.format(new Date(), "MM/dd/yyyy HH:mm:ss"));
         out.println("AIRLINE,NUMBER,LEG,EQTYPE,FROM,DTIME,TO,ATIME,DISTANCE,HISTORIC,PURGE");

         // Loop through the results
         for (Iterator<ScheduleEntry> i = results.iterator(); i.hasNext();) {
            ScheduleEntry entry = i.next();
            StringBuilder buf = new StringBuilder(entry.getAirline().getCode());
            buf.append(',');
            buf.append(StringUtils.format(entry.getFlightNumber(), "#000"));
            buf.append(',');
            buf.append(String.valueOf(entry.getLeg()));
            buf.append(',');
            buf.append(entry.getEquipmentType());
            buf.append(',');
            buf.append(doICAO ? entry.getAirportD().getICAO() : entry.getAirportD().getIATA());
            buf.append(',');
            buf.append(StringUtils.format(entry.getTimeD(), "HH:mm"));
            buf.append(',');
            buf.append(doICAO ? entry.getAirportA().getICAO() : entry.getAirportA().getIATA());
            buf.append(',');
            buf.append(StringUtils.format(entry.getTimeA(), "HH:mm"));
            buf.append(',');
            buf.append(String.valueOf(entry.getDistance()));
            buf.append(',');
            buf.append(String.valueOf(entry.getHistoric()));
            buf.append(',');
            buf.append(String.valueOf(entry.getCanPurge()));

            // Write the entry
            out.println(buf.toString());
         }
      } catch (IOException ie) {
         throw new CommandException(ie);
      }

      // Set the result code
      CommandResult result = ctx.getResult();
      result.setType(ResultType.HTTPCODE);
      result.setHttpCode(HttpServletResponse.SC_OK);
      result.setSuccess(true);
   }
}