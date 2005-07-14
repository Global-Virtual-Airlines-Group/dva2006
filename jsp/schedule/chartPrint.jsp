<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<HTML>
<HEAD>
<TITLE><content:airline /> Approach Chart - ${chart.name}</TITLE>
<content:css name="main" />
</HEAD>
<content:copyright visible="false" />
<BODY>
<DIV CLASS="noPrint">
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
</DIV>

<!-- Main Body Frame -->
<DIV ID="main">
<SPAN CLASS="noPrint"><A HREF="javascript:window.print()">Print Chart</A><br></SPAN>
<IMG ALT="${chart.name}, ${chart.size} bytes" SRC="/charts/${chart.ID}" BORDER="0" />
</DIV>
</BODY>
</HTML>
