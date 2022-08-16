package io.javabrains.inbox.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.folders.FolderService;
import io.javabrains.inbox.message.Message;
import io.javabrains.inbox.message.MessageRepository;
import io.javabrains.inbox.message.MessageService;

@Controller
public class ComposeController {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderService folderService;   

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;
    
    @GetMapping("/compose")
    public String getComposePage(
        @RequestParam(required = false) String to,
        @RequestParam(required = false) UUID id,
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
        model.addAttribute("stats", folderService.mapCountToLabels(userId));
        model.addAttribute("userName", oauth2User.getAttribute("name"));

        if(StringUtils.hasText(to)){
            List<String> uniqueToIds = splitIds(to);
            model.addAttribute("toIds", String.join(", ", uniqueToIds));
        }
        
        Optional<Message> optionalMessage = messageRepository.findById(id);
        if(optionalMessage.isPresent()){
            Message message = optionalMessage.get();
            String toIds = String.join(", ", message.getTo());
            //check if user is allowed to see the email
            if(messageService.doesHaveAcess(message, userId)){
                model.addAttribute("subject", messageService.getReplySubject(message.getSubject()));
                model.addAttribute("body", messageService.getReplyBody(message));
            }
        }
        return "compose";
    }
    
    private List<String> splitIds(String to){
        if(!StringUtils.hasText(to)){
            return new ArrayList<String>();
        }
        String[] splitIds  = to.split(",");
        List<String> uniqueToIds = Arrays.asList(splitIds)
                .stream()
                .map(id -> StringUtils.trimWhitespace(id))
                .filter(id -> StringUtils.hasText(id))
                .distinct()
                .collect(Collectors.toList());
        return uniqueToIds;        
    }

    @PostMapping("/send-message")
    public ModelAndView sendMessages(
        @RequestBody MultiValueMap<String, String> formData,
        @AuthenticationPrincipal OAuth2User oauth2User
    ){
        if(oauth2User == null || !StringUtils.hasText(
            oauth2User.getAttribute("name"))){
            System.out.println("User not logged in !!");
            return new ModelAndView("redirect:/");
        }
        String from = oauth2User.getAttribute("login");
        List<String> toIds = splitIds(formData.getFirst("toIds"));
        String subject = formData.getFirst("subject");
        String body = formData.getFirst("body");
        messageService.sendMessage(from, toIds, subject, body);
        return new ModelAndView("redirect:/");
    }
}
