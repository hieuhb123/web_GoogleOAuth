<%@ page import="io.github.cdimascio.dotenv.Dotenv" %>
<%@ page import="java.net.URLEncoder" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link rel="stylesheet" href="css/sign-in.css">
</head>

<body class="d-flex align-items-center py-4 bg-body-tertiary">
<%
    Dotenv dotenv = Dotenv.load();
    String client_id = dotenv.get("GOOGLE_CLIENT_ID");
    String redirect_uri = dotenv.get("GOOGLE_REDIRECT_URI");
    String SCOPE = "https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/gmail.compose openid email";
    String googleOAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"+
            "?client_id=" + client_id +
            "&redirect_uri=" + URLEncoder.encode(redirect_uri, "UTF-8") +
            "&response_type=code" +
            "&scope=" + URLEncoder.encode(SCOPE, "UTF-8") +
            "&access_type=offline";

%>
    <form action="/" method="get">
      <button type="submit" class="btn-close btn-position"></button>
    </form>
    
    <main class="form-signin w-100 m-auto">
        <form action="/login" method="post">
          
          <h1 class="h3 mb-3 fw-normal">
              Please sign in
          </h1>
          <div class="form-floating">
            <input type="text" class="form-control" placeholder="name@example.com" name="username">
            <label for="floatingInput" id="message0">User name</label>
          </div>
          <div class="form-floating">
            <input type="password" class="form-control" placeholder="Password" name="password">
            <label for="floatingPassword" id="message1">Password</label>
          </div>

          <button class="btn btn-primary w-100 py-2" type="submit">Sign in</button>
        <a href="<%= googleOAuthUrl %>"
           class="btn btn-danger w-100 py-2 d-flex align-items-center justify-content-center">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-google" viewBox="0 0 16 16">
  <path d="M15.545 6.558a9.4 9.4 0 0 1 .139 1.626c0 2.434-.87 4.492-2.384 5.885h.002C11.978 15.292 10.158 16 8 16A8 8 0 1 1 8 0a7.7 7.7 0 0 1 5.352 2.082l-2.284 2.284A4.35 4.35 0 0 0 8 3.166c-2.087 0-3.86 1.408-4.492 3.304a4.8 4.8 0 0 0 0 3.063h.003c.635 1.893 2.405 3.301 4.492 3.301 1.078 0 2.004-.276 2.722-.764h-.003a3.7 3.7 0 0 0 1.599-2.431H8v-3.08z"/>
</svg>
             Login with Google
        </a>
        </form>
      </main>

      <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>

</body>
</html>