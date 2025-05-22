<%@ page import="org.apache.http.Header"%>
<%@ page import="org.apache.http.HttpEntity"%>
<%@ page import="org.apache.http.client.ResponseHandler"%>
<%@ page import="org.apache.http.client.methods.CloseableHttpResponse"%>
<%@ page import="org.apache.http.client.methods.HttpPost"%>
<%@ page import="org.apache.http.client.methods.RequestBuilder"%>
<%@ page import="org.apache.http.entity.ContentType"%>
<%@ page import="org.apache.http.entity.StringEntity"%>
<%@ page import="org.apache.http.impl.client.CloseableHttpClient"%>
<%@ page import="org.apache.http.impl.client.HttpClients"%>
<%@ page import="org.apache.http.message.BasicHeader"%>
<%@ page import="org.apache.http.util.EntityUtils"%>
<%@ page import="org.apache.commons.codec.binary.Base64"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Arrays"%>
<%@ page import="java.util.List"%>
<%@ page import="com.fasterxml.jackson.databind.JsonNode"%>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@ page import="com.google.gson.JsonArray"%>
<%@ page import="com.google.gson.JsonElement"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="com.google.gson.JsonParser"%>
<%@ page import="io.github.cdimascio.dotenv.Dotenv" %>
<html>
<head>
    <title>Google OAuth: Session initialization after Google Sign-in</title>
</head>
<body>
<%


String code = (String)request.getParameter("code");
String scope = (String)request.getParameter("scope");
String session_state = (String)request.getParameter("session_state");
String prompt = (String)request.getParameter("prompt");
%>
<h3>Step 1: Google Sign-in result</h3>
<ul>
    <li>code: <%=code %></li>
    <li>scope: <%=scope %></li>
    <li>session_state: <%=session_state %></li>
    <li>prompt: <%=prompt %></li>
</ul>
    <%
    // Load environment variables
Dotenv dotenv = Dotenv.load();
// Get Google OAuth credentials from .env
String client_id = dotenv.get("GOOGLE_CLIENT_ID");
String client_secret = dotenv.get("GOOGLE_CLIENT_SECRET");
String redirect_uri = dotenv.get("GOOGLE_REDIRECT_URI");
CloseableHttpClient httpClient = HttpClients.createDefault();
HttpPost httpPost = new HttpPost("https://www.googleapis.com/oauth2/v4/token");
httpPost.setHeader("content-type", "application/x-www-form-urlencoded");
String request_body = "grant_type=authorization_code&" +
    "code=" + code + "&" +
    "client_id=" + client_id + "&" +
    "client_secret=" + client_secret + "&" +
    "redirect_uri=" + redirect_uri;
StringEntity entity = new StringEntity(request_body);
 httpPost.setEntity(entity);

 //String responseBody = httpClient.execute(httpPost, responseHandler);
 CloseableHttpResponse resp = httpClient.execute(httpPost);
 String return_body = EntityUtils.toString(resp.getEntity());
 JsonParser parser = new JsonParser();
 JsonElement jsonTree = parser.parse(return_body);
 String access_token = jsonTree.getAsJsonObject().get("access_token").toString();
 String expires_in = jsonTree.getAsJsonObject().get("expires_in").toString();
 String scope2 = jsonTree.getAsJsonObject().get("scope").toString();
 String token_type = jsonTree.getAsJsonObject().get("token_type").toString();
 String id_token = jsonTree.getAsJsonObject().get("id_token").toString();
%>
<h3>Step 2: Making call to Google OAuth server:</h3>
<ul>
    <li>client_id: <%= client_id %></li>
    <li>client_secret: <%= client_secret %></li>
</ul>
<h4>Got return from server:</h4>
<ul>
    <%= return_body %>
</ul>
<h4>Decode return values:</h4>
<ul>
    <li>access_token: <%= access_token %></li>
    <li>expires_in: <%= expires_in %></li>
    <li>scope: <%= scope2 %></li>
    <li>token_type: <%= token_type %></li>
    <li>id_token: <%= id_token %></li>
</ul>
    <%
Base64 base64Url = new Base64(true);
String[] split_string = id_token.split("\\.");
String header = new String(base64Url.decode(split_string[0]));
String body = new String(base64Url.decode(split_string[1]));
String signature = split_string[2];
%>
<h4>Decode id_token</h4>
    <ul>
        <li>header: <%= header %></li>
        <li>body: <%= body %></li>
        <li>signature: <%= signature %></li>
    </ul>
    <h3>Step 3: Working in session</h3>
    <%
        jsonTree = parser.parse(body);
        String email = jsonTree.getAsJsonObject().get("email").toString();
    %>
    <%
        Cookie sessionCookie = new Cookie("session_id", email);
        sessionCookie.setMaxAge(60 * 60 * 24); // 1 ngày
        sessionCookie.setPath("/");

        Cookie tokenCookie = new Cookie("access_token", access_token);
        tokenCookie.setMaxAge(60 * 60 * 24); // 1 giờ
        tokenCookie.setPath("/");

        response.addCookie(sessionCookie);
        response.addCookie(tokenCookie);

    %>
    <a href="../work.jsp">Continue working with Google account: <%= email%></a>

</body>
</html>