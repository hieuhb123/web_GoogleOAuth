<!DOCTYPE html>
<html>
<head>
    <title>Edit Email</title>
</head>
<body>
    <h2>Edit Email</h2>
    <%
        Cookie[] cookies = request.getCookies();
        String from = "";
        for (Cookie c : cookies) {
            if (c.getName().compareTo("session_id") == 0) {
                from = c.getValue();
                break;
            }
        }

    %>
    <form method="POST" action="/sendMail" enctype="multipart/form-data">
        From: <input type="email" name="from" value=<%= from %> readonly><br><br>
        To: <input type="email" name="to" required><br><br>
        Title: <input type="text" name="subject" required><br><br>
        Text:<br>
        <textarea name="body" rows="8" cols="50" required></textarea><br><br>
        Attach file: <input type="file" name="attachment" multiple><br><br>
        <button type="submit">Submit</button>
    </form>
</body>
</html>
