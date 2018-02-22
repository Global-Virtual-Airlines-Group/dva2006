<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Multi-Player Aircraft Liveries</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.update = function(combo) {
	self.location = '/liveries.do?airline=' + combo.options[combo.selectedIndex].value;
	return true;	
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="liveries.do" method="get" validate="return false">
<view:table cmd="liveries">
<tr class="title">
 <td colspan="2" class="caps">ACARS MULTI-PLAYER AIRCRAFT LIVERIES</td>
 <td colspan="2" class="right">AIRLINE <el:combo name="airline" idx="*" size="1" options="${airlines}" value="${param.airline}" firstEntry="All Airlines" onChange="void golgotha.local.update(this)" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title">
 <td style="width:20%">AIRLINE</td>
 <td>LIVERY CODE</td>
 <td>DESCRIPTION</td>
 <td><el:cmdbutton url="livery" op="edit" label="NEW LIVERY" /></td>
</tr>

<!-- Table Aircraft Data -->
<c:forEach var="livery" items="${viewContext.results}">
<view:row entry="${livery}">
 <td>${livery.airline.name}</td>
 <td><el:cmd url="livery" op="edit" linkID="${livery.airline.code}-${livery.code}" className="pri bld">${livery.code}</el:cmd></td>
 <td colspan="2" class="left small">${livery.description}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="4"><view:legend width="100" labels="Default" classes="opt1" /><view:scrollbar><br />
<view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
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
