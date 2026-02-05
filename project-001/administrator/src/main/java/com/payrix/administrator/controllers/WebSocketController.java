package com.payrix.administrator.controllers;



import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.payrix.administrator.dtos.NotificationMessage;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class WebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    @MessageMapping("/notifications")
    @SendTo("/topic/notifications")
    public NotificationMessage sendNotification(NotificationMessage message, Principal principal) {
        message.setTimestamp(LocalDateTime.now());
        message.setSender(principal.getName());
        return message;
    }
    
    @SubscribeMapping("/user/queue/notifications")
    public void subscribeUserNotifications(Principal principal) {
        // Send initial notifications when user subscribes
        NotificationMessage welcome = new NotificationMessage();
        welcome.setType("INFO");
        welcome.setContent("Welcome to Admin Panel!");
        welcome.setSender("System");
        welcome.setTimestamp(LocalDateTime.now());
        
        messagingTemplate.convertAndSendToUser(
            principal.getName(),
            "/queue/notifications",
            welcome
        );
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/admin/dashboard";
    }


}
