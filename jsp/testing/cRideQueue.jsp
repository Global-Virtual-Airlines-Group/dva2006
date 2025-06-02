<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Submitted Check Rides</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:googleAnalytics />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="crqueue">
<!-- Table Header Bars -->
<tr class="title caps">
 <td colspan="6" class="left"><content:airline /> SUBMITTED CHECK RIDES</td>
</tr>
<tr class="title caps">
 <td>CHECK RIDE NAME</td>
 <td class="nophone" style="width:5%">STAGE</td>
 <td style="width:20%">PILOT NAME</td>
 <td class="nophone" style="width:20%">RANK / EQUIPMENT</td>
 <td class="nophone" style="width:10%">ASSIGNED</td>
 <td style="width:10%">SUBMITTED</td>
</tr>

<!-- Table Data -->
<c:forEach var="cr" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[cr.authorID]}" scope="page" />
<tr>
 <td class="pri bld"><el:cmd url="checkride" link="${cr}">${cr.name}</el:cmd></td>
 <td class="sec bld nophone"><fmt:int value="${cr.stage}" /></td>
 <td class="bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="nophone">${pilot.rank.name}, ${pilot.equipmentType}</td>
 <td class="sec nophone"><fmt:date t="HH:mm" date="${cr.date}" /></td>
 <td><fmt:date t="HH:mm" date="${cr.submittedOn}" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>

