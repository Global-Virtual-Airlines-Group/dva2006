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
<title><content:airline /> Duplicate Pilot Search</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

hasFirst = (form.firstName.value.length > 2) || (form.firstName2.value.length > 2);
hasLast = (form.lastName.value.length > 2) || (form.lastName2.value.length > 2);
if (hasFirst || hasLast) {
	setSubmit();
	disableButton('SearchButton');
	return true;
}
	
alert('Please provide at least one First or Last Name.');
form.firstName.focus();
return false;
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
<el:form action="dupesearch.do" method="post" validate="return validate(this)">
<!-- Search Criteria -->
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">DUPLICATE PILOT SEARCH</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="firstName" idx="*" size="12" max="24" value="${param.firstName}" />
&nbsp;<el:text name="lastName" idx="*" size="18" max="36" value="${param.lastName}" /></td>
</tr>
<tr>
 <td class="label">Additional First / Last Name</td>
 <td class="data"><el:text name="firstName2" idx="*" size="12" max="24" value="${param.firstName}" />
&nbsp;<el:text name="lastName2" idx="*" size="18" max="36" value="${param.lastName}" /></td>
</tr>
<tr>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="maxResults" idx="*" size="2" max="2" value="${maxResults}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data sec small"><el:box name="exactMatch" idx="*" value="true" label="Exact Matches only" checked="${param.exactMatch == '1'}" /></td>
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
 <td colspan="6" class="pri bld left">No Pilots matching your search criteria were found.</td>
</tr>
</c:when>
<c:otherwise>
<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">&nbsp;</td>
 <td width="10%">PILOT ID</td>
 <td width="25%">PILOT NAME</td>
 <td width="15%">RANK</td>
 <td width="10%">JOINED ON</td>
 <td>FLIGHTS / HOURS</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${results}">
<view:row entry="${pilot}">
 <td><el:box name="sourceID" idx="*" value="${fn:hex(pilot.ID)}" label="" /></td>
 <td class="bld">${pilot.pilotCode}</td>
 <td class="pri bld"><el:cmd url="profile" linkID="0x${pilot.ID}">${pilot.name}</el:cmd></td>
 <td class="small">${pilot.rank}, ${pilot.equipmentType}</td>
 <td><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
<c:if test="${pilot.legs > 0}">
 <td><el:cmd url="logbook" op="log" linkID="0x${pilot.ID}"><fmt:int value="${pilot.legs}" /> legs</el:cmd>, 
<el:cmd url="logbook" op="log" linkID="0x${pilot.ID}"><fmt:dec value="${pilot.hours}" /> hours</el:cmd></td>
</c:if>
<c:if test="${pilot.legs == 0}">
 <td class="bld">NO FLIGHTS LOGGED</td>
</c:if>
</view:row>
</c:forEach>

<!-- Table Legend Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="100" labels="Active,Inactive,Retired,On Leave" classes=" ,opt2,opt3,warn" />
 MERGE INTO <el:combo name="id" idx="*" size="1" firstEntry="-" options="${results}" /> WITH
 PILOT CODE <el:combo name="code" idx="*" size="1" firstEntry="-" options="${pilotCodes}" />
 <el:cmdbutton url="dupemerge" post="true" label="MERGE" /></td>
</tr>
</c:otherwise>
</c:choose>
</view:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
