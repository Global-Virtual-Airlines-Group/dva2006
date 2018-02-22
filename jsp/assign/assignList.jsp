<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Assignments</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script>
golgotha.local.setEQType = function(combo) {
	self.location = '/assignments.do?eqType=' + golgotha.form.getCombo(combo) + '&status=' + golgotha.form.getCombo(document.forms[0].status);
	return true;
};

golgotha.local.setStatus = function(combo) {
	var eq = golgotha.form.getCombo(document.forms[0].eqType);
	self.location = '/assignments.do?status=' + golgotha.form.getCombo(combo) + (eq == null ? '' : '&eqType=' + eq);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="statuses" className="org.deltava.beans.assign.AssignmentStatus" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="assignments.do" method="GET" validate="return false">
<view:table cmd="assignments">
<!-- Table Select Bar -->
<tr class="title">
 <td colspan="2" class="left caps"><content:airline /> FLIGHT ASSIGNMENTS</td>
 <td colspan="4" class="right"><el:cmd url="findflight">FIND FLIGHTS</el:cmd>&nbsp;|&nbsp;<el:cmd url="myassign">MY ASSIGNMENTS</el:cmd>&nbsp;|
&nbsp;EQUIPMENT <el:combo name="eqType" idx="*" size="1" options="${eqTypes}" firstEntry="-" value="${param.eqType}" onChange="void golgotha.local.setEQType(this)" />
&nbsp;|&nbsp;STATUS <el:combo name="status" idx="*" size="1" options="${statuses}" firstEntry="-" value="${param.status}" onChange="void golgotha.local.setStatus(this)" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:15%">&nbsp;</td>
 <td style="width:30%">PILOT NAME</td>
 <td style="width:15%">EQUIPMENT</td>
 <td style="width:10%">SIZE</td>
 <td style="width:15%">ASSIGNED ON</td>
 <td>COMPLETED ON</td>
</tr>

<!-- Table Assignment Data -->
<c:set var="idx" value="-1" scope="page" />
<c:forEach var="assign" items="${viewContext.results}">
<c:set var="idx" value="${idx + 1}" scope="page" />
<c:set var="pilot" value="${pilots[assign.pilotID]}" scope="page" />
<c:set var="access" value="${fn:get(accessList, idx)}" scope="page" />
<view:row entry="${assign}">
<td>
<c:if test="${!access.canReserve && !access.canRelease && !access.canDelete}">&nbsp;</c:if>
<c:if test="${access.canReserve}">
<el:cmdbutton url="assignreserve" link="${assign}" label="RESERVE" />
</c:if>
<c:if test="${access.canRelease}">
<el:cmdbutton url="assignrelease" link="${assign}" label="RELEASE" />
</c:if>
<c:if test="${access.canDelete}">
<el:cmdbutton url="assignDelete" link="${assign}" label="DELETE" />
</c:if>
</td>
<c:if test="${!empty pilot}">
 <td class="pri bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
</c:if>
<c:if test="${empty pilot}">
 <td class="pri bld">NOT ASSIGNED</td>
</c:if>
 <td class="sec bld">${assign.equipmentType}</td>
 <td><fmt:int value="${fn:sizeof(assign.assignments)}" /> flights</td>
 <td><fmt:date fmt="d" date="${assign.assignDate}" default="-" /></td>
 <td><fmt:date fmt="d" date="${assign.completionDate}" default="-" /></td>
</view:row>
<c:forEach var="leg" items="${assign.assignments}">
<tr>
 <td class="bld">${leg.flightCode}</td>
 <td colspan="5" class="left">${leg.airportD.name} (<fmt:airport airport="${leg.airportD}" />) to
${leg.airportA.name} (<fmt:airport airport="${leg.airportA}" />)</td>
</tr>
</c:forEach>
</c:forEach>

<!-- Scroll Bar Row -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="95" labels="Available,Assigned,Complete" classes=" ,opt2,opt3" /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
