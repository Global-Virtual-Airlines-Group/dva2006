<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Virtual Airline - ${aInfo.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.validate({f:f.domain, l:6, t:'Domain Name'});
    golgotha.form.validate({f:f.db, l:2, t:'Database Name'});
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
<el:form action="ainfo.do" method="post" linkID="${aInfo.code}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">VIRTUAL AIRLINE PROFILE - ${aInfo.name}</td>
</tr>
<tr>
 <td class="label">Name</td>
 <td class="data pri bld">${aInfo.name}</td>
</tr>
<tr>
 <td class="label">Airline Code</td>
 <td class="data sec bld">${aInfo.code}</td>
</tr>
<tr>
 <td class="label">Domain Name</td>
 <td class="data"><el:text name="domain" idx="*" required="true" size="12" max="32" value="${aInfo.domain}" /></td>
</tr>
<tr>
 <td class="label">Database</td>
 <td class="data"><el:text name="db" idx="*" className="bld" required="true" size="5" max="12" value="${aInfo.DB}" /></td> 
</tr>
<tr>
 <td class="label">Elite Program Name</td>
 <td class="data"><el:text name="eliteName" idx="*" size="16" max="32" value="${aInfo.eliteProgram}" />
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="canTX" className="small" label="Airline allows inbound Pilot transfers" value="true" checked="${aInfo.canTransfer}" /><br />
<el:box name="historicRestrict" className="small" label="Historic Routes require Historic Aircraft" value="true" checked="${aInfo.historicRestricted}" /><br />
<el:box name="allowsMulti" className="small" label="Allows membership in multiple Virtual Airlines" value="true" checked="${aInfo.allowMultiAirline}" /><br /></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE VIRTUAL AIRLNE PROFILE" /></td>
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
