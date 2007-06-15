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
<title><content:airline /> ACARS Client Error Logs</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.viewType, 'Filter Type')) return false;
var filterType = (form.viewType) ? form.viewType.selectedIndex : 0;
if (form.viewType == 1) {
 if (!validateCombo(form.author, 'Error Report Author')) return false;
} else if (form.viewType == 2) {
 if (!validateCombo(form.build, 'ACARS Client Build')) return false;
}

setSubmit();
disableButton('SortButton');
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
<el:form action="acarserrors.do" method="post" validate="return validate(this)">
<view:table className="view" space="default" pad="default" cmd="acarserrors">
<!-- View Header Bar -->
<tr class="title">
 <td colspan="2" class="left">ACARS CLIENT ERROR LOGS</td>
 <td colspan="4" class="right">FILTER BY <el:combo name="viewType" idx="*" size="1" firstEntry="-" options="${filterOpts}" value="${param.viewType}" onChange="void switchType(this)" />
 BUILD <el:combo name="build" idx="*" size="1" firstEntry="-" options="${clientBuilds}" value="${param.build}" />
 USER <el:combo name="author" idx="*" size="1" firstEntry="-" options="${authors}" value="${param.author}" />
 <el:button ID="SortButton" type="submit" className="BUTTON" label="GO" /></td>
</tr>

<!-- View Legend Bar -->
<tr class="title caps">
 <td width="5%">#</td>
 <td width="15%">DATE/TIME</td>
 <td width="15%">PILOT NAME</td>
 <td width="5%">BUILD</td>
 <td width="5%">FS</td>
 <td class="left">ERROR MESSAGE</td>
</tr>

<!-- View Data -->
<!-- Log Entries -->
<c:forEach var="err" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[err.userID]}" scope="request" />
<c:set var="pilotLoc" value="${userData[err.userID]}" scope="request" />
<view:row entry="${err}">
 <td class="sec bld"><fmt:int value="${err.ID}" /></td>
 <td class="small bld"><el:cmd url="acarserror" link="${err}"><fmt:date date="${err.createdOn}" /></el:cmd></td>
 <td class="pri bld"><el:profile location="${pilotLoc}">${pilot.name}</el:profile></td>
 <td class="sec bld"><fmt:int value="${err.clientBuild}" /></td>
<c:if test="${err.FSVersion > 0}">
 <td class="small">FS${err.FSVersion}</td>
</c:if>
<c:if test="${err.FSVersion == 0}">
 <td class="small">N/A</td>
</c:if>
 <td class="left"><fmt:text value="${err.message}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
