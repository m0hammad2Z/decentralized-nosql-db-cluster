package com.dbsim.demo.controller;
import com.dbsim.demo.model.Email;
import com.dbsim.demo.service.EmailService;
import com.dbsim.demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/email")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @PostMapping("/send")
    public String sendEmail(@RequestParam String receiver, @RequestParam String subject, @RequestParam String body, Model model) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String sender = userService.getUserByUsername(currentUser).getEmail();
        Email email = new Email(sender, receiver, subject, body);
        try {
            emailService.sendEmail(email);
            model.addAttribute("message", "Email sent successfully");
        } catch (RuntimeException | JsonProcessingException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "sendEmail";

    }

    @PostMapping("/delete")
    public String deleteEmail(@RequestParam int id, Model model) {
        try {
            emailService.deleteEmail(id);
            model.addAttribute("message", "Email deleted successfully");
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/email";
    }

    @GetMapping
    public String getEmailsPage(Model model) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userEmail = userService.getUserByUsername(currentUser).getEmail();
        List<Email> receivedEmails = emailService.getEmails(userEmail, "receiver");
        List<Email> sentEmails = emailService.getEmails(userEmail, "sender");
        model.addAttribute("receivedEmails", receivedEmails);
        model.addAttribute("sentEmails", sentEmails);

        return "emails";
    }

    @GetMapping("/send")
    public String sendEmailPage() {
        return "sendEmail";
    }
}
