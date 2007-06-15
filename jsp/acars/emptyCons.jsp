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
<title><content:airline /> ACARS Empty Connection Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function switchType(combo)
{
self.location = 'acarsempty.do?id=' + combo.options[combo.selectedIndex].value;
return true;
}

function selectAll()
{
var form = document.forms[0];
for (var x = 0; x < form.conID.length; x++)
	isChecked = form.conID[x].checked = true;
	
return true;
}

function validate(form)
{
var isChecked = false;
for (var x = 0; x < form.conID.length; x++)
	isChecked = (isChecked || form.conID[x].checked);

// Check if at least one connection is checked
if (!isChecked) {
	alert('Select at least one Connection entry to delete.');
	return false;
}

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsdelc.do" method="post" validate="return validate(this)">
<view:table className="view" space="default" pad="default" cmd="acarsempty">
<!-- View Header Bar -->
<tr class="title">
 <td colspan="4" class="left">EMPTY ACARS CONNECTION ENTRIES</td>
 <td colspan="3" class="right">VIEW EMPTY <el:combo name="viewType" idx="*" size="1" options="${displayTypes}" value="${displayType}" onChange="void switchType(this)" /></td>
</tr>

<!-- View Legend Bar -->
<tr class="title caps">
 <td width="10%">ID</td>
 <td width="5%"><el:button onClick="void selectAll()" className="BUTTON" label="ALL" /></td>
 <td width="15%">DATE/TIME</td>
 <td width="10%">PILOT CODE</td>
 <td width="20%">PILOT NAME</td>
 <td width="10%">IP ADDRESS</td>
 <td>HOST NAME</td>
</tr>

<!-- Log Entries -->
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[entry.pilotID]}" scope="request" />
<c:set var="pilotLoc" value="${userData[entry.pilotID]}" scope="request" />
<view:row entry="${entry}">
 <td class="pri bld small"><fmt:hex value="${entry.ID}" /></td>
 <td><el:box name="conID" idx="*" value="${entry.ID}" label="" /></td>
 <td><fmt:date date="${entry.startTime}" /></td>
 <td class="sec bld">${pilot.pilotCode}</td>
 <td class="pri bld"><el:profile location="${pilotLoc}">${pilot.name}</el:profile></td>
 <td>${entry.remoteAddr}</td>
 <td class="small">${entry.remoteHost}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><el:button onClick="void selectAll()" className="BUTTON" label="SELECT ALL" />
 <el:button type="submit" label="DELETE CONNECTIONS" className="BUTTON" /><view:scrollbar><br />
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
