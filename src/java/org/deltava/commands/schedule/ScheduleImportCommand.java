// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;

import org.deltava.dao.SetSchedule;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to import Flight Schedule data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleImportCommand extends AbstractCommand {
   
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the command results
      CommandResult result = ctx.getResult();
      
		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Flight Schedule data");
      
      // If we are not uploading a CSV file, then redirect to the JSP
      FileUpload csvData = ctx.getFile("csvData");
      if (csvData == null) {
         result.setURL("/jsp/schedule/flightImport.jsp");
         result.setSuccess(true);
         return;
      }
      
      // Check if we are purging the schedule
      boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();

      DateFormat df = new SimpleDateFormat("hh:mm aa");
      Collection errors = new ArrayList();
      int entryCount = 0;
      try {
         Connection con = ctx.getConnection();
         
         // Get the write DAO
         SetSchedule dao = new SetSchedule(con);
         
         // Purge the schedule if needed
         if (doPurge)
            dao.purge(false);
         
			// Get the csv data - skipping the first line
			InputStream is = new ByteArrayInputStream(csvData.getBuffer());
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
			
			// Iterate through the file
			while (br.ready()) {
				String txtData = br.readLine();
				if ((!txtData.startsWith(";")) && (txtData.length() > 5)) {
				   try {
				      StringTokenizer tkns = new StringTokenizer(txtData, ",");
				      if (tkns.countTokens() != 11)
					      throw new ParseException("Invalid number of tokens, count=" + tkns.countTokens(), 0);
				      
				      // Get the airline
				      String aCode = tkns.nextToken();
				      Airline a = SystemData.getAirline(aCode);
				      if (a == null)
				         throw new ParseException("Invalid airline code - " + aCode, 0);
				      
				      // Build the flight number and equipment type
				      ScheduleEntry entry = new ScheduleEntry(a, Integer.parseInt(tkns.nextToken()),
				            Integer.parseInt(tkns.nextToken()));
				      entry.setEquipmentType(tkns.nextToken());
				      
				      // Get the airports and times
				      entry.setAirportD(SystemData.getAirport(tkns.nextToken()));
				      entry.setTimeD(df.parse(tkns.nextToken()));
				      entry.setAirportA(SystemData.getAirport(tkns.nextToken()));
				      entry.setTimeA(df.parse(tkns.nextToken()));
				      if ((entry.getAirportD() == null) || (entry.getAirportA() == null))
				         throw new ParseException("Invalid Airpot Code", 0);
				      
				      // Discard distance
				      tkns.nextToken();
				      
				      // Load historic/purgeable attributes
				      entry.setHistoric(Boolean.valueOf(tkns.nextToken()).booleanValue());
				      entry.setPurge(Boolean.valueOf(tkns.nextToken()).booleanValue());
				      
				      // Save the schedule entry
				      dao.create(entry);
				      entryCount++;
				   } catch (DAOException de) {
				      errors.add("Error saving entry on line " + br.getLineNumber() + " - " + de.getMessage());
				   } catch (Exception e) {
				      errors.add("Error on line " + br.getLineNumber() + " - " + e.getMessage());
				   }
				}
			}
      } catch (IOException ie) {
         throw new CommandException(ie);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Save error messages and entry count
      ctx.setAttribute("isFlights", Boolean.TRUE, REQUEST);
      ctx.setAttribute("entryCount", new Integer(entryCount), REQUEST);
      ctx.setAttribute("doPurge", Boolean.valueOf(doPurge), REQUEST);
      if (!errors.isEmpty())
         ctx.setAttribute("errors", errors, REQUEST);
      
      // Forward to the JSP
      result.setURL("/jsp/schedule/scheduleUpdate.jsp");
      result.setType(CommandResult.REQREDIRECT);
      result.setSuccess(true);
   }
}