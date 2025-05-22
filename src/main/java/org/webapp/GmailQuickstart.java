package org.webapp;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.services.gmail.model.Message;
import com.sun.mail.util.MailSSLSocketFactory;

import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.codec.binary.Base64;



/* class to demonstrate use of Gmail list labels API */
public class GmailQuickstart {
    private static final String APPLICATION_NAME = "Web client 1";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final Gmail service;

    public GmailQuickstart(String accessToken) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    public void sendMail(String fromEmailAddress, String to, String subject, String body, Part filePart) throws IOException, GeneralSecurityException, MessagingException {

        // Encode as MIME message
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Fix for SSL/TLS issues if needed
        try {
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.socketFactory", sf);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        Session session = Session.getInstance(props);

        // Create a new MimeMessage
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress("me"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        // Handle attachments if present
        if (filePart != null && filePart.getSize() > 0) {
            // Create a multipart message
            Multipart multipart = new MimeMultipart();

            // Set the email text
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            multipart.addBodyPart(textPart);

            // Save uploaded file to temp directory
            String fileName = getFileName(filePart);
            File tempFile = File.createTempFile("upload_", "_" + fileName);
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 InputStream is = filePart.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Create attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(tempFile);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(fileName);
            multipart.addBodyPart(attachmentPart);

            // Set the multipart as email content
            email.setContent(multipart);

            // Schedule file for deletion on JVM exit
            tempFile.deleteOnExit();
        } else {
            // Simple email without attachments
            email.setText(body);
        }

        // Encode and send the message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        // Send the message
        message = service.users().messages().send("me", message).execute();
    }

    public String handleListEmails(int page, int pageSize) throws IOException {
        // Calculate total count of messages first
        ListMessagesResponse countResponse = service.users().messages().list("me")
                .setMaxResults(1L)  // Just to get the response structure
                .execute();

        int totalCount = 0;
        if (countResponse.getResultSizeEstimate() != null) {
            totalCount = countResponse.getResultSizeEstimate().intValue();
        }

        // If we couldn't get the estimate, let's use a default value (e.g., 100)
        if (totalCount == 0) {
            totalCount = 100;
        }

        // Calculate pagination parameters
        String pageToken = null;
        int offset = (page - 1) * pageSize;

        // Get the list of messages with pagination
        ListMessagesResponse listResponse;

        if (offset > 0) {
            // For pagination, we need to get the pageToken for the desired page
            // First, get the first page to get the first pageToken
            listResponse = service.users().messages().list("me")
                    .setMaxResults((long) pageSize)
                    .execute();

            pageToken = listResponse.getNextPageToken();

            // Continue fetching page tokens until we reach the desired page
            int currentOffset = pageSize;
            while (pageToken != null && currentOffset < offset) {
                listResponse = service.users().messages().list("me")
                        .setPageToken(pageToken)
                        .setMaxResults((long) pageSize)
                        .execute();

                pageToken = listResponse.getNextPageToken();
                currentOffset += pageSize;
            }

            // Now get the actual page we want
            listResponse = service.users().messages().list("me")
                    .setPageToken(pageToken)
                    .setMaxResults((long) pageSize)
                    .execute();
        } else {
            // Just get the first page
            listResponse = service.users().messages().list("me")
                    .setMaxResults((long) pageSize)
                    .execute();
        }

        if (listResponse.getMessages() != null) {
            // Get snippets for each message
            for (Message message : listResponse.getMessages()) {
                Message fullMessage = service.users().messages().get("me", message.getId())
                        .setFormat("metadata")
                        .setMetadataHeaders(Collections.singletonList("Subject"))
                        .execute();

                message.setSnippet(fullMessage.getSnippet());
                message.setPayload(fullMessage.getPayload()); // Include headers for subject
            }
        }

        // Build the response JSON with pagination details
        StringBuilder responseJson = new StringBuilder();
        responseJson.append("{");
        responseJson.append("\"totalCount\":").append(totalCount).append(",");
        responseJson.append("\"pageSize\":").append(pageSize).append(",");
        responseJson.append("\"currentPage\":").append(page).append(",");
        responseJson.append("\"hasNextPage\":").append(listResponse.getNextPageToken() != null).append(",");
        responseJson.append("\"messages\":");

        if (listResponse.getMessages() != null) {
            responseJson.append(JSON_FACTORY.toString(listResponse.getMessages()));
        } else {
            responseJson.append("[]");
        }

        responseJson.append("}");
        System.out.println(responseJson.toString());
        return responseJson.toString();
    }

    public String handleGetEmail(String messageId, HttpServletResponse resp) throws IOException {
        Message message = service.users().messages().get("me", messageId).execute();

        // Extract the email body (simplified approach)
        String body = "No content";
        if (message.getPayload() != null && message.getPayload().getBody() != null &&
                message.getPayload().getBody().getData() != null) {
            body = new String(Base64.decodeBase64(message.getPayload().getBody().getData()));
        } else if (message.getPayload() != null && message.getPayload().getParts() != null) {
            // Try to get body from parts (for multipart emails)
            for (MessagePart part : message.getPayload().getParts()) {
                if (part.getMimeType().equals("text/plain") && part.getBody() != null && part.getBody().getData() != null) {
                    body = new String(Base64.decodeBase64(part.getBody().getData()));
                    break;
                }
            }
        }
        return "{\"id\":\"" + messageId + "\",\"body\":\"" +
                escapeJsonString(body) + "\"}";

    }

    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "attachment";
    }
}