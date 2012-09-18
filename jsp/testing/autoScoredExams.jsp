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
<title><content:airline /> Automatically Scored Examinations</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function setExam(combo)
{
var examName = combo.options[combo.selectedIndex].text;
self.location = '/autoscoredexams.do?examName=' +examName;
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
<el:form action="autoscoredexams.do" method="get" validate="return false">
<view:table className="view" cmd="autoscoredexams">
<tr class="title">
 <td colspan="3" class="left caps">AUTOMATICALLY SCORED EXAMINATIONS</td>
 <td colspan="4" class="right">EXAMINATION <el:combo name="examName" size="1" idx="*" options="${examNames}" firstEntry="All Exams" value="${param.examName}" onChange="void setExam(this)" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td>&nbsp;</td>
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
<c:choose>
<c:when test="${!fn:passed(exam) && !fn:failed(exam)}">
 <td><el:img caption="Not Scored" x="21" y="21" src="blank.png" /></td>
</c:when>
<c:when test="${fn:passed(exam)}">
 <td><el:img caption="Passed" x="21" y="21" src="testing/pass.png" /></td>
</c:when>
<c:when test="${fn:failed(exam)}">
 <td><el:img caption="Unsatisfactory" x="21" y="21" src="testing/fail.png" /></td>
</c:when>
</c:choose>
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
