<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline />&nbsp;${eliteName} Rollover for ${year}</title>
<content:css name="main" />
<content:css name="form" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.submit(f);
	return true;
};
<c:if test="${!isRolloverPeriod}">
golgotha.onDOMReady(function() { golgotha.util.disable(document.getElementById('isCommit')); });
</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %>
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="pointUnit" name="econ.elite.points" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eliterollover.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;${eliteName}&nbsp;</span>ROLLOVER RESULTS FOR ${year}</td>
</tr>
<c:if test="${!empty msgs}">
<tr>
 <td class="label top">Operations Log</td>
 <td class="data"><c:forEach var="msg" items="${msgs}" varStatus="msgStatus">${msg}<c:if test="${!msgStatus.last}"><br /></c:if></c:forEach></td>
</tr>
<c:if test="${!isPersisted}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><fmt:int value="${rollovers}" className="pri bld" /> status rollovers to ${year}, <fmt:int value="${downgrades}" className="bld" /> downgrades in ${year}.<br /> 
<span class="sec bld caps">These changes have not been written to the database</span></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="allowPointRollover" value="true" className="small ita" label="Allow rollover based on ${elitePoints} attainment" checked="${param.allowPointRollover}" /><br />
<el:box ID="isCommit" name="isCommit" value="true" label="Write Updated ${eliteName} qualification levels to Database" /><c:if test="${!isRollover}"><br /><span class="ita">(Requirements can only be updated during the ${eliteName} status rollover period.)</span></c:if></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="ROLLOVER ${year}&nbsp;${eliteName} LEVELS" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
