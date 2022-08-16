package io.javabrains.inbox.folders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FolderService {

    @Autowired
    private UnreadMessageStatsRepository unreadMessageStatsRepository;
    
    public List<Folder> fetchDefaultFolders(String userId){
        return Arrays.asList(
            new Folder(userId, "Inbox", "blue"),
            new Folder(userId, "Sent Items", "green"),
            new Folder(userId, "Important", "red")
        );
    }

    //setting counters
    public Map<String, Integer> mapCountToLabels(String userId){
        List<UnreadMessageStats> stats = unreadMessageStatsRepository.findAllById(userId);
        return stats.stream()
            .collect(Collectors.toMap(UnreadMessageStats::getLabel, UnreadMessageStats::getUnreadCount));
    }
}
