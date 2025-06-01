<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Charter Flight Request</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:googleAnalytics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">CHARTER FLIGHT REQUEST #<fmt:int value="${chreq.ID}" /></td>
</tr>
<tr>
 <td class="label">Requested by</td>
 <td class="data"><el:cmd url="profile" link="${author}" className="pri bld">${author.name}</el:cmd><c:if test="${!empty author.pilotCode}" ><span class="bld"> (${author.pilotCode })</span></c:if> on <fmt:date date="${chreq.createdOn}" className="sec bld" /></td>
</tr>
<c:if test="${charterCount > 0}">
<tr>
 <td class="label">Charters Flown</td>
 <td class="data"><fmt:int value="${charterCount}" /> flights since <fmt:date date="${charterCountStart}" fmt="d" /></td>  
</tr>
</c:if>
<c:if test="${!empty chreq.disposedOn}">
<tr>
 <td class="label">Disposed on</td>
 <td class="data"><span class="bld">${disposedBy.name} (${disposedBy.pilotCode})</span> on <fmt:date date="${chreq.disposedOn}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Departing from</td>
 <td class="data">${chreq.airportD.name} (<el:cmd url="airportinfo" linkID="${chreq.airportD.IATA}" authOnly="true" className="plain"><fmt:airport airport="${chreq.airportD}" /></el:cmd>)</td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data">${chreq.airportA.name} (<el:cmd url="airportinfo" linkID="${chreq.airportA.IATA}" authOnly="true" className="plain"><fmt:airport airport="${chreq.airportA}" /></el:cmd>)</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data sec bld">${chreq.airline.name}</td>
</tr>
<content:defaultMethod var="dst" object="${chreq}" method="distance" />
<content:defaultMethod var="flightType" object="${chreq}" method="flightType" />
<tr>
 <td class="label">Flight Information</td>
 <td class="data"><fmt:distance value="${dst}" className="bld" />, Flight type: <span class="sec bld"><fmt:edesc object="${flightType}" /></span></td> 
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data">${chreq.equipmentType}<c:if test="${dst > opts.range}">&nbsp;<span class="warn bld caps">Flight Distance exceeds maximum Aircraft Range</span></c:if></td>
</tr>
<c:if test="${!empty chreq.comments}">
<tr>
 <td class="label top">Comments</td>
 <td class="data"><fmt:text value="${chreq.comments}" /></td>
</tr>
</c:if>
<c:if test="${!empty airlines}">
<tr>
 <td colspan="2" class="title caps mid caps"><span class="nophone">Route currently served by </span><fmt:list value="${airlines}" delim=", " /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}">&nbsp;<el:cmdbutton url="chreq" link="${chreq}" op="edit" label="EDIT CHARTER REQUEST" />&nbsp;<el:cmdbutton url="chreqdel" link="${chreq}" label="DELETE CHARTER REQUEST" /></c:if>
<c:if test="${access.canDispose}">&nbsp;<el:cmdbutton url="chreqdsp" link="${chreq}" op="approved" label="APPROVE CHARTER REQUEST" />&nbsp;<el:cmdbutton url="chreqdsp" link="${chreq}" op="rejected" label="REJECT CHARTER REQUEST" /></c:if>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
