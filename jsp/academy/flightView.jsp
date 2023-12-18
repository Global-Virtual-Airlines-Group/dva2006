<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Academy Flight Report</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<c:set var="pilot" value="${pilots[flight.pilotID]}" scope="page" />
<c:set var="ins" value="${pilots[flight.instructorID]}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title">
 <td class="caps" colspan="2">FLIGHT ACADEMY FLIGHT FLOWN ON <fmt:date fmt="d" date="${flight.date}" /> 
by ${pilot.name}</td>
</tr>

<!-- PIREP Data -->
<tr>
 <td class="label">Pilot Code / Rank</td>
 <td class="data">${pilot.pilotCode} (${pilot.rank.name}, ${pilot.equipmentType})</td>
</tr>
<content:filter roles="Instructor,HR,PIREP">
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
</tr>
</content:filter>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data">${flight.equipmentType}</td>
</tr>
<tr>
 <td class="label">Instructor</td>
 <td class="data">${ins.name} (${ins.pilotCode})</td>
</tr>
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><fmt:dec value="${flight.length / 10.0}" /> hours</td>
</tr>
<c:if test="${!empty flight.comments}">
<tr>
 <td class="label">Comments</td>
 <td class="data"><fmt:text value="${flight.comments}" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="insflight" link="${flight}" op="edit" label="EDIT FLIGHT REPORT" />
 <el:cmdbutton url="profile" link="${pilot}" label="VIEW PILOT PROFILE" /></td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
