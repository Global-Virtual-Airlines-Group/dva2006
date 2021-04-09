<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Mass Mailing</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.subject, l:7, t:'Message Subject'});
golgotha.form.validate({f:f.body, l:25, t:'Message Body'});
golgotha.form.validate({f:f.eqType, t:'Recipients Equipment Type'});
golgotha.form.validate({f:f.fAttach, ext:['pdf','txt'], t:'Attached File', empty:true});
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
<el:form action="massmail.do" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">PILOT MASS E-MAIL MESSAGE</td>
</tr>
<tr>
 <td class="label">Message Subject</td>
 <td class="data"><el:text name="subject" idx="*" size="48" max="64" className="bld req" value="${param.subject}" /></td>
</tr>
<tr>
 <td class="label">Attached File</td>
 <td class="data"><el:file name="fAttach" idx="*" size="96" max="144" /></td>
</tr>
<tr>
 <td class="label">Recipient Program / Security Role</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="-" className="req" options="${eqTypes}" value="${param.eqType}" /></td>
</tr>
<tr>
 <td class="label top">Message Text</td>
 <td class="data"><el:textbox name="body" className="req" idx="*" width="80%" height="5" resize="true">${param.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SEND MESSAGE" /></td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
