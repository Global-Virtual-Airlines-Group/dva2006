<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Accomplishments</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:googleAnalytics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.update = function() { document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="units" className="org.deltava.beans.stats.AccomplishUnit" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="accomplishments.do" method="post" validate="return true">
<view:table cmd="accomplishments">
<!-- Table Title Bar -->
<tr class="title caps">
 <td colspan="3" class="left"><span class="nophone"><content:airline />&nbsp;</span>PILOT ACCOMPLISHMENTS</td>
<td colspan="4" class="right">UNIT <el:combo name="unit" value="${param.unit}" options="${units}" firstEntry="[ ALL ]" onChange="void golgotha.local.update()" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
<c:if test="${access.canCreate}">
<td style="width:10%">NAME</td>
<td style="width:15%"><el:cmdbutton url="accomplishment" op="edit" label="NEW ACCOMPLISHMENT" /></td>
</c:if>
<c:if test="${!access.canCreate}">
 <td colspan="2">NAME</td>
</c:if>
 <td style="width:12%">UNITS</td>
 <td style="width:8%">VALUE</td>
 <td class="nophone" style="width:7%">PILOTS</td>
 <td class="nophone" style="width:30%">CHOICES</td>
 <td class="nophone">COLOR</td>
</tr>

<!-- Table Accomplishment Data -->
<c:forEach var="a" items="${viewContext.results}">
<c:set var="ac" value="${accessMap[a]}" scope="page" />
<view:row entry="${a}">
<c:if test="${ac.canEdit}">
 <td class="pri bld" colspan="2"><el:cmd url="accomplishment" link="${a}" op="edit">${a.name}</el:cmd></td>
</c:if>
<c:if test="${!ac.canEdit}">
 <td class="pri bld" colspan="2">${a.name}</td>
</c:if>
 <td class="sec bld">${a.unit.description}</td>
 <td class="bld"><fmt:int value="${a.value}" /></td>
 <td class="nophone"><fmt:int value="${a.pilots}" /></td>
 <td class="small nophone"><fmt:list value="${a.choices}" delim=", " empty="No Restrctions" /></td>
 <td class="nophone"><div class="mid" style="width:90%; background-color:#${a.hexColor};">&nbsp;</div></td>
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
</body>
</html>
