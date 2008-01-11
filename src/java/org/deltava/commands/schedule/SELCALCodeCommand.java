// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.SelectCall;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.SELCALAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display aircraft SELCAL codes.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class SELCALCodeCommand extends AbstractViewCommand {
	
	private static final String[] SORT_CODES = {"SC.CODE", "SC.EQTYPE", "SC.AIRCRAFT", "PNAME DESC", "RESERVED"}; 
	private static final List SORT_OPTIONS = ComboUtils.fromArray(new String[] {"SELCAL Code", "Equipment Type",
			"Registration Code", "Pilot Name", "My Reserved Codes"}, SORT_CODES);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vc = initView(ctx);
		vc.setSortType(ctx.getParameter("sortType"));
		if (StringUtils.arrayIndexOf(SORT_CODES, vc.getSortType()) == -1)
			vc.setSortType(SORT_CODES[0]);
		
		// Get the equipment type
		String eqType = ctx.getParameter("eqType");
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the parameters
			GetSELCAL dao = new GetSELCAL(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Pull up the SELCAL codes
			Collection<SelectCall> codes = null;
			if (SORT_CODES[4].equals(vc.getSortType()))
				codes = dao.getReserved(ctx.getUser().getID());
			else if (eqType != null)
				codes = dao.getByEquipmentType(eqType);
			else 
				codes = dao.getCodes(vc.getSortType());
			
			// Save codes and equipment types
			vc.setResults(codes);
			ctx.setAttribute("eqTypes", dao.getEquipmentTypes(), REQUEST);
			
			// Check if we can reserve new codes
			int maxCodes = SystemData.getInt("users.selcal.max", 2);
			int usrCodes = dao.getReserved(ctx.getUser().getID()).size();
			
			// Get the release dates
			int releaseDays = SystemData.getInt("users.selcal.reserve", 10);
			Map<String, Date> releaseDates = new HashMap<String, Date>();
			
			// Calculate the access control
			Map<String, SELCALAccessControl> access = new HashMap<String, SELCALAccessControl>();
			for (Iterator<SelectCall> i = codes.iterator(); i.hasNext(); ) {
				SelectCall sc = i.next();
				SELCALAccessControl ac = new SELCALAccessControl(ctx, sc);
				if (usrCodes >= maxCodes)
					ac.markUnavailable();
				
				ac.validate();
				access.put(sc.getCode(), ac);
				
				// Get the release dates
				if (sc.getReservedOn() != null)
					releaseDates.put(sc.getCode(), CalendarUtils.adjust(sc.getReservedOn(), releaseDays));
			}
			
			// Save the access control map
			ctx.setAttribute("accessMap", access, REQUEST);
			ctx.setAttribute("releaseDates", releaseDates, REQUEST);
			
			// Load the Pilots
			Collection<Integer> ids = new HashSet<Integer>();
			for (Iterator<SelectCall> i = codes.iterator(); i.hasNext(); ) {
				SelectCall sc = i.next();
				if (sc.getReservedBy() != 0)
					ids.add(new Integer(sc.getReservedBy()));
			}
			
			// Get the Pilot DAO and the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(ids, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the sort options
		ctx.setAttribute("sortOptions", SORT_OPTIONS, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/selcal.jsp");
		result.setSuccess(true);
	}
}