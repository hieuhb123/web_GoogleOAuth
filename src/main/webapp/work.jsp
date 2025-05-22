<html>
<head>
    <title>Google OAuth: Working in session</title>
</head>
<body>

<%
    Cookie[] cookies = request.getCookies();
    String logout = (String)request.getParameter("logout");
    if (logout != null) {
        for (int i=0; i<cookies.length; i++) {
            if (cookies[i].getName().compareTo("session_id") == 0) {
                cookies[i].setMaxAge(0);
                response.addCookie(cookies[i]);
                response.sendRedirect("login.jsp");
                return;
            }
        }
    }
    boolean session_validate = false;
    if (cookies != null) {
        for (Cookie c : cookies) {
            if (c.getName().compareTo("session_id") == 0) {
                session_validate = true;

%>
<h3>You are working in a session identified by following cookie:</h3>
<%= c.getName() %>: <%= c.getValue() %>
<br>
<a href="inbox.jsp">Mail box</a>
<br>
<a href="compose.jsp">compose letter</a>
<br>
<a href="work.jsp?logout=yes">Logout</a>

<%
        }
    }
}
if (!session_validate) {
%>
<h3>Your session has terminated</h3>

Please <a href="login.jsp">login</a> again.
<%
    }
%>

</body>
</html>