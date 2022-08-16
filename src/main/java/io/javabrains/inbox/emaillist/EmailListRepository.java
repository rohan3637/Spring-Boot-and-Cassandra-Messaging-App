package io.javabrains.inbox.emaillist;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailListRepository extends CassandraRepository<EmailList, EmailListPrimaryKey> {
    
    List<EmailList> findAllById_UserIdAndId_Label(String userId, String label);
}
