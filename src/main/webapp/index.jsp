<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1><%= "Hello World!" %>
</h1>
<br/>
<%
    Cookie[] cookies = request.getCookies();
    boolean session_validate = false;
    if (cookies != null) {
    for (Cookie c : cookies) {
    if (c.getName().compareTo("session_id") == 0) {
    session_validate = true;

%>
<h3>You are working in a session identified by following cookie:</h3>
<%= c.getName() %>: <%= c.getValue() %>
<br>
<a href="compose.jsp">compose letter</a>
<a href="inbox.jsp">Mail box</a>
<br>
<a href="work.jsp?logout=yes">Logout</a>
<%
    }
    }
    }
    if (!session_validate) {
%>
<a href="login.jsp">login</a>
<%
    }
%>
</body>
</html>