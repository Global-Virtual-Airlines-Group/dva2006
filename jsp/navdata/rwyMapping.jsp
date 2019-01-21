<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><fmt:airport airport="${airport}" /> - Runway Mappings</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;	
	f.json.value = JSON.stringify(golgotha.local.rwyData);
	golgotha.form.submit(f);
	return true;
};

golgotha.local.addRunway = function(f)
{
golgotha.form.validate({f:f.oldCode, l:1, t:'Old Runway code'});
golgotha.form.validate({f:f.newCode, l:1, t:'New Runway code'});
var jo = {o:f.oldCode.value, n:f.newCode.value};
f.oldCode.value = ''; f.newCode.value = '';

// Check for duplicates
var m = golgotha.local.rwyData.mappings;
for (var x = 0; x < m.length; x++) {
	if (jo.o == m[x].o)
		return false;
}

// Create the new row
var r = document.createElement('tr');
r.setAttribute('class', 'newMapRow');
r.setAttribute('id', 'rwyMap-' + f.oldCode);
var ld = document.createElement('td');
ld.setAttribute('class', 'label');
ld.appendChild(document.createTextNode('New Mapping'));
var dd = document.createElement('td');
dd.setAttribute('class', 'data');
dd.setAttribute('colspan', '2');
dd.appendChild(document.createTextNode('Runway ' + jo.o + ' is now ' + jo.n));
r.appendChild(ld);
r.appendChild(dd);

// Add to the table / data object
var aRow = document.getElementById('addRow');
aRow.parentElement.insertBefore(r, aRow);
m.push(jo);
return true;
};

golgotha.local.remove = function(oc)
{
var idx = -1; var c = ''; var m = golgotha.local.rwyData.mappings;
while ((idx < m.length) && (oc != c)) {
	idx++;
	c = m[idx].o;
}

if (idx >= golgotha.local.rwyData.mappings.length) return false;
var r = document.getElementById('rwyMap-' + oc);
r.parentElement.removeChild(r);
m.splice(idx, 1);
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
<el:form action="rwymapping.do" method="post" linkID="${airport.ICAO}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Title Bar -->
<tr class="title caps" id="titleRow">
 <td colspan="2">${airport.name} (<fmt:airport airport="${airport}" />) RUNWAY MAPPINGS</td>
 <td style="width:25%;" class="right nophone">AIRPORT <el:combo name="airports" options="${airports}" value="${airport}" firstEntry="[ AIRPORT ]" onChange="void golgotha.local.updateAirport(this)" /></td>
</tr>
<c:forEach var="rm" items="${rwyMappings}">
<tr class="rwyMapRow" id="rwyMap-${rm.oldCode}">
 <td class="label">Runway ${rm.oldCode}</td>
 <td colspan="2" class="data">is now Runway <span class="pri bld">${rm.newCode}</span> <a href="javascript:void golgotha.local.remove('${rm.oldCode}')" class="small sec bld">DELETE</a></td>
</tr>
</c:forEach>

<!-- Add Runway -->
<tr id="addRow">
 <td class="label">New Runway Mapping</td>
 <td colspan="2" class="data">Runway <el:text name="oldCode" size="3" max="4" className="bld" idx="*" /> is now numbered <el:text name="newCode" size="3" max="4" className="pri bld" idx="*" />
&nbsp;<el:button label="ADD RUNWAY MAPPING" onClick="void golgotha.form.wrap(golgotha.local.addRunway, document.forms[0])" /></td>
</tr>
</el:table>

<!-- Button bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE RUNWAY MAPPINGS" /></td>
</tr>
</el:table>
<el:text type="hidden" name="json" value="" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
golgotha.local.rwyData = JSON.parse('${json}');
</script>
</body>
</html>
