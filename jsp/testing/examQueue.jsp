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
<title><content:airline /> Submitted Examinations</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="eprofiles">
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
<c:set var="pilot" value="${pilots[exam.pilotID]}" scope="request" />
<view:row entry="${exam}">
 <td class="pri bld"><el:cmd url="exam" linkID="0x${exam.ID}">${exam.name}</el:cmd></td>
 <td class="bld"><el:cmd url="profile" linkID="0x${exam.pilotID}">${pilot.name}</el:cmd></td>
 <td>${pilot.rank}, ${pilot.equipmentType}</td>
 <td class="sec"><fmt:date t="hh:mm" date="${exam.date}" /></td>
 <td><fmt:int value="${exam.size}" /></td>
 <td class="sec"><fmt:int value="${exam.stage}" /></td>
</view:row>
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
