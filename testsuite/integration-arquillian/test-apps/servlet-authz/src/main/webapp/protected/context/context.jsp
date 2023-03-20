<%@page import="org.keycloak.AuthorizationContext" %>

<%
    AuthorizationContext authzContext = (AuthorizationContext) request.getAttribute(AuthorizationContext.class.getName());
%>

<html>
<body>
<h2>Access granted: <%= authzContext.isGranted() %></h2>
<%@include file="../../logout-include.jsp"%>
</body>
</html>