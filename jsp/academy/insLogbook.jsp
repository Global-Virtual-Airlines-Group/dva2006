<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Logbook<c:if test="${!empty pilot}"> for ${pilot.name} (${pilot.pilotCode})</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<script type="text/javascript">
golgotha.local.sort = function(combo) {
	self.location = '/inslogbook.do?id=' + golgotha.form.getCombo(combo);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="inslogbook.do" method="get" validate="return false">
<view:table cmd="inslogbook">
<tr class="title">
 <td colspan="7" class="caps left">PILOT LOGBOOK<c:if test="${!empty pilot}"> FOR ${pilot.rank.name} ${pilot.name} (${pilot.pilotCode})</c:if></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:10%">DATE</td>
 <td style="width:15%">COURSE</td>
 <td style="width:14%">STUDENT</td>
 <td style="width:14%">INSTRUCTOR</td>
 <td style="width:9%">DURATION</td>
 <td class="left" width="10%">COMMENTS</td>
<content:filter roles="HR"><td class="right">INSTRUCTOR <el:combo name="id" idx="*" size="1" options="${instructors}" value="${ins}" onChange="void golgotha.local.sort(this)" /></td>
</content:filter>
<content:filter roles="!HR">
 <td>${ins.name}</td>
</content:filter>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[pirep.pilotID]}" scope="page" />
<c:set var="ins" value="${pilots[pirep.instructorID]}" scope="page" />
<view:row entry="${pirep}">
 <td><el:cmd url="insflight" link="${pirep}"><fmt:date date="${pirep.date}" fmt="d" default="-" /></el:cmd></td>
 <td class="pri small"><el:cmd url="course" linkID="${fn:hex(pirep.courseID)}">${pirep.courseName}</el:cmd></td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="sec"><el:cmd url="profile" link="${ins}">${ins.name}</el:cmd></td>
 <td class="small"><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
 <td class="small left" colspan="2">${pirep.comments}</td>
</view:row>
</c:forEach>
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
