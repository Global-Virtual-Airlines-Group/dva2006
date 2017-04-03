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
<title><content:airline /> Flight Academy Check Rides</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="academyridequeue">
<tr class="title">
 <td colspan="6" class="left caps"><content:airline /> FLIGHT ACADEMY SUBMITTED CHECK RIDES</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:8%">DATE</td>
 <td style="width:7%">&nbsp;</td>
 <td style="width:20%">PILOT NAME</td>
 <td class="nophone" style="width:15%">COURSE NAME</td>
 <td class="nophone" style="width:10%">AIRCRAFT</td>
 <td class="nophone">FLIGHT ROUTE</td>
</tr>
 
<!-- Table View data -->
<c:forEach var="ride" items="${viewContext.results}">
<c:set var="course" value="${courses[ride.courseID]}" scope="page" />
<c:set var="pilot" value="${pilots[ride.authorID]}" scope="page" />
<c:set var="pirep" value="${pireps[ride.authorID]}" scope="page" />
<tr>
 <td><el:cmd url="checkride" link="${ride}"><fmt:date date="${ride.submittedOn}" fmt="d" /></el:cmd></td>
 <td><el:cmdbutton url="crview" linkID="${ride.hexID}" label="SCORE" /></td>
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd></td>
 <td class="nophone"><el:cmd url="course" link="${course}">${course.name}</el:cmd></td>
 <td class="sec nophone">${ride.aircraftType}</td>
 <td class="small nophone">${pirep.airportD.name} - ${pirep.airportA.name}</td>
</tr>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
