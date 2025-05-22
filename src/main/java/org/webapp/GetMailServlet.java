package org.webapp;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Properties;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@MultipartConfig
public class GetMailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        String access_token = "";
        for (Cookie c : cookies) {
            if (c.getName().compareTo("access_token") == 0) {
                access_token = c.getValue();
                break;
            }
        }


        String pathInfo = request.getPathInfo();
        if("/getMailList".equals(pathInfo)){
            int page = 1;
            int pageSize = 10;
            try {
                if (request.getParameter("page") != null) {
                    page = Integer.parseInt(request.getParameter("page"));
                    if (page < 1) page = 1;
                }

                if (request.getParameter("size") != null) {
                    pageSize = Integer.parseInt(request.getParameter("size"));
                    if (pageSize < 1) pageSize = 10;
                    if (pageSize > 50) pageSize = 50; // Limit max page size
                }
            } catch (NumberFormatException e) {
                // Use defaults if parsing fails
            }
            try {
                response.getWriter().write(new GmailQuickstart(access_token).handleListEmails(page, pageSize));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if("/getMailInfo".equals(pathInfo)){
            String messageId = request.getParameter("id");
            if (messageId != null && !messageId.isEmpty()) {
                try {
                    response.getWriter().write(new GmailQuickstart(access_token).handleGetEmail(messageId, response));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                response.getWriter().write("{\"error\":\"Message ID is required\"}");
            }
        }
    }
}