package org.webapp;

import java.io.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.http.Part;


@WebServlet("/sendMail")
@MultipartConfig
public class SendMailServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        String access_token = "";
        for (Cookie c : cookies) {
            if (c.getName().compareTo("access_token") == 0) {
                access_token = c.getValue();
                break;
            }
        }
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String subject = request.getParameter("subject");
        String body = request.getParameter("body");
        Part filePart = request.getPart("attachment");
        // Set response content type
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><body>");
        out.println("<h2>Email Info Submitted</h2>");
        out.println("<p><strong>From:</strong> " + from + "</p>");
        out.println("<p><strong>To:</strong> " + to + "</p>");
        out.println("<p><strong>Body:</strong> " + body + "</p>");
        out.println("</body></html>");
        try {
            new GmailQuickstart(access_token).sendMail(from, to, subject, body, filePart);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}