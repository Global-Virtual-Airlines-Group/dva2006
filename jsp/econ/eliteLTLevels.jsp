<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline /> - ${eliteName} Lifetime Status Levels</title>
<content:css name="main" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="eliteDistance" name="econ.elite.distance" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="elitelevels">

<!-- Table Header Bar -->
<tr class="title caps">
 <td colspan="3" class="left"><span class="nophone"><content:airline />&nbsp;${eliteName} </span>LIFETIME STATUS LEVELS</td>
 <td colspan="2" class="mid"><el:cmd url="eliteltlevel">NEW STATUS LEVEL</el:cmd></td>
</tr>

<tr class="title caps">
 <td>NAME</td>
 <td style="width:10%">ABBR</td>
 <td style="width:10%">LEGS</td>
 <td style="width:20%">DISTANCE</td>
 <td>EQUIVALENT STATUS</td>
</tr>

<!-- Table Lifetime Level Data -->
<c:forEach var="lvl" items="${viewContext.results}">
<tr>
 <td style="color:#ffffff; background-color:#${lvl.hexColor}"><el:cmd url="eliteltlevel" className="bld" linkID="${lvl.code}" op="edit">${lvl.name}</el:cmd></td>
 <td class="pri bld">${lvl.code}</td>
 <td><fmt:int value="${lvl.legs}" zero="-" /></td>
 <td class="bld"><fmt:int value="${lvl.distance}" zero="-" /><span class="nophone"> ${eliteDistance}</span></td>
 <td style="color:#ffffff; background-color:#${lvl.hexColor}"><el:cmd url="eliteltlevel" className="bld" linkID="${lvl.level.name}-${lvl.level.year}" op="edit">${lvl.level.name}</el:cmd></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
