package ru.mirea.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.mirea.entity.Message;
import ru.mirea.entity.User;
import ru.mirea.repository.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class ChatController {
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @Autowired
    UserRepo userRepo;

    @Autowired
    MessageRepo messageRepo;
    Authentication authNow;
    User chatUserNow;

    @GetMapping(value = "/Chat")
    public String messenger(@RequestParam("username") String username, Model model, @ModelAttribute("new_message") Message newMessage){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authNow = auth;
        User user = userRepo.getUserByUsername(auth.getName());
        User chatUser = userRepo.getUserByUsername(username);
        chatUserNow = chatUser;
        ArrayList<Message> userNowMessages = (ArrayList<Message>) messageRepo.findAllBySenderAndRecipient(user, chatUser);
        ArrayList<Message> chatUserMessages = (ArrayList<Message>) messageRepo.findAllBySenderAndRecipient(chatUser, user);

        ArrayList<Message> messages = new ArrayList<>();

        messages.addAll(userNowMessages);
        messages.addAll(chatUserMessages);

        messages.sort(Comparator.comparing(Message::getId));
        model.addAttribute("new_message", newMessage);
        model.addAttribute("messages", messages);
        model.addAttribute("user", user);
        model.addAttribute("chat_user", chatUser);
        return "/Chat_v2";
    }

    @PostMapping(value = "/send_message")
    public String sendMessage(@RequestParam("username") String username, @ModelAttribute("new_message") Message newMessage){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.getUserByUsername(auth.getName());
        AtomicInteger i = new AtomicInteger();
        messageRepo.findAll().forEach(message -> i.getAndIncrement());
        messageRepo.save(new Message((long) i.get()+1, newMessage.getMess(), user, userRepo.getUserByUsername(username)));
        return "redirect:/Chat?username=" + username;
    }

    @Async
    @MessageMapping("/get_messages")
    @SendTo("/chat_topic/messages")
    public ArrayList<Message> getMessages(){
        User user = userRepo.getUserByUsername(authNow.getName());
        ArrayList<Message> userNowMessages = (ArrayList<Message>) messageRepo.findAllBySenderAndRecipient(user, chatUserNow);
        ArrayList<Message> chatUserMessages = (ArrayList<Message>) messageRepo.findAllBySenderAndRecipient(chatUserNow, user);

        ArrayList<Message> messages = new ArrayList<>();

        messages.addAll(userNowMessages);
        messages.addAll(chatUserMessages);

        messages.sort(Comparator.comparing(Message::getId));

        return (ArrayList<Message>) messages;

    }

    @Async
    @MessageMapping("/get_user")
    @SendTo("/chat_topic/auth_user")
    public String getAuthUser(){
         return authNow.getName();
    }

    @Async
    @MessageMapping("/send")
    @SendTo("/chat_topic/one_message")
    public Message sendMess(String mess)
    {
        System.out.println("yes");
        User user = userRepo.getUserByUsername(authNow.getName());
        AtomicInteger i = new AtomicInteger();
        messageRepo.findAll().forEach(message -> i.getAndIncrement());
        Message newMess = new Message((long) i.get()+1, mess , user, chatUserNow);
        messageRepo.save(newMess);
        return newMess;
    }
}
