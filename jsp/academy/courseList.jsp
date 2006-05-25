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
<title><content:airline /> Flight Academy Courses</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function setType()
{
document.forms[0].submit();
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
<el:form action="courses.do" method="post" validate="return true">
<view:table className="view" space="default" pad="default" cmd="courses">
<tr class="title">
 <td colspan="3" class="left caps"><content:airline /> FLIGHT ACADEMY COURSES</td>
 <td colspan="4" class="right">SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortTypes}" value="${viewContext.sortType}" onChange="void setType()" />
 FILTER <el:combo name="filterType" size="1" idx="*" options="${viewOpts}" value="${sortOpt}" onChange="void setType()" /></td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="20%">COURSE NAME</td>
 <td width="7%">STAGE</td>
 <td width="25%">PILOT NAME</td>
 <td width="10%">STATUS</td>
 <td width="10%">STARTED</td>
 <td width="10%">LAST COMMENT</td>
 <td>COMPLETED</td>
</tr>

<!-- Table View data -->
<c:forEach var="course" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[course.pilotID]}" scope="request" />
<view:row entry="${course}">
 <td><el:cmd url="course" linkID="0x${course.ID}" className="pri bld">${course.name}</el:cmd></td>
 <td class="bld"><fmt:int value="${course.stage}" /></td>
 <td class="small"><el:cmd url="profile" linkID="0x${pilot.ID}" className="sec bld">${pilot.name}</el:cmd>
 (${pilot.pilotCode})</td>
 <td class="pri bld small">${course.statusName}</td>
 <td class="small"><fmt:date fmt="d" date="${course.startDate}" /></td>
 <td class="sec small"><fmt:date fmt="d" date="${course.lastComment}" default="-" /></td>
 <td class="small"><fmt:date fmt="d" date="${course.endDate}" default="N/A" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
