<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Academy Roster</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<script type="text/javascript">
golgotha.local.setType = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<content:attr attr="canView" value="true" roles="HR,AcademyAdmin,AcademyAudit,Instructor" /> 

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="graduates.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<view:table cmd="graduates">
<tr class="title">
 <td colspan="3" class="left caps"><span class="nophone"><content:airline /> </span>FLIGHT ACADEMY GRADUATES</td>
 <td colspan="5" class="right">SELECT COURSE <el:combo name="cert" idx="*" size="1" options="${certs}" firstEntry="-" value="${param.cert}" onChange="void golgotha.local.setType()" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td>PILOT CODE</td>
 <td style="max-width:30%">PILOT NAME</td>
 <td style="width:20%">RANK</td>
 <td class="nophone">STARTED</td>
 <td>COMPLETED</td>
 <td class="nophone">FLIGHTS</td>
 <td class="nophone">HOURS</td>
 <td class="nophone">LAST FLIGHT</td>
</tr>

<!-- Table View data -->
<c:forEach var="course" items="${viewContext.results}">
<c:set var="pilotLoc" value="${userData[course.pilotID]}" scope="page" />
<c:set var="pilot" value="${pilots[course.pilotID]}" scope="page" />
<view:row entry="${pilot}">
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:profile location="${pilotLoc}" className="sec bld plain">${pilot.name}</el:profile></td>
 <td>${pilot.rank.name}, ${pilot.equipmentType}</td>
<c:if test="${canView}">
 <td class="nophone"><el:cmd url="course" link="${course}"><fmt:date fmt="d" date="${course.startDate}" /></el:cmd></td>
</c:if>
<c:if test="${!canView}">
 <td class="nophone"><fmt:date fmt="d" date="${course.startDate}" /></td>
</c:if>
 <td class="bld"><fmt:date fmt="d" date="${course.endDate}" /></td>
 <td class="sec bld small nophone"><fmt:int value="${pilot.legs}" /></td>
 <td class="small nophone"><fmt:dec value="${pilot.hours}" /></td>
 <td class="small nophone"><fmt:date fmt="d" date="${pilot.lastFlight}" default="-" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="100" labels="Active,Inactive,Retired,On Leave,Suspended" classes=" ,opt2,opt3,warn,error" /></td>
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
