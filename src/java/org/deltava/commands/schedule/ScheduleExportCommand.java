// Copyright 2005, 2006, 2008, 2010, 2015, 2016, 2019 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;
import java.time.Instant;
import java.time.format.*;

import javax.servlet.http.HttpServletResponse;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site command to export raw Flight Schedule data in CSV format.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ScheduleExportCommand extends AbstractCommand {

	/**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
	@Override
   public void execute(CommandContext ctx) throws CommandException {

      // Check our access level
      ScheduleAccessControl access = new ScheduleAccessControl(ctx);
      access.validate();
      if (!access.getCanExport())
         throw securityException("Cannot export Flight Schedule");
      
      // Get raw schedule entries
      Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
      try {
    	  GetRawSchedule dao = new GetRawSchedule(ctx.getConnection());
    	  Collection<ScheduleSourceInfo> srcs = dao.getSources();
    	  for (ScheduleSourceInfo si : srcs)
    		  results.addAll(dao.load(si.getSource(), null));
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set the content type and force Save As
      String aCode = SystemData.get("airline.code");
      ctx.getResponse().setContentType("text/csv");
      ctx.getResponse().setCharacterEncoding("utf-8");
      ctx.setHeader("Content-disposition", "attachment; filename=" + aCode.toLowerCase() + "_raw_schedule.csv");
      
      // Create formatters
      DateTimeFormatter df = new DateTimeFormatterBuilder().appendPattern("dd MMM").toFormatter();
      DateTimeFormatter tf = new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter();

      // Get the airport code type
      boolean doICAO = (ctx.getUser().getAirportCodeType() == Airport.Code.ICAO);
      try (PrintWriter out = ctx.getResponse().getWriter()) {
         // Write the header
         out.println("; " + aCode + " Flight Schedule - exported on " + StringUtils.format(Instant.now(), "MM/dd/yyyy HH:mm:ss") + " UTC");
         out.println("SOURCE,LINE,STARTS,ENDS,AIRLINE,NUMBER,LEG,EQTYPE,FROM,DTIME,TO,ATIME,DISTANCE,HISTORIC,PURGE");

         // Loop through the results
         for (RawScheduleEntry entry : results) {
            StringBuilder buf = new StringBuilder(entry.getSource().name());
            buf.append(',');
            buf.append(entry.getLineNumber());
            buf.append(',');
            buf.append(df.format(entry.getStartDate()));
            buf.append(',');
            buf.append(df.format(entry.getEndDate()));
            buf.append(',');
            buf.append(entry.getAirline().getCode());
            buf.append(',');
            buf.append(StringUtils.format(entry.getFlightNumber(), "#000"));
            buf.append(',');
            buf.append(String.valueOf(entry.getLeg()));
            buf.append(',');
            buf.append(entry.getEquipmentType());
            buf.append(',');
            buf.append(doICAO ? entry.getAirportD().getICAO() : entry.getAirportD().getIATA());
            buf.append(',');
            buf.append(tf.format(entry.getTimeD()));
            buf.append(',');
            buf.append(doICAO ? entry.getAirportA().getICAO() : entry.getAirportA().getIATA());
            buf.append(',');
            buf.append(tf.format(entry.getTimeA()));
            buf.append(',');
            buf.append(entry.getDistance());
            buf.append(',');
            buf.append(entry.getHistoric());
            buf.append(',');
            buf.append(entry.getCanPurge());
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