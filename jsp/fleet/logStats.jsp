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
<title><content:airline /> Installer Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

setSubmit();
disableButton('SearchButton');
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
<el:form action="fleetstats.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">FLEET INSTALLER SYSTEM DATA STATISTICS</td>
</tr>
<tr>
 <td class="label">Sort Options</td>
 <td clas="data"><el:combo name="orderBy" idx="*" size="1" options="${sortOptions}" value="${param.orderBy}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td clas="data"><el:box name="sortLabel" idx="*" className="sec small" value="1" label="Sort Labels instead of Totals" checked="${param.sortLabel == '1'}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH FLEET INSTALLER STATISTICS" /></td>
</tr>
</el:table>
</el:form>

<c:if test="${!empty stats}">
<el:table className="view" pad="default" space="default">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="65%">STATISTICS LABEL</td>
 <td>TOTAL INSTANCES</td>
</tr>

<!-- Table Statistics Data -->
<c:forEach var="stat" items="${stats}">
<view:row entry="${stat}">
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:int value="${stat.count}" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="2">&nbsp;</td>
</tr>
</el:table>
</c:if>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
