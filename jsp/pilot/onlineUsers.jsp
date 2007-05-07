<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Users</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function sortBy(combo)
{
var sortCode = combo.options[combo.selectedIndex].value;
self.location = '/users.do?sortOpt=' + sortCode;
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
<el:form action="users.do" method="get" validate="return false">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td colspan="5" class="left caps">CURRENTLY LOGGED IN USERS</td>
 <td colspan="2" class="right">SORT BY <el:combo name="sortOpt" idx="*" size="1" options="${sortOptions}" value="${sortOpt}" onChange="void sortBy(this)" /></td>
</tr>

<!-- Pilot Title Bar -->
<tr class="title caps">
 <td width="10%">PILOT ID</td>
 <td width="20%">PILOT NAME</td>
 <td width="12%">RANK</td>
 <td width="13%">EQUIPMENT TYPE</td>
 <td width="20%">LOCATION</td>
 <td>JOINED ON</td>
</tr>

<!-- Pilot Data Bar -->
<c:forEach var="pilot" items="${pilots}">
<tr>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td class="bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="pri">${pilot.rank}</td>
 <td class="sec">${pilot.equipmentType}</td>
 <td>${pilot.location}</td>
 <td class="small"><fmt:date date="${pilot.createdOn}" fmt="d" d="EEEE MMMM dd, yyyy" /></td>
</tr>
<content:filter roles="Admin,HR">
<tr>
 <td colspan="6">Logged in since <fmt:date date="${pilot.lastLogin}" />, from ${pilot.loginHost}.</td>
</tr>
</content:filter>
</c:forEach>
<c:if test="${empty pilots}">
<tr>
 <td colspan="7" class="pri bld">NO CURRENTLY LOGGED IN WEB SITE USERS</td>
</tr>
</c:if>
<tr class="title">
 <td colspan="7">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
