<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Search</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

hasFirst = (form.firstName.value.length > 2);
hasLast = (form.lastName.value.length > 2);
hasCode = (form.pilotCode.value.length > 1);
hasEMail = (form.eMail.value.length > 5);
if (hasFirst || hasLast || hasCode || hasEMail) {
	setSubmit();
	disableButton('SearchButton');
	return true;
}
	
alert('Please provide a First or Last Name, Pilot Code, or an e-mail address.');
form.firstName.focus();
return false;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="pilotsearch.do" method="POST" validate="return validate(this)">
<!-- Search Criteria -->
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">PILOT SEARCH</td>
</tr>
<tr>
 <td class="label">Pilot Code</td>
 <td class="data"><el:text name="pilotCode" idx="*" size="7" max="8" value="${param.pilotCode}" /><br />
<span class="small"><b>NOTE:</b> Pilot Code searches override Name / E-Mail criteria.</span></td>
</tr>
<tr>
 <td class="label">First / Last Names</td>
 <td class="data"><el:text name="firstName" idx="*" size="12" max="24" value="${param.firstName}" />
&nbsp;<el:text name="lastName" idx="*" size="18" max="36" value="${param.lastName}" /></td>
</tr>
<content:filter roles="HR,Examination,PIREP">
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="eMail" idx="*" size="24" max="64" value="${param.eMail}" /></td>
</tr>
</content:filter>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data sec small"><el:box name="exactMatch" idx="*" value="1" label="Exact Matches only" checked="${param.exactMatch == '1'}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="SUBMIT" className="BUTTON" label="PILOT SEARCH" /></td>
</tr>
</el:table>

<!-- Search Results -->
<c:if test="${!noResults}">
<view:table className="view" pad="default" space="default" cmd="">
<tr class="title caps">
 <td colspan="7" class="left">SEARCH RESULTS</td>
</tr>

<c:choose>
<c:when test="${empty results}">
<!-- No Pilots Found -->
<tr>
 <td colspan="7" class="pri bld left">No Pilots matching your search criteria were found.</td>
</tr>
</c:when>
<c:otherwise>
<!-- Table Header Bar-->
<tr class="title">
 <td width="15%">&nbsp;</td>
 <td width="30%">PILOT NAME</td>
 <td width="10%">PILOT ID</td>
 <td width="10%">JOINED ON</td>
 <td width="10%">LAST FLIGHT</td>
 <td width="10%">FLIGHTS</td>
 <td>HOURS</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${results}">
<c:set var="access" value="${accessMap[pilot.ID]}" scope="request" />
<view:row entry="${pilot}">
<c:choose>
<c:when test="${access.canActivate}">
 <td><el:cmdbutton url="activate" linkID="0x${pilot.ID}" label="ACTIVATE PILOT" /></td>
</c:when>
<c:when test="${access.canChangeSignature}">
 <td><el:cmdbutton url="sigupdate" linkID="0x${pilot.ID}" label="EDIT SIGNATURE" /></td>
</c:when>
<c:otherwise>
 <td>&nbsp;</td>
</c:otherwise>
</c:choose>
 <td class="pri bld"><el:cmd url="profile" linkID="0x${pilot.ID}">${pilot.name}</el:cmd></td>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
<c:if test="${pilot.legs > 0}">
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" /></td>
 <td class="bld"><el:cmd url="logbook" op="log" linkID="0x${pilot.ID}"><fmt:int value="${pilot.legs}" /></el:cmd></td>
 <td class="pri bld"><el:cmd url="logbook" op="log" linkID="0x${pilot.ID}"><fmt:dec value="${pilot.hours}" /></el:cmd></td>
</c:if>
<c:if test="${pilot.legs == 0}">
 <td colspan="3" class="bld">NO FLIGHTS LOGGED</td>
</c:if>
</view:row>
</c:forEach>

<!-- Table Legend Bar -->
<tr class="title">
 <td colspan="7"><view:legend width="100" labels="Active,Inactive,Retired,On Leave" classes=" ,opt2,opt3,warn" /></td>
</tr>
</c:otherwise>
</c:choose>
</view:table>
</c:if>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
