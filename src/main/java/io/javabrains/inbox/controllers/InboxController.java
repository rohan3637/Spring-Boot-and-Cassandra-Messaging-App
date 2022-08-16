package io.javabrains.inbox.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import java.security.Principal;
import java.util.Date;
import org.ocpsoft.prettytime.PrettyTime;

import io.javabrains.inbox.emaillist.EmailList;
import io.javabrains.inbox.emaillist.EmailListRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.folders.FolderService;

@Controller
public class InboxController {

    @Autowired 
    private FolderRepository folderRepository;

    @Autowired
    private FolderService folderService;

    @Autowired
    private EmailListRepository emailListRepository;
    
    @GetMapping("/")
    public String homepage(@RequestParam(required = false) String folder, @AuthenticationPrincipal OAuth2User oauth2User, Model model){
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

        //Fetch messages
        if(!StringUtils.hasText(folder)){
            folder = "Inbox";
        } 
        List<EmailList> emailLists = emailListRepository.findAllById_UserIdAndId_Label(userId, folder);
        PrettyTime prettyTime = new PrettyTime();
        emailLists.stream().forEach(emailItem -> {
            UUID timeUuid = emailItem.getId().getTimeId();
            Date emailDateTime = new Date(Uuids.unixTimestamp(timeUuid));
            emailItem.setAgoTimeString(prettyTime.format(emailDateTime));
        });
        model.addAttribute("emailList", emailLists);
        model.addAttribute("folderName", folder);
        return "inbox";
    }
}
