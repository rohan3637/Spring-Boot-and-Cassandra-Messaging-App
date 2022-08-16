package io.javabrains;

import java.nio.file.Path;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.message.MessageService;

@SpringBootApplication
@RestController
public class InboxApp {
  
	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private MessageService messageService;

	public static void main(String[] args) {
		SpringApplication.run(InboxApp.class, args);
	}

	@RequestMapping("/user")
	public String user(@AuthenticationPrincipal OAuth2User principal) {
		System.out.println(principal);
		return principal.getAttribute("name");
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}
	
	@PostConstruct
	public void init(){
		folderRepository.save(new Folder("rohan3637", "Work", "blue"));
		folderRepository.save(new Folder("rohan3637", "Home", "green"));
		folderRepository.save(new Folder("rohan3637", "Imortant", "yellow"));

		for(int i = 0; i < 10; i++){
			messageService.sendMessage("rohan3637", Arrays.asList("rohan3637", "virat18", "rohit264"), "Hello Subjet " + i, "Body of Email " + i);
		}
	}
}
