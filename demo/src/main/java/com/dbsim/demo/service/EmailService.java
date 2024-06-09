package com.dbsim.demo.service;

import com.dbsim.demo.config.db.DatabaseConnection;
import com.dbsim.demo.model.ApiResponse;
import com.dbsim.demo.model.Email;
import com.dbsim.demo.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

@Service
public class EmailService {
    @Autowired
    private DatabaseConnection databaseConnection;

    @Autowired
    private UserService userService;

    public Email sendEmail(Email email) throws JsonProcessingException {
        if(email.getSender().equals(email.getReceiver())) {
            throw new RuntimeException("Sender and receiver cannot be the same");
        }

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("id", String.valueOf(email.getId() == 0 ? UUID.randomUUID().hashCode() : email.getId()));
        emailData.put("sender", email.getSender());
        emailData.put("receiver", email.getReceiver());
        emailData.put("subject", email.getSubject());
        emailData.put("body", email.getBody());

        ApiResponse<Map<String, Object>> response = databaseConnection.createDocument("emails", emailData);

        if (response.getSuccess()) {
            return email;
        } else {
            throw new RuntimeException("Failed to send email: " + response.getMessage());
        }
    }

    public List<Email> getEmails(String email, String type) {
        ApiResponse<List<Map<String, Object>>> response = databaseConnection.getDocumentsByProperty("emails", type, email);
        List<Email> emails = new ArrayList<>();
        if (response.getData() instanceof List) {
            List<Map<String, Object>> documents = response.getData();
            for (Map<String, Object> documentData : documents) {
                if (documentData.get("document") == null) {
                    continue;
                }
                Map<String, Object> emailData = (Map<String, Object>) documentData.get("document");
                Email emailObj = new Email();
                String id = (String) emailData.get("id");
                emailObj.setId(Integer.parseInt(id));
                emailObj.setSender((String) emailData.get("sender"));
                emailObj.setReceiver((String) emailData.get("receiver"));
                emailObj.setSubject((String) emailData.get("subject"));
                emailObj.setBody((String) emailData.get("body"));
                emails.add(emailObj);
            }
        }

        return emails;
    }

    public void deleteEmail(int emailId) {
        String documentId = databaseConnection.getDocumentId("emails", "id", String.valueOf(emailId));
        ApiResponse<Map<String, Object>> response = databaseConnection.deleteDocument("emails", documentId);
        if (!response.getSuccess()) {
            throw new RuntimeException("Failed to delete email: " + response.getMessage());
        }
    }

}
