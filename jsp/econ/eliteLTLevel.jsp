<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline />&nbsp;${eliteName}<c:if test="${!empty lvl}"> - ${lvl.name}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.name, l:3, t:'Level Name'});
	golgotha.form.validate({f:f.code, l:3, t:'Level Abbreviation'});
	golgotha.form.validate({f:f.legs, min:0, t:'Minimum Legs'});
	golgotha.form.validate({f:f.distance, min:0, t:'Minimum Distance'});
	golgotha.form.validate({f:f.level, t:'${eliteName} Status Level'});
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %>
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eliteltlevel.do" method="post" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline /> </span>${eliteName} LIFETIME STATUS<c:if test="${!empty lvl}"> - ${lvl.name} (${lvl.year})</c:if></td>
</tr>
<tr>
 <td class="label">Name</td>
 <td class="data"><el:text name="name" idx="*" size="24" max="32" required="true" className="pri bld" value="${lvl.name}" /></td>
</tr>
<tr>
 <td class="label">Abbreviation</td>
 <td class="data"><el:text name="code" idx="*" size="4" max="5" required="true" className="bld" value="${lvl.code}" /></td>
</tr>
<tr>
 <td class="label">${eliteName} Level</td>
 <td class="data"><el:combo name="level" required="true" idx="*" firstEntry="[ SELECT LEVEL ]" options="${statusLevels}" /></td> 
</tr>
<tr class="title caps">
 <td colspan="2">ELIGIBILITY REQUIREMENTS</td>
</tr>
<tr>
 <td class="label">Minimum Legs</td>
 <td class="data"><el:text name="legs" idx="*" size="3" max="4" required="true" className="bld" value="${lvl.legs}" /></td>
</tr>
<tr>
 <td class="label">Minimum Distance</td>
 <td class="data"><el:text name="distance" idx="*" size="6" max="8" required="true" value="${lvl.distance}" /> miles</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data sec bld caps">This ${eliteName} lifetime Status Level has been reached by <fmt:int value="${pilotCount}" />&nbsp;<content:airline /> Pilots</td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE ${eliteName} LIFETIME PROFILE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
