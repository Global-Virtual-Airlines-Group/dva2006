// Copyright 2006, 2009, 2010, 2011, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.beans.EquipmentType;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CertificationAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view and update Flight Academy certification profiles.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class CertificationCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Get the certification name
		String name = (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();
			
			// Check our access
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			
			// If we're saving an existing cert, get it
			Certification cert = null;
			if (name != null) {
				GetAcademyCertifications dao = new GetAcademyCertifications(con);
				cert = dao.get(name);
				if (cert == null)
					throw notFoundException("Unknown Certification - " + name);

				// Check our access
				if (!access.getCanEdit())
					throw securityException("Cannot edit Certification");
				
				cert.setName(ctx.getParameter("name"));
			} else {
				// Check our access
				if (!access.getCanCreate())
					throw securityException("Cannot create Certification");

				cert = new Certification(ctx.getParameter("name"));
			}
			
			// Update from the request
			cert.setCode(ctx.getParameter("code"));
			cert.setStage(StringUtils.parse(ctx.getParameter("stage"), 1));
			cert.setReqs(StringUtils.arrayIndexOf(Certification.REQ_NAMES, ctx.getParameter("preReqs")));
			cert.setActive(Boolean.valueOf(ctx.getParameter("isActive")).booleanValue());
			cert.setAutoEnroll(Boolean.valueOf(ctx.getParameter("autoEnroll")).booleanValue());
			cert.setVisible(Boolean.valueOf(ctx.getParameter("visible")).booleanValue());
			cert.setRideCount(StringUtils.parse(ctx.getParameter("rideCount"), 0));
			cert.setReqCert((cert.getReqs() != Certification.REQ_SPECIFIC) ? null : ctx.getParameter("reqCert"));
			cert.setDescription(ctx.getParameter("desc"));
			cert.setRoles(ctx.getParameters("enrollRoles"));
			cert.setExams(ctx.getParameters("reqExams"));
			cert.setRideEQ(ctx.getParameters("rideEQ"));
			cert.setNetwork(OnlineNetwork.fromName(ctx.getParameter("network")));
			cert.setNetworkRatingCode((cert.getNetwork() == null) ? null : ctx.getParameter("ratingCode"));
			if ((cert.getReqs() == Certification.REQ_FLIGHTS) || (cert.getReqs() == Certification.REQ_HOURS)) {
				cert.setFlightCount(StringUtils.parse(ctx.getParameter("flightCount"), 1));
				String eqProgram = ctx.getParameter("eqProgram");
				cert.setEquipmentProgram(eqProgram.startsWith("[") ? null : eqProgram);
			} else {
				cert.setFlightCount(0);
				cert.setEquipmentProgram(null);
			}
			
			// Load apps
			cert.getAirlines().clear();
			for (String appName : ctx.getParameters("airlines"))
				cert.addAirline(SystemData.getApp(appName));
			
			// Make sure that each requirement with an exam remains valid
			for (CertificationRequirement req : cert.getRequirements()) {
				if ((req.getExamName() != null) && !cert.getExamNames().contains(req.getExamName()))
					req.setExamName(null);
			}
			
			// Get the write DAO and save the certification
			SetAcademyCertification wdao = new SetAcademyCertification(con);
			if (name != null)
				wdao.update(cert, name);
			else
				wdao.write(cert);
			
			// Save the certification in the request
			ctx.setAttribute("cert", cert, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
		ctx.setAttribute("isNew", Boolean.valueOf(name == null), REQUEST);
		
		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/academy/certUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Check our access
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			
			// Get the DAO and all certifications
			GetAcademyCertifications dao = new GetAcademyCertifications(con);
			Map<String, Certification> allCerts = CollectionUtils.createMap(dao.getAll(), "code");
			
			// Get the certification
			String name = (String) ctx.getCmdParameter(ID, null);
			if (name != null) {
				Certification cert = dao.get(name);
				if (cert == null)
					throw notFoundException("Unknown Certification - " + name);
				
				// Check our access
				if (!access.getCanEdit())
					throw securityException("Cannot edit Certification");
				
				ctx.setAttribute("cert", cert, REQUEST);
				allCerts.remove(cert.getCode());
			} else if (!access.getCanCreate())
				throw securityException("Cannot create Certification");
			
			// Load all certification names
			ctx.setAttribute("allCerts", new TreeSet<Certification>(allCerts.values()), REQUEST);
			
			// Get all equipment type profiles
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAll(), REQUEST);
			
			// Get all equipment type programs
			Collection<EquipmentType> eqPrograms = new TreeSet<EquipmentType>();
			GetEquipmentType eqdao = new GetEquipmentType(con);
			for (AirlineInformation ai : SystemData.getApps())
				eqPrograms.addAll(eqdao.getActive(ai.getDB()));
			
			ctx.setAttribute("allPrograms", eqPrograms, REQUEST);
			
			// Get available examinations
			GetExamProfiles exdao = new GetExamProfiles(con);
			ctx.setAttribute("exams", exdao.getExamProfiles(), REQUEST); 
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save prerequisite choices
		ctx.setAttribute("preReqNames", ComboUtils.fromArray(Certification.REQ_NAMES), REQUEST);
		
		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/certEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Certification
			GetAcademyCertifications dao = new GetAcademyCertifications(con);
			String name = (String) ctx.getCmdParameter(ID, null);
			Certification cert = dao.get(name);
			if (cert == null)
				throw notFoundException("Unknown Certification - " + name);

			// Check our access - this'll blow up if we cannot view
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			
			// Check if we have check ride scripts
			Collection<Integer> missingScripts = new TreeSet<Integer>();
			Collection<AcademyRideScript> crScripts = new ArrayList<AcademyRideScript>(); 
			for (int x = 1; x <= cert.getRideCount(); x++) {
				AcademyRideScript sc = dao.getScript(new AcademyRideID(cert.getName(), x));
				if (sc == null)
					missingScripts.add(Integer.valueOf(x));
				else
					crScripts.add(sc);
			}
				
			// If we have a specific pre-req, load it as well
			if (cert.getReqs() == Certification.REQ_SPECIFIC)
				ctx.setAttribute("preReqCert", dao.get(cert.getReqCert()), REQUEST);
			
			// If we have a specific program for minimum flight hours, load it as well
			if (!StringUtils.isEmpty(cert.getEquipmentProgram()) && ((cert.getReqs() == Certification.REQ_FLIGHTS) || (cert.getReqs() == Certification.REQ_HOURS))) {
				GetEquipmentType eqdao = new GetEquipmentType(con);
				ctx.setAttribute("eqProgram", eqdao.get(cert.getEquipmentProgram()), REQUEST);
			}
			
			// Get associated documents
			GetDocuments ddao = new GetDocuments(con);
			ctx.setAttribute("docs", ddao.getByCertification(SystemData.get("airline.db"), cert.getCode()), REQUEST);

			// Save in the request
			ctx.setAttribute("cert", cert, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("missingScripts", missingScripts, REQUEST);
			ctx.setAttribute("crScripts", crScripts, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/certView.jsp");
		result.setSuccess(true);
	}
}