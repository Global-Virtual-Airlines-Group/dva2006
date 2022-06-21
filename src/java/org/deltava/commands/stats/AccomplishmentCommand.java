// Copyright 2010, 2011, 2015, 2016, 2017, 2019, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.AuditLog;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.CountryComparator;

import org.deltava.security.command.AccomplishmentAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Accomplishment profiles. 
 * @author Luke
 * @version 10.2
 * @since 3.2
 */

public class AccomplishmentCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving the Accomplishment.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Check if we're updating
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			Accomplishment a = null; Accomplishment oa = null;	
			if (!isNew) {
				GetAccomplishment dao = new GetAccomplishment(con);
				a = dao.get(ctx.getID());
				if (a == null)
					throw notFoundException("Invalid Accomplishment - " + ctx.getID());
				
				oa = BeanUtils.clone(a);
				a.setName(ctx.getParameter("name"));
			} else
				a = new Accomplishment(ctx.getParameter("name"));
			
			// Check our access
			AccomplishmentAccessControl ac = new AccomplishmentAccessControl(ctx, a);
			ac.validate();
			boolean canExec = isNew ? ac.getCanCreate() : ac.getCanEdit();
			if (!canExec)
				throw securityException("Cannot " + (isNew ? "create" : "edit") + " Accomplishment profile");
			
			// Update fields
			a.setActive(Boolean.parseBoolean(ctx.getParameter("active")));
			a.setAlwaysDisplay(Boolean.parseBoolean(ctx.getParameter("alwaysDisplay")));
			a.setUnit(AccomplishUnit.valueOf(ctx.getParameter("units")));
			a.setColor(StringUtils.parse("0x" + ctx.getParameter("color"), 0));
			
			// Get choices
			switch (a.getUnit()) {
			case COUNTRIES:
				a.setChoices(StringUtils.nullTrim(ctx.getParameters("countries")));
				break;
				
			case CONTINENTS:
				a.setChoices(StringUtils.nullTrim(ctx.getParameters("continents")));
				break;

			case STATES:
				a.setChoices(StringUtils.nullTrim(ctx.getParameters("states")));
				break;
				
			case AIRLINES:
				a.setChoices(StringUtils.nullTrim(ctx.getParameters("airlines")));
				break;
				
			case AIRCRAFT:
			case EQLEGS:
				a.setChoices(StringUtils.nullTrim(ctx.getParameters("eqTypes")));
				break;
				
			case PROMOLEGS:
				a.setChoices(StringUtils.nullTrim(ctx.getParameters("eqPrograms")));
				break;
				
			case AIRPORTS:
			case AIRPORTD:
			case AIRPORTA:
				Collection<String> iataCodes = new TreeSet<String>(); Collection<String> badCodes = new LinkedHashSet<String>();
				StringTokenizer tk = new StringTokenizer(ctx.getParameter("choices"), ",\n");
				while (tk.hasMoreElements()) {
					String c = tk.nextToken().trim();
					Airport ap = SystemData.getAirport(c);
					if (ap != null)
						iataCodes.add(ap.getIATA());
					else
						badCodes.add(c);
				}
				
				a.setChoices(iataCodes);
				ctx.setAttribute("invalidAirports", badCodes, REQUEST);
				break;
				
			default:
				a.setChoices(StringUtils.nullTrim(StringUtils.split(ctx.getParameter("choices"), ",")));
			}
			
			// Get count/all
			boolean doAll = Boolean.parseBoolean(ctx.getParameter("doAll"));
			doAll &= (a.getUnit() != AccomplishUnit.ADLEGS) && (a.getUnit() != AccomplishUnit.AALEGS);
			a.setValue(doAll ? a.getChoices().size() : StringUtils.parse(ctx.getParameter("value"), 0));
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oa, a);
			AuditLog ae = AuditLog.create(a, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Write the accomplishment
			SetAccomplishment wdao = new SetAccomplishment(con);
			wdao.write(a);
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();
			
			// Save status attributes
			ctx.setAttribute("ap", a, REQUEST);
			ctx.setAttribute("isNew", Boolean.valueOf(isNew), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/stats/accomplishmentUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Accomplishment.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get the command results
		CommandResult result = ctx.getResult();
		ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
		
		// Get the Accomplishment code - if we're new, check if the airport exists
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			GetAccomplishment dao = new GetAccomplishment(con);
			Accomplishment a = null;
			if (!isNew) {
				a = dao.get(ctx.getID());
				if (a == null)
					throw notFoundException("Invalid Accomplishment - " + ctx.getID());
			}
			
			// Check our security
			AccomplishmentAccessControl ac = new AccomplishmentAccessControl(ctx, a);
			ac.validate();
			boolean canExec = isNew ? ac.getCanCreate() : ac.getCanEdit();
			if (!canExec)
				throw securityException("Cannot create/edit Accomplishment profile");
			
			// Get the aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
			
			// Get equipment programs
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqPrograms", eqdao.getAll(), REQUEST);
			
			// Load all countries
			GetScheduleInfo sdao = new GetScheduleInfo(con);
			Comparator<Country> ccmp = new CountryComparator(CountryComparator.NAME);
			Collection<Country> activeCountries = CollectionUtils.sort(sdao.getCountries(), ccmp);
			Collection<Country> inactiveCountries = CollectionUtils.getDelta(Country.getAll(), activeCountries);
			ctx.setAttribute("inactiveCountries", CollectionUtils.sort(inactiveCountries, ccmp), REQUEST);
			ctx.setAttribute("activeCountries", activeCountries, REQUEST);
			
			// Save in request
			readAuditLog(ctx, a);
			ctx.setAttribute("ap", a, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/stats/accomplishmentEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Accomplishment. <i>NOT IMPLEMENTED - Edits the Accomplishment</i>
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}