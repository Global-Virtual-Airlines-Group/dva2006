<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Submitted Examinations</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" cmd="examqueue">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="25%">EXAMINATION NAME</td>
 <td width="20%">PILOT NAME</td>
 <td width="20%">RANK / EQUIPMENT</td>
 <td width="15%">CREATED ON</td>
 <td width="10%">QUESTIONS</td>
 <td>STAGE</td>
</tr>

<!-- Table Data -->
<c:forEach var="exam" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[exam.pilotID]}" scope="page" />
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
