<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Assignments</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function setEQType(combo)
{
var f = document.forms[0];
var st = f.status.options[f.status.selectedIndex].text;
self.location = '/assignments.do?eqType=' + combo.options[combo.selectedIndex].text + '&status=' + st;
return true;
}

function setStatus(combo)
{
var f = document.forms[0];
var eqCombo = f.eqType;
var eq = (eqCombo.selectedIndex < 1) ? null : eqCombo.options[eqCombo.selectedIndex].text;
self.location = '/assignments.do?status=' + combo.options[combo.selectedIndex].text + (eq == null ? '' : '&eqType=' + eq);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="assignments.do" method="GET" validate="return false">
<view:table className="view" space="default" pad="default" cmd="assignments">
<!-- Table Select Bar -->
<tr class="title">
 <td colspan="2" class="left caps">FLIGHT ASSIGNMENTS</td>
 <td colspan="4" class="right"><el:cmd url="myassign">MY ASSIGNMENTS</el:cmd>&nbsp;|&nbsp;EQUIPMENT TYPE&nbsp;
<el:combo name="eqType" idx="*" size="1" options="${eqTypes}" firstEntry="-" value="${param.eqType}" onChange="void setEQType(this)" />
&nbsp;|&nbsp;STATUS&nbsp;<el:combo name="status" idx="*" size="1" options="${statuses}" firstEntry="-" value="${param.status}" onChange="void setStatus(this)" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="15%">&nbsp;</td>
 <td width="30%">PILOT NAME</td>
 <td width="15%">EQUIPMENT</td>
 <td width="10%">SIZE</td>
 <td width="15%">ASSIGNED ON</td>
 <td>COMPLETED ON</td>
</tr>

<!-- Table Assignment Data -->
<c:set var="idx" value="${-1}" scope="request" />
<c:forEach var="assign" items="${viewContext.results}">
<c:set var="idx" value="${idx + 1}" scope="request" />
<c:set var="pilot" value="${pilots[assign.pilotID]}" scope="request" />
<c:set var="access" value="${fn:get(accessList, idx)}" scope="request" />
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
</body>
</html>
