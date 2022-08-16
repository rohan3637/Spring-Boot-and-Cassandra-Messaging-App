package io.javabrains.inbox.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import java.util.Date;
import org.ocpsoft.prettytime.PrettyTime;

import io.javabrains.inbox.emaillist.EmailList;
import io.javabrains.inbox.emaillist.EmailListPrimaryKey;
import io.javabrains.inbox.emaillist.EmailListRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.folders.FolderService;
import io.javabrains.inbox.folders.UnreadMessageStatsRepository;
import io.javabrains.inbox.message.Message;
import io.javabrains.inbox.message.MessageRepository;
import io.javabrains.inbox.message.MessageService;

@Controller
public class MessageController {
    
    @Autowired 
    private FolderRepository folderRepository;

    @Autowired
    private FolderService folderService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmailListRepository emailListRepository;

    @Autowired 
    private MessageRepository messageRepository;

    @Autowired 
    private UnreadMessageStatsRepository unreadMessageStatsRepository;
    
    @GetMapping("/messages/{id}")
    public String messageController(
        @RequestParam(required = false) String folder, 
        @PathVariable UUID id, 
        @AuthenticationPrincipal OAuth2User oauth2User, 
        Model model
    ){
        if(oauth2User == null || !StringUtils.hasText(oauth2User.getAttribute("name"))){
            System.out.println("User not logged in !!");
            return "index";
        }
        //Fetch folders
        String userId = oauth2User.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(userId);
        model.addAttribute("userFolders", userFolders);
        List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
        model.addAttribute("defaultFolders", defaultFolders);
        model.addAttribute("userName", oauth2User.getAttribute("name"));

        model.addAttribute("folderName", folder); 

        //show speific messages
        Optional<Message> optionalMessage = messageRepository.findById(id);
        if(!optionalMessage.isPresent()){
            System.out.println("Message doesnot exits with this id!!");
            return "inbox";
        }
        Message message = optionalMessage.get();
        String toIds = String.join(", ", message.getTo());

        //check if user is allowed to see the email
        if(!messageService.doesHaveAcess(message, userId)){
            return "redirect:/";
        }

        model.addAttribute("message", message);
        model.addAttribute("toIds", toIds);

        EmailListPrimaryKey key = new EmailListPrimaryKey();
        key.setUserId(userId);
        key.setLabel(folder);
        key.setTimeId(message.getId());

        Optional<EmailList> optionalEmailList = emailListRepository.findById(key);
        if(optionalEmailList.isPresent()){
            EmailList emailList = optionalEmailList.get();
            if(!emailList.isRead()){
                emailList.setRead(true);
                emailListRepository.save(emailList);
                unreadMessageStatsRepository.decrementUnreadCount(userId, folder);
            }
        }
        model.addAttribute("stats", folderService.mapCountToLabels(userId));
        return "message";
    }
}
