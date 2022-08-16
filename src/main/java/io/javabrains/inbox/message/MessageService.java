package io.javabrains.inbox.message;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import io.javabrains.inbox.emaillist.EmailList;
import io.javabrains.inbox.emaillist.EmailListPrimaryKey;
import io.javabrains.inbox.emaillist.EmailListRepository;
import io.javabrains.inbox.folders.UnreadMessageStatsRepository;

@Service
public class MessageService{

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EmailListRepository emailListRepository;

    @Autowired
    private UnreadMessageStatsRepository unreadMessageStatsRepository;

    public void sendMessage(String from, List<String> to, String subject, String body){
        Message message = new Message();
        message.setTo(to);
        message.setFrom(from);
        message.setBody(body);
        message.setSubject(subject);
        message.setId(Uuids.timeBased());
        messageRepository.save(message);

        to.forEach(toId -> {
            EmailList emailList = createMessageList(to, subject, message, toId, "Inbox");
            emailListRepository.save(emailList);
            unreadMessageStatsRepository.incrementUnreadCount(toId, "Inbox");
        });

        EmailList sentItems = createMessageList(to, subject, message, from, "Sent Items");
        sentItems.setRead(true);
        emailListRepository.save(sentItems);
    }

    public boolean doesHaveAcess(Message message, String userId){
        return (!userId.equals(message.getFrom()) || message.getTo().contains(userId));
    }

    public String getReplySubject(String subject){
        return "Re: " + subject;
    }

    public String getReplyBody(Message message){
        return "\n\n\n----------------------------------\n" + 
            "From: " + message.getFrom() + "\n" + 
            "To: " + message.getTo() + "\n\n" + 
            message.getBody();
    }

    private EmailList createMessageList(List<String> to, String subject, Message message, String itemOwner, String folder) {
        EmailListPrimaryKey emailListPrimaryKey = new EmailListPrimaryKey();
        emailListPrimaryKey.setUserId(itemOwner);
        emailListPrimaryKey.setLabel(folder);
        emailListPrimaryKey.setTimeId(message.getId());

        EmailList emailList = new EmailList();
        emailList.setId(emailListPrimaryKey);
        emailList.setTo(to);
        emailList.setSubject(subject);
        emailList.setRead(false);

        return emailList;
    }
}
