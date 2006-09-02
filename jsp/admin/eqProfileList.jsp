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
<title><content:airline /> Equipment Type Programs</title>
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
<view:table className="view" pad="default" space="default" cmd="eqTypes">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="10%">PROGRAM NAME</td>
 <td width="5%">STAGE</td>
 <td width="12%">CHIEF PILOT</td>
 <td width="23%">RANKS</td>
 <td width="5%">SIZE</td>
 <td>RATINGS</td>
</tr>

<!-- Table data -->
<c:forEach var="eqType" items="${eqTypes}">
<c:set var="pilotCount" value="${eqTypeStats[eqType.name]}" scope="request" />
<view:row entry="${eqType}">
 <td class="pri bld"><el:cmd url="eqtype" linkID="${eqType.name}" op="edit">${eqType.name}</el:cmd></td>
 <td class="sec bld"><fmt:int value="${eqType.stage}" /></td>
 <td><el:cmd url="profile" linkID="0x${eqType.CPID}">${eqType.CPName}</el:cmd></td>
 <td class="sec small"><fmt:list value="${eqType.ranks}" delim=", " /></td>
 <td><fmt:int value="${empty pilotCount ? 0 : pilotCount}" /></td>
 <td class="left small"><span class="pri">PRIMARY: <fmt:list value="${eqType.primaryRatings}" delim=", " /></span>
<c:if test="${!empty eqType.secondaryRatings}">
<br />SECONDARY: <fmt:list value="${eqType.secondaryRatings}" delim=", " />
</c:if>
</td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="90" classes=" ,opt2" labels="Active,Inactive" />
<content:filter roles="HR">&nbsp;<el:cmdbutton url="eqtype" op="edit" label="NEW EQUIPMENT TYPE" /></content:filter>
 </td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
