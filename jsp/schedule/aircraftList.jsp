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
<title><content:airline /> Schedule - Aircraft</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="aircraftlist">

<!-- Table Header Bar -->
<tr class="title">
 <td width="25%">AIRCRAFT NAME</td>
 <td width="17%">IATA CODE</td>
 <td width="28%">WEB APPLICATIONS</td>
 <td><el:cmdbutton url="airline" op="edit" label="NEW AIRCRAFT" /></td>
</tr>

<!-- Table Aircraft Data -->
<c:forEach var="aircraft" items="${aircraftInfo}">
<view:row entry="${aircraft}">
 <td><el:cmd url="aircraft" linkID="${aircraft.name}" op="edit" className="pri bld">${aircraft.name}</el:cmd></td>
 <td><fmt:list value="${aircraft.IATA}" delim=", " /></td>
 <td colspan="2" class="sec"><fmt:list value="${aircraft.apps}" delim=", " /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="4"><view:legend width="100" labels="Historic,Current" classes="opt1, " /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
