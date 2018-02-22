<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Submitted Examinations</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="examqueue">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:25%">EXAMINATION NAME</td>
 <td style="width:20%">PILOT NAME</td>
 <td style="width:20%">RANK / EQUIPMENT</td>
 <td style="width:15%">CREATED ON</td>
 <td style="width:10%">QUESTIONS</td>
 <td>STAGE</td>
</tr>

<!-- Table Data -->
<c:forEach var="exam" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[exam.authorID]}" scope="page" />
<view:row entry="${exam}">
 <td class="pri bld"><el:cmd url="exam" link="${exam}">${exam.name}</el:cmd></td>
 <td class="bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td>${pilot.rank.name}, ${pilot.equipmentType}</td>
 <td class="sec"><fmt:date t="HH:mm" date="${exam.date}" /></td>
 <td><fmt:int value="${exam.size}" /></td>
 <td class="sec"><fmt:int value="${exam.stage}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="100" labels="Time Expired,Submitted" classes="opt2,opt1" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
