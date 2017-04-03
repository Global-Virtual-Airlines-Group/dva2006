<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule Synchronizatoin</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.purgeOnly = false;
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.airline, t:'Airline Name'});
if (!golgotha.local.purgeOnly)
	golgotha.form.validate({f:f.vaCode, t:'Virtual Airline Name'});

golgotha.form.submit(f);
return true;
};

golgotha.local.toggle = function(cb)
{
var f = document.forms[0];
golgotha.local.purgeOnly = cb.checked;
golgotha.util.disable(f.vaCode, golgotha.local.purgeOnly);
golgotha.util.disable(f.purgeEntries, golgotha.local.purgeOnly);
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
<el:form action="schedsync.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLIGHT SCHEDULE SYNCHRONIZATION</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" firstEntry="[ AIRLINE ]" required="true" value="${airline}" options="${airlines}" /></td>
</tr>
<tr>
 <td class="label">Synchronize from</td>
 <td class="data"><el:combo name="vaCode" idx="*" size="1" firstEntry="[ VIRTUAL AIRLINE ]" options="${apps}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="purgeOnly" value="true" label="Remove existing Schedule entries only" onChange="void golgotha.local.toggle(this)" /><br />
<el:box name="purgeEntries" value="true" label="Purge synchronized Schedule entries at next Import" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SYNCHRONIZE FLIGHT SCHEDULES" /></td>
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
