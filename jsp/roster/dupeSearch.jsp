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
<title><content:airline /> Duplicate Pilot Search</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
const act = f.action;
if (act.indexOf('dupemerge.do') != -1) {
	golgotha.form.validate({f:f.id, t:'Pilot to merge into'});
	golgotha.form.validate({f:f.code, t:'new Pilot Code'});
} else {
	const hasFirst = (f.firstName.value.length > 2) || (f.firstName2.value.length > 2);
	const hasLast = (f.lastName.value.length > 2) || (f.lastName2.value.length > 2);
	if (!hasFirst && !hasLast)
		throw new golgotha.util.ValidationErorr('Please provide at least one First or Last Name.', f.firstName);	
}

golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="dupesearch.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<!-- Search Criteria -->
<el:table className="form">
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
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="PILOT SEARCH" /></td>
</tr>
</el:table>

<!-- Search Results -->
<c:if test="${!noResults}">
<view:table cmd="">
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
 <td style="width:10%">&nbsp;</td>
 <td style="width:10%">PILOT ID</td>
 <td style="width:25%">PILOT NAME</td>
 <td style="width:20%">RANK</td>
 <td style="width:10%">JOINED ON</td>
 <td>FLIGHTS / HOURS</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${results}">
<view:row entry="${pilot}">
 <td><el:box name="sourceID" idx="*" value="${pilot.hexID}" label="" /></td>
 <td class="bld">${pilot.pilotCode}</td>
 <td class="pri bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="small">${pilot.rank.name}, ${pilot.equipmentType}</td>
 <td><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
<c:if test="${pilot.legs > 0}">
 <td><el:cmd url="logbook" op="log" link="${pilot}"><fmt:int value="${pilot.legs}" /> legs</el:cmd>, 
<el:cmd url="logbook" op="log" link="${pilot}"><fmt:dec value="${pilot.hours}" /> hours</el:cmd></td>
</c:if>
<c:if test="${pilot.legs == 0}">
 <td class="bld">NO FLIGHTS LOGGED</td>
</c:if>
</view:row>
</c:forEach>

<!-- Table Legend Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="100" labels="Active,Inactive,Retired,On Leave" classes=" ,opt2,opt3,warn" /></td>
</tr>
</c:otherwise>
</c:choose>
</view:table>
<c:if test="${fn:sizeof(results) > 1}">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">MERGE SELECTED PILOTS</td>
</tr>
<tr>
 <td class="label">Merge selected Pilots</td>
 <td class="data">into <el:combo name="id" idx="*" size="1" firstEntry="-" options="${pilotChoices}" /> 
 using Pilot Code <el:combo name="code" idx="*" size="1" firstEntry="-" options="${pilotCodes}" /></td>
</tr>
<tr>
 <td class="label top">Merge Options</td>
 <td class="data"><el:box name="mergeFlights" value="true" checked="true" label="Merge Flight Reports" /><br />
<el:box name="mergeExams" value="true" checked="true" label="Merge Examinations" /><br />
<el:box name="mergeCRs" value="true" checked="true" label="Merge Check Rides" /><br />
<el:box name="mergeFA" value="true" checked="true" label="Merge Flight Academy Certifications" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="dupemerge" post="true" label="MERGE PILOTS" /></td>
</tr>
</el:table>
</c:if>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
