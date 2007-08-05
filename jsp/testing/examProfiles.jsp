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
<title><content:airline /> Examination Profiles</title>
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
<view:table className="view" pad="default" space="default" cmd="eprofiles">

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="15%">EXAM NAME</td>
<c:if test="${access.canEdit}">
 <td width="20%"><el:cmdbutton url="eprofile" op="edit" label="NEW EXAMINATION PROFILE" /></td>
</c:if>
<c:if test="${!access.canEdit}">
 <td width="20%">&nbsp;</td>
</c:if>
 <td width="15%">EQUIPMENT TYPE</td>
 <td width="10%">STAGE</td>
 <td width="10%">MIN STAGE</td>
 <td width="10%">SIZE</td>
 <td>PASSING SCORE</td>
</tr>

<!-- Table Examination Profile Data -->
<c:forEach var="exam" items="${examProfiles}">
<view:row entry="${exam}">
 <td class="pri bld" colspan="2"><el:cmd url="eprofile" linkID="${exam.name}" op="read">${exam.name}</el:cmd></td>
 <td>${empty exam.equipmentType ? 'N/A' : exam.equipmentType}</td>
 <td class="sec bld"><fmt:int value="${exam.stage}" /></td>
 <td class="sec"><fmt:int value="${exam.minStage}" /></td>
 <td class="bld"><fmt:int value="${exam.size}" /></td>
 <td><fmt:int value="${exam.passScore}" /> (<fmt:dec value="${exam.passScore / exam.size * 100}" />%)</td>
</view:row>
</c:forEach>

<!-- Table Legend Bar -->
<tr class="title">
 <td colspan="7"><view:legend width="95" labels="Active,Inactive,Academy" classes=" ,warn,opt2" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
