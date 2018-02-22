<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Academy Courses</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:pics />
<content:favicon />
<script type="text/javascript">
golgotha.local.setType = function() {
	document.forms[0].submit();
	return true;
};

golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.util.disable('stCombo');
	golgotha.util.disable('ftCombo');
	golgotha.form.submit(f);
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
<el:form action="courses.do" method="post" validate="return golgotha.local.validate(this)">
<view:table cmd="courses">
<tr class="title">
 <td colspan="3" class="left caps"><content:airline /> FLIGHT ACADEMY COURSES</td>
 <td colspan="4" class="right">SORT BY <el:combo ID="stCombo" name="sortType" idx="*" size="1" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.setType()" />
 FILTER <el:combo ID="ftCombo" name="filterType" size="1" idx="*" options="${viewOpts}" value="${filterOpt}" onChange="void golgotha.local.setType()" /></td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps">
 <td>COURSE NAME</td>
 <td class="nophone">STAGE</td>
 <td style="width:25%">PILOT NAME</td>
 <td class="nophone">STATUS</td>
 <td>STARTED</td>
 <td class="nophone">LAST COMMENT</td>
 <td>COMPLETED</td>
</tr>

<!-- Table View data -->
<c:forEach var="course" items="${viewContext.results}">
<c:set var="pilotLoc" value="${userData[course.pilotID]}" scope="page" />
<c:set var="pilot" value="${pilots[course.pilotID]}" scope="page" />
<view:row entry="${isPending ? pilot : course}">
 <td><el:cmd url="course" link="${course}" className="pri bld">${course.name}</el:cmd></td>
 <td class="sec bld nophone"><fmt:int value="${course.stage}" /></td>
 <td><el:profile location="${pilotLoc}" className="pri bld">${pilot.name}</el:profile> <span class="small">(${pilot.pilotCode})</span></td>
 <td class="pri bld nophone">${course.status.name}</td>
 <td class="small"><fmt:date fmt="d" date="${course.startDate}" /></td>
 <td class="sec small nophone"><fmt:date fmt="d" date="${course.lastComment}" default="-" /></td>
 <td class="small"><fmt:date fmt="d" date="${course.endDate}" default="N/A" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="7">
<c:if test="${isPending}"><view:legend width="100" labels="Active,Inactive,Retired,On Leave,Suspended" classes=" ,opt2,opt3,warn,error" /></c:if>
<view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
