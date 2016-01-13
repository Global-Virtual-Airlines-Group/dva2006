<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Reactivation</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
<fmt:jsarray var="golgotha.form.invalidDomains" items="${badDomains}" />
<c:forEach var="domain" items="${ourDomains}">
golgotha.form.invalidDomains.push('${domain}');</c:forEach>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.eMail, addr:true, t:'E-Mail Address'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="activate.do" method="POST" link="${pilot}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">PILOT REACTIVATION - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data sec bld">${pilot.equipmentType}</td>
</tr>
<tr>
 <td class="label">Rank</td>
 <td class="data"><el:combo name="rank" idx="*" size="1" options="${eqType.ranks}" value="${pilot.rank.name}" /></td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:addr name="eMail" required="true" idx="*" size="32" max="80" value="${pilot.email}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="ActivateButton" type="submit" label="REACTIVATE PILOT" /></td>
</tr>
</el:table>
<el:text name="op" type="hidden" value="force" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
