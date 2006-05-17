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
<title><content:airline /> Logbook<c:if test="${!empty pilot}"> for ${pilot.name} (${pilot.pilotCode})</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="javascript" type="text/javascript">
function sort(combo)
{
var sortType = combo.options[combo.selectedIndex].value;
self.location = '/inslogbook.do?id=0x<fmt:hex value="${param.id}" />';
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
<el:form action="inslogbook.do" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="inslogbook">
<tr class="title">
 <td colspan="6" class="caps left">PILOT LOGBOOK<c:if test="${!empty pilot}"> FOR ${pilot.rank} ${pilot.name} (${pilot.pilotCode})</c:if></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">DATE</td>
 <td width="15%">COURSE</td>
 <td width="15%">STUDENT</td>
 <td width="15%">INSTRUCTOR</td>
 <td width="10%">DURATION</td>
 <td class="left" width="10%">COMMENTS</td>
<content:filter roles="HR">
 <td class="right"><el:cmd url="inslogbook">ALL</el:cmd> | INSTRUCTOR 
<el:combo name="id" idx="*" size="1" options="${instructors}" value="${ins}" /></td>
</content:filter>
<content:filter roles="!HR">
 <td>${ins.name}</td>
</content:filter>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[pirep.pilotID]}" scope="request" />
<c:set var="ins" value="${pilots[pirep.instructorID]}" scope="request" />
<view:row entry="${pirep}">
 <td class="title"><fmt:date date="${pirep.date}" fmt="d" default="-" /></td>
 <td class="pri"><el:cmd url="course" linkID="0x${pirep.courseID}">${pirep.courseName}</el:cmd></td>
 <td><el:cmd url="profile" linkID="0x${pilot.ID}">${pilot.name}</el:cmd></td>
 <td><el:cmd url="profile" linkID="0x${ins.ID}" className="sec">${ins.name}</el:cmd></td>
 <td><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
 <td class="small left" colspan="2">${pirep.comments}</td>
</view:row>
</c:forEach>
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
