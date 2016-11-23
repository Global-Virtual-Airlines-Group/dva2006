<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Fleet Gallery</title>
<content:expire expires="240" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:sysdata var="imgDB" name="airline.db" />
<script type="text/javascript">
golgotha.local.selectAircraft = function(combo)
{
if (!golgotha.form.comboSet(combo)) {
	golgotha.util.display('descRow', false);
	golgotha.util.display('imgRow', false);
	return false;
}

// Get the image object and its description object
var img = document.getElementById('fleetPic');
var desc = document.getElementById('fleetDesc');

// Load the picture in its place, save the description
img.src = '/gallery/${imgDB}/0x' + escape(golgotha.form.getCombo(combo)) + '.jpg';
desc.innerHTML = golgotha.local.dList[combo.selectedIndex - 1];
golgotha.util.display('imgRow', true);
golgotha.util.display('descRow', true);
desc.focus(); 
return true;
};

<fmt:jsarray var="golgotha.local.dList" items="${fleetGalleryDesc}" />
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="imgPath" name="path.img" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="fleet.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLEET GALLERY</td>
</tr>
<tr>
 <td class="label">Select Aircraft</td>
 <td><el:combo name="Aircraft" size="1" idx="1" options="${fleetGallery}" firstEntry="[ SELECT AIRCRAFT ]" onChange="void golgotha.local.selectAircraft(this)" /></td>
</tr>
<tr id="descRow" style="display:none;">
 <td class="label">Description</td>
 <td><span id="fleetDesc"></span></td>
</tr>
<tr id="imgRow" style="display:none;">
 <td colspan="2" class="mid"><img id="fleetPic" style="max-width:98%;" src="/${imgPath}/blank.png" alt="<content:airline /> Fleet Gallery" /></td>
</tr>
<tr class="title">
 <td colspan="2">&nbsp;</td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
